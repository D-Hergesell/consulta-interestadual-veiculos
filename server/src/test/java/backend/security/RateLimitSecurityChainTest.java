package backend.security;

import backend.config.CorsConfig;
import backend.config.SecurityConfig;
import backend.controller.EstadoController;
import backend.services.EstadoService;
import backend.services.ServicoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Garante que o filtro esta registrado na cadeia do Spring Security, depois do CORS. */
@WebMvcTest(controllers = EstadoController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class,
        properties = {
                "app.rate-limit.requests-per-minute=1",
                "app.cors.allowed-origins=http://localhost:5173"
        })
@Import({SecurityConfig.class, CorsConfig.class})
class RateLimitSecurityChainTest {

    private static final String ORIGEM = "http://localhost:5173";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstadoService estadoService;

    @MockitoBean
    private ServicoService servicoService;

    @Test
    void segundaRequisicaoNoMinutoRecebe429ComHeadersCors() throws Exception {
        mockMvc.perform(get("/api/estados").header("Origin", ORIGEM))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/estados").header("Origin", ORIGEM))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGEM))
                // Sem o expose, o JavaScript do front nao consegue ler o Retry-After
                .andExpect(header().string("Access-Control-Expose-Headers", containsString("Retry-After")))
                .andExpect(jsonPath("$.status").value(429));
    }
}
