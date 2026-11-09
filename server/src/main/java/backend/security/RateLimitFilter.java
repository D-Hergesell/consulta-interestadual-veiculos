package backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * Limita requisicoes em /api/** por cliente, em janelas fixas de 1 minuto. Acima do limite responde
 * 429 (ProblemDetail) com Retry-After.
 * <p>
 * Cliente = IPv4 exato ou o prefixo /64 do IPv6 (um provedor entrega um /64 inteiro para cada
 * assinante, entao contar por endereco IPv6 exato deixaria o atacante trocar de IP a vontade).
 * As rotas de placa (/api/veiculos/**) tem contador e limite proprios, menores que o do restante da API.
 * <p>
 * Os contadores ficam em memoria e a cada janela nova o mapa inteiro e descartado, entao a memoria
 * fica limitada aos clientes vistos no ultimo minuto. Vale por instancia: com varias replicas o limite
 * efetivo e multiplicado pelo numero de replicas.
 */
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long JANELA_MS = Duration.ofMinutes(1).toMillis();
    private static final String PREFIXO_API = "/api/";
    private static final String PREFIXO_PLACA = "/api/veiculos/";
    /** So digitos, hexa, ':' e '.': aceita IPv4/IPv6 e nunca um nome de host (que dispararia DNS). */
    private static final Pattern PARECE_IP = Pattern.compile("[0-9A-Fa-f:.]{2,45}");

    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final AtomicReference<Janela> janelaAtual;

    public RateLimitFilter(RateLimitProperties properties, ObjectMapper objectMapper, Clock clock) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.janelaAtual = new AtomicReference<>(new Janela(clock.millis() / JANELA_MS));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !properties.enabled() || !caminho(request).startsWith(PREFIXO_API);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long agora = clock.millis();
        Janela janela = janelaDe(agora);

        boolean rotaDePlaca = caminho(request).startsWith(PREFIXO_PLACA);
        int limite = rotaDePlaca ? properties.placaRequestsPerMinute() : properties.requestsPerMinute();
        String cliente = chaveDoCliente(ipDoCliente(request));
        String chave = (rotaDePlaca ? "placa|" : "api|") + cliente;
        int total = janela.contadores().computeIfAbsent(chave, c -> new AtomicInteger()).incrementAndGet();

        if (total <= limite) {
            chain.doFilter(request, response);
            return;
        }
        // Loga so a primeira rejeicao de cada cliente na janela, para nao inundar o log durante um abuso
        if (total == limite + 1) {
            log.warn("Limite de {} requisições/min ({}) excedido por {}", limite,
                    rotaDePlaca ? "placa" : "api", cliente);
        }
        rejeitar(request, response, segundosAteProximaJanela(janela, agora));
    }

    private static String caminho(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    /**
     * IP usado no limite. Padrao: o IP da conexao. Com app.rate-limit.ip-header configurado (ex.:
     * CF-Connecting-IP atras do proxy do Render), usa o primeiro valor desse header, desde que seja um
     * IP valido; se o header faltar ou vier lixo, volta para o IP da conexao.
     * <p>
     * So configure um header que o proxy SOBRESCREVE. Header que o cliente consegue enviar e o proxy so
     * repassa (como o X-Forwarded-For no Render) deixa o atacante escolher o proprio IP e escapar do limite.
     */
    String ipDoCliente(HttpServletRequest request) {
        String remoto = request.getRemoteAddr();
        String nomeHeader = properties.ipHeader();
        if (log.isDebugEnabled()) {
            // Diagnostico para escolher o header no deploy: ligue com LOGGING_LEVEL_BACKEND_SECURITY=DEBUG
            log.debug("Origem do IP: remoteAddr={} | X-Forwarded-For={} | CF-Connecting-IP={} | True-Client-IP={}",
                    remoto, request.getHeader("X-Forwarded-For"), request.getHeader("CF-Connecting-IP"),
                    request.getHeader("True-Client-IP"));
        }
        if (nomeHeader == null || nomeHeader.isBlank()) {
            return remoto;
        }
        String valor = request.getHeader(nomeHeader);
        if (valor == null) {
            return remoto;
        }
        String primeiro = valor.split(",", 2)[0].strip();
        return PARECE_IP.matcher(primeiro).matches() ? primeiro : remoto;
    }

    /**
     * IPv4 fica como esta; IPv6 vira o prefixo /64 (ex.: 2001:db8:1:2::/64).
     * IPv4 mapeado em IPv6 (::ffff:10.0.0.1) volta para a forma IPv4.
     */
    static String chaveDoCliente(String ip) {
        if (ip == null || ip.indexOf(':') < 0) {
            return ip;
        }
        // Remove o escopo de link-local (fe80::1%eth0) antes de converter
        int escopo = ip.indexOf('%');
        String literal = escopo < 0 ? ip : ip.substring(0, escopo);
        try {
            // Com literal IP o getByName so valida o formato, sem consulta DNS
            InetAddress endereco = InetAddress.getByName(literal);
            if (endereco instanceof Inet6Address) {
                byte[] bytes = endereco.getAddress();
                StringBuilder prefixo = new StringBuilder();
                for (int i = 0; i < 8; i += 2) {
                    prefixo.append(Integer.toHexString(((bytes[i] & 0xff) << 8) | (bytes[i + 1] & 0xff))).append(':');
                }
                return prefixo.append(":/64").toString();
            }
            return endereco.getHostAddress();
        } catch (UnknownHostException e) {
            return ip;
        }
    }

    /** Devolve a janela do instante informado, trocando a atual quando o minuto virou. */
    private Janela janelaDe(long agora) {
        long indice = agora / JANELA_MS;
        Janela atual = janelaAtual.get();
        if (atual.indice() >= indice) {
            return atual;
        }
        Janela nova = new Janela(indice);
        // Se outra thread trocou antes, usa a janela que ela criou
        return janelaAtual.compareAndSet(atual, nova) ? nova : janelaAtual.get();
    }

    private static long segundosAteProximaJanela(Janela janela, long agora) {
        long restanteMs = (janela.indice() + 1) * JANELA_MS - agora;
        // Arredonda para cima: o cliente nunca e orientado a tentar antes da janela virar
        return Math.max(1, Math.ceilDiv(restanteMs, 1000L));
    }

    private void rejeitar(HttpServletRequest request, HttpServletResponse response, long retryAfterSegundos)
            throws IOException {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS,
                "Muitas requisições. Tente novamente em " + retryAfterSegundos + " segundos.");
        problema.setInstance(URI.create(request.getRequestURI()));

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSegundos));
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), problema);
    }

    private record Janela(long indice, ConcurrentMap<String, AtomicInteger> contadores) {

        Janela(long indice) {
            this(indice, new ConcurrentHashMap<>());
        }
    }
}
