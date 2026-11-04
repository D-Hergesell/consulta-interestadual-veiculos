package backend.controller;

import backend.config.CorsConfig;
import backend.config.SecurityConfig;
import backend.dto.InstrucaoTransferenciaResponse;
import backend.dto.RegraEspecialResponse;
import backend.enums.TipoRegra;
import backend.exception.OrigemDestinoIguaisException;
import backend.services.TransferenciaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransferenciaController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, CorsConfig.class})
class TransferenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransferenciaService transferenciaService;

    @Test
    void procedimentoPadraoSaiComRegrasEspeciaisNoJson() throws Exception {
        when(transferenciaService.buscarInstrucao("RJ", "SC")).thenReturn(InstrucaoTransferenciaResponse.builder()
                .ufOrigem("RJ").ufDestino("SC")
                .procedimentoPadrao(true)
                .passoAPasso("passos padrao")
                .taxaEstimada(new BigDecimal("220.00"))
                .prazoDias(30)
                .documentosNecessarios("CRV")
                .observacaoLegal("CTB")
                .regrasEspeciaisDestino(List.of(new RegraEspecialResponse(TipoRegra.ISENCAO_VISTORIA,
                        "RENAVE Usados dispensa nova vistoria", "descricao", null)))
                .build());

        mockMvc.perform(get("/api/transferencias").param("origem", "RJ").param("destino", "SC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.procedimentoPadrao").value(true))
                .andExpect(jsonPath("$.observacaoLegal").value("CTB"))
                .andExpect(jsonPath("$.regrasEspeciaisDestino[0].tipoRegra").value("ISENCAO_VISTORIA"))
                .andExpect(jsonPath("$.regrasEspeciaisDestino[0].titulo").value("RENAVE Usados dispensa nova vistoria"));
    }

    @Test
    void origemIgualAoDestinoRetorna400() throws Exception {
        when(transferenciaService.buscarInstrucao("SC", "SC")).thenThrow(new OrigemDestinoIguaisException("SC"));

        mockMvc.perform(get("/api/transferencias").param("origem", "SC").param("destino", "SC"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void ufInvalidaRetorna400() throws Exception {
        mockMvc.perform(get("/api/transferencias").param("origem", "S1").param("destino", "SC"))
                .andExpect(status().isBadRequest());
    }
}
