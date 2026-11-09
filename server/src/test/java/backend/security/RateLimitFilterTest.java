package backend.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    private static final int LIMITE = 2;
    private static final int LIMITE_PLACA = 1;

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
    // Comeca 30s depois do inicio de um minuto, para o Retry-After ser previsivel
    private final RelogioAjustavel relogio = new RelogioAjustavel(Instant.parse("2026-10-20T12:00:30Z"));

    @Test
    void permiteAteOLimiteERejeitaComProblemDetailERetryAfter() throws Exception {
        RateLimitFilter filtro = filtro(true);

        assertThat(executar(filtro, "/api/estados", "10.0.0.1").getStatus()).isEqualTo(200);
        assertThat(executar(filtro, "/api/estados", "10.0.0.1").getStatus()).isEqualTo(200);

        MockHttpServletResponse rejeitada = executar(filtro, "/api/estados", "10.0.0.1");
        assertThat(rejeitada.getStatus()).isEqualTo(429);
        assertThat(rejeitada.getHeader("Retry-After")).isEqualTo("30");
        assertThat(rejeitada.getContentType()).startsWith("application/problem+json");

        JsonNode corpo = objectMapper.readTree(rejeitada.getContentAsByteArray());
        assertThat(corpo.get("status").asInt()).isEqualTo(429);
        assertThat(corpo.get("instance").asText()).isEqualTo("/api/estados");
        assertThat(corpo.has("properties")).isFalse();
    }

    @Test
    void requisicaoRejeitadaNaoChegaAoController() throws Exception {
        RateLimitFilter filtro = filtro(true);
        executar(filtro, "/api/estados", "10.0.0.1");
        executar(filtro, "/api/estados", "10.0.0.1");

        MockFilterChain chain = new MockFilterChain();
        filtro.doFilter(requisicao("/api/estados", "10.0.0.1"), new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void contaCadaIpSeparadamente() throws Exception {
        RateLimitFilter filtro = filtro(true);
        executar(filtro, "/api/estados", "10.0.0.1");
        executar(filtro, "/api/estados", "10.0.0.1");

        assertThat(executar(filtro, "/api/estados", "10.0.0.2").getStatus()).isEqualTo(200);
    }

    @Test
    void ipv6DoMesmoPrefixo64CompartilhaOContador() throws Exception {
        RateLimitFilter filtro = filtro(true);
        executar(filtro, "/api/estados", "2001:db8:1:2::1");
        executar(filtro, "/api/estados", "2001:db8:1:2:aaaa:bbbb:cccc:dddd");

        assertThat(executar(filtro, "/api/estados", "2001:db8:1:2:ffff::9").getStatus()).isEqualTo(429);
    }

    @Test
    void ipv6DePrefixos64DiferentesContaSeparado() throws Exception {
        RateLimitFilter filtro = filtro(true);
        executar(filtro, "/api/estados", "2001:db8:1:2::1");
        executar(filtro, "/api/estados", "2001:db8:1:2::2");

        assertThat(executar(filtro, "/api/estados", "2001:db8:1:3::1").getStatus()).isEqualTo(200);
    }

    @Test
    void chaveDoCliente() {
        assertThat(RateLimitFilter.chaveDoCliente("10.0.0.1")).isEqualTo("10.0.0.1");
        assertThat(RateLimitFilter.chaveDoCliente("2001:DB8:0:2:1:2:3:4")).isEqualTo("2001:db8:0:2::/64");
        assertThat(RateLimitFilter.chaveDoCliente("fe80::1%eth0")).isEqualTo("fe80:0:0:0::/64");
        assertThat(RateLimitFilter.chaveDoCliente("::ffff:10.0.0.1")).isEqualTo("10.0.0.1");
    }

    @Test
    void rotasDePlacaTemLimiteProprio() throws Exception {
        RateLimitFilter filtro = filtro(true);
        assertThat(executar(filtro, "/api/veiculos/ABC1234", "10.0.0.1").getStatus()).isEqualTo(200);

        assertThat(executar(filtro, "/api/veiculos/ABC1235", "10.0.0.1").getStatus()).isEqualTo(429);
        // O restante da API continua com o contador e o limite gerais
        assertThat(executar(filtro, "/api/estados", "10.0.0.1").getStatus()).isEqualTo(200);
        assertThat(executar(filtro, "/api/estados", "10.0.0.1").getStatus()).isEqualTo(200);
    }

    @Test
    void liberaNovamenteQuandoAJanelaVira() throws Exception {
        RateLimitFilter filtro = filtro(true);
        executar(filtro, "/api/estados", "10.0.0.1");
        executar(filtro, "/api/estados", "10.0.0.1");
        assertThat(executar(filtro, "/api/estados", "10.0.0.1").getStatus()).isEqualTo(429);

        relogio.avancar(Duration.ofSeconds(30));

        assertThat(executar(filtro, "/api/estados", "10.0.0.1").getStatus()).isEqualTo(200);
    }

    @Test
    void ignoraRotasForaDaApi() throws Exception {
        RateLimitFilter filtro = filtro(true);
        for (int i = 0; i < LIMITE + 3; i++) {
            assertThat(executar(filtro, "/swagger-ui.html", "10.0.0.1").getStatus()).isEqualTo(200);
        }
    }

    @Test
    void desabilitadoNaoLimita() throws Exception {
        RateLimitFilter filtro = filtro(false);
        for (int i = 0; i < LIMITE + 3; i++) {
            assertThat(executar(filtro, "/api/estados", "10.0.0.1").getStatus()).isEqualTo(200);
        }
    }

    private RateLimitFilter filtro(boolean habilitado) {
        return new RateLimitFilter(new RateLimitProperties(habilitado, LIMITE, LIMITE_PLACA, null), objectMapper, relogio);
    }

    private RateLimitFilter filtroComHeaderDeIp(String header) {
        return new RateLimitFilter(new RateLimitProperties(true, LIMITE, LIMITE_PLACA, header), objectMapper, relogio);
    }

    @Test
    void comHeaderDeIpConfiguradoContaPeloIpDoHeaderENaoPeloProxy() throws Exception {
        RateLimitFilter filtro = filtroComHeaderDeIp("CF-Connecting-IP");
        // Todas as requisicoes chegam do mesmo proxy (10.0.0.1), mas de clientes diferentes
        executar(filtro, requisicao("/api/estados", "10.0.0.1", "CF-Connecting-IP", "200.1.1.1"));
        executar(filtro, requisicao("/api/estados", "10.0.0.1", "CF-Connecting-IP", "200.1.1.1"));
        assertThat(executar(filtro, requisicao("/api/estados", "10.0.0.1", "CF-Connecting-IP", "200.1.1.1"))
                .getStatus()).isEqualTo(429);

        assertThat(executar(filtro, requisicao("/api/estados", "10.0.0.1", "CF-Connecting-IP", "200.2.2.2"))
                .getStatus()).isEqualTo(200);
    }

    @Test
    void headerDeIpUsaSoOPrimeiroValorDaLista() {
        RateLimitFilter filtro = filtroComHeaderDeIp("X-Client-IP");
        assertThat(filtro.ipDoCliente(requisicao("/api/estados", "10.0.0.1", "X-Client-IP", " 200.1.1.1 , 10.9.9.9")))
                .isEqualTo("200.1.1.1");
    }

    @Test
    void headerDeIpAusenteOuInvalidoVoltaParaOIpDaConexao() {
        RateLimitFilter filtro = filtroComHeaderDeIp("CF-Connecting-IP");
        assertThat(filtro.ipDoCliente(requisicao("/api/estados", "10.0.0.1"))).isEqualTo("10.0.0.1");
        assertThat(filtro.ipDoCliente(requisicao("/api/estados", "10.0.0.1", "CF-Connecting-IP", "evil.example.com")))
                .isEqualTo("10.0.0.1");
        assertThat(filtro.ipDoCliente(requisicao("/api/estados", "10.0.0.1", "CF-Connecting-IP", "")))
                .isEqualTo("10.0.0.1");
    }

    @Test
    void semHeaderConfiguradoIgnoraHeadersDeIpDoCliente() {
        // Padrao seguro: um X-Forwarded-For enviado pelo proprio cliente nao muda nada
        RateLimitFilter filtro = filtro(true);
        assertThat(filtro.ipDoCliente(requisicao("/api/estados", "10.0.0.1", "X-Forwarded-For", "1.2.3.4")))
                .isEqualTo("10.0.0.1");
    }

    private static MockHttpServletResponse executar(RateLimitFilter filtro, MockHttpServletRequest request)
            throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filtro.doFilter(request, response, new MockFilterChain());
        return response;
    }

    private static MockHttpServletRequest requisicao(String uri, String ip, String header, String valor) {
        MockHttpServletRequest request = requisicao(uri, ip);
        request.addHeader(header, valor);
        return request;
    }

    private static MockHttpServletResponse executar(RateLimitFilter filtro, String uri, String ip) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filtro.doFilter(requisicao(uri, ip), response, new MockFilterChain());
        return response;
    }

    private static MockHttpServletRequest requisicao(String uri, String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRemoteAddr(ip);
        return request;
    }

    /** Clock que o teste consegue avancar, para simular a virada da janela sem esperar. */
    private static final class RelogioAjustavel extends Clock {

        private Instant agora;

        RelogioAjustavel(Instant inicio) {
            this.agora = inicio;
        }

        void avancar(Duration duracao) {
            agora = agora.plus(duracao);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return agora;
        }
    }
}
