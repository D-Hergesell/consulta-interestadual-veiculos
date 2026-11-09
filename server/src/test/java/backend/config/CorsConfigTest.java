package backend.config;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Origens do front: exata, com curinga de subdominio (deploys de preview) e lista com espacos. */
@WebMvcTest(controllers = EstadoController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class,
        properties = "app.cors.allowed-origins=http://localhost:5173 , https://*.vercel.app")
@Import({SecurityConfig.class, CorsConfig.class})
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstadoService estadoService;
    @MockitoBean
    private ServicoService servicoService;

    @Test
    void origemExataEhLiberada() throws Exception {
        mockMvc.perform(get("/api/estados").header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void origemDePreviewNoPadraoEhLiberada() throws Exception {
        mockMvc.perform(get("/api/estados").header("Origin", "https://consulta-veiculos-git-feat-x.vercel.app"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin",
                        "https://consulta-veiculos-git-feat-x.vercel.app"));
    }

    @Test
    void origemForaDaListaEhRecusada() throws Exception {
        mockMvc.perform(get("/api/estados").header("Origin", "https://site-malicioso.example"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void preflightLiberaOHeaderDoRenavam() throws Exception {
        mockMvc.perform(options("/api/veiculos/ABC1234/detalhes")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "X-Renavam"))
                .andExpect(status().isOk());
    }
}
