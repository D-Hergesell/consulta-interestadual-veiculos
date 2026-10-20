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
        return new RateLimitFilter(new RateLimitProperties(habilitado, LIMITE), objectMapper, relogio);
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
