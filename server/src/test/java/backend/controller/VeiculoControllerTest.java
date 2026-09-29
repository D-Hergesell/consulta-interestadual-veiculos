package backend.controller;

import backend.config.CorsConfig;
import backend.config.SecurityConfig;
import backend.exception.PlacaInvalidaException;
import backend.exception.PlacaNaoEncontradaException;
import backend.services.VeiculoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = VeiculoController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, CorsConfig.class})
class VeiculoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VeiculoService veiculoService;

    @Test
    void placaNaoEncontradaRetorna404() throws Exception {
        when(veiculoService.consultarPorPlaca("ABC1234")).thenThrow(new PlacaNaoEncontradaException("ABC1234"));

        mockMvc.perform(get("/api/veiculos/ABC1234"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void placaInvalidaRetorna400() throws Exception {
        when(veiculoService.consultarPorPlaca("XYZ")).thenThrow(new PlacaInvalidaException());

        mockMvc.perform(get("/api/veiculos/XYZ"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void metodosDeEscritaSaoNegados() throws Exception {
        mockMvc.perform(post("/api/veiculos/ABC1234")).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/veiculos/ABC1234")).andExpect(status().isForbidden());
    }

    @Test
    void respostaIncluiCabecalhosDeSeguranca() throws Exception {
        when(veiculoService.consultarPorPlaca("ABC1234")).thenThrow(new PlacaNaoEncontradaException("ABC1234"));

        mockMvc.perform(get("/api/veiculos/ABC1234"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }
}
