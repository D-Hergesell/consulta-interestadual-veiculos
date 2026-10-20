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
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Limita requisicoes em /api/** por IP, em janelas fixas de 1 minuto. Acima do limite responde
 * 429 (ProblemDetail) com Retry-After.
 * <p>
 * Os contadores ficam em memoria e a cada janela nova o mapa inteiro e descartado, entao a memoria
 * fica limitada aos IPs vistos no ultimo minuto. Vale por instancia: com varias replicas o limite
 * efetivo e multiplicado pelo numero de replicas.
 */
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long JANELA_MS = Duration.ofMinutes(1).toMillis();
    private static final String PREFIXO_API = "/api/";

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
        String caminho = request.getRequestURI().substring(request.getContextPath().length());
        return !properties.enabled() || !caminho.startsWith(PREFIXO_API);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long agora = clock.millis();
        Janela janela = janelaDe(agora);
        String ip = request.getRemoteAddr();
        int total = janela.contadores().computeIfAbsent(ip, chave -> new AtomicInteger()).incrementAndGet();

        if (total <= properties.requestsPerMinute()) {
            chain.doFilter(request, response);
            return;
        }
        // Loga so a primeira rejeicao de cada IP na janela, para nao inundar o log durante um abuso
        if (total == properties.requestsPerMinute() + 1) {
            log.warn("Limite de {} requisições/min excedido pelo IP {}", properties.requestsPerMinute(), ip);
        }
        rejeitar(request, response, segundosAteProximaJanela(janela, agora));
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
