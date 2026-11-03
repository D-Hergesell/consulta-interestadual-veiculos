package backend.controller;

import backend.config.CorsConfig;
import backend.config.SecurityConfig;
import backend.entities.Veiculo;
import backend.enums.Situacao;
import backend.enums.StatusPagamento;
import backend.mapper.VeiculoMapper;
import backend.repository.MultaRepository;
import backend.repository.OcorrenciaFurtoRouboRepository;
import backend.repository.RestricaoRepository;
import backend.repository.VeiculoRepository;
import backend.services.VeiculoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prova de posse com o VeiculoService real (so os repositorios sao mockados): sem RENAVAM, RENAVAM
 * errado e placa inexistente precisam dar exatamente a mesma resposta.
 */
@WebMvcTest(controllers = VeiculoController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, CorsConfig.class, VeiculoService.class, VeiculoMapper.class})
class VeiculoProvaDePosseTest {

    private static final String PLACA = "ABC1234";
    private static final String RENAVAM = "00123456789";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VeiculoRepository veiculoRepository;
    @MockitoBean
    private OcorrenciaFurtoRouboRepository ocorrenciaRepository;
    @MockitoBean
    private RestricaoRepository restricaoRepository;
    @MockitoBean
    private MultaRepository multaRepository;

    @BeforeEach
    void setUp() {
        Veiculo veiculo = mock(Veiculo.class);
        when(veiculo.getId()).thenReturn(1);
        when(veiculo.getPlaca()).thenReturn(PLACA);
        when(veiculo.getRenavam()).thenReturn(RENAVAM);
        when(veiculo.getSituacao()).thenReturn(Situacao.REGULAR);
        when(veiculoRepository.findByPlaca(PLACA)).thenReturn(Optional.of(veiculo));
        when(veiculoRepository.findByPlaca("XYZ9876")).thenReturn(Optional.empty());
        when(ocorrenciaRepository.findByVeiculoIdAndAtivoTrueOrderByDataOcorrenciaDesc(anyInt())).thenReturn(List.of());
        when(restricaoRepository.findByVeiculoIdAndAtivaTrueOrderByDataInclusaoDesc(anyInt())).thenReturn(List.of());
        when(multaRepository.findNaoPagas(anyInt(), any(StatusPagamento.class))).thenReturn(List.of());
        when(multaRepository.findByVeiculoIdOrderByDataInfracaoDesc(anyInt())).thenReturn(List.of());
    }

    @Test
    void multasSemRenavamRetorna404() throws Exception {
        mockMvc.perform(get("/api/veiculos/ABC1234/multas"))
                .andExpect(status().isNotFound());
        verify(multaRepository, never()).findByVeiculoIdOrderByDataInfracaoDesc(anyInt());
    }

    @Test
    void multasComRenavamErradoRetorna404() throws Exception {
        mockMvc.perform(comRenavam(get("/api/veiculos/ABC1234/multas"), "00123456780"))
                .andExpect(status().isNotFound());
        verify(multaRepository, never()).findByVeiculoIdOrderByDataInfracaoDesc(anyInt());
    }

    @Test
    void multasComRenavamCertoRetorna200() throws Exception {
        mockMvc.perform(comRenavam(get("/api/veiculos/ABC1234/multas"), RENAVAM))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void renavamAceitaPontuacaoEZerosOmitidos() throws Exception {
        mockMvc.perform(comRenavam(get("/api/veiculos/ABC1234/detalhes"), "123.456.789"))
                .andExpect(status().isOk());
    }

    @Test
    void detalhesSemRenavamErradoECertoSeComportamComoEsperado() throws Exception {
        mockMvc.perform(get("/api/veiculos/ABC1234/detalhes")).andExpect(status().isNotFound());
        mockMvc.perform(comRenavam(get("/api/veiculos/ABC1234/detalhes"), "99999999999"))
                .andExpect(status().isNotFound());
        mockMvc.perform(comRenavam(get("/api/veiculos/ABC1234/detalhes"), RENAVAM))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.veiculo.placa").value(PLACA))
                .andExpect(jsonPath("$.multasPendentes").isArray())
                .andExpect(jsonPath("$.restricoesAtivas").isArray())
                .andExpect(jsonPath("$.ocorrenciasAtivas").isArray());
    }

    @Test
    void renavamErradoTemCorpoIgualAoDePlacaInexistente() throws Exception {
        String placaInexistente = corpo(comRenavam(get("/api/veiculos/XYZ9876/multas"), RENAVAM));
        String semRenavam = corpo(get("/api/veiculos/ABC1234/multas"));
        String renavamErrado = corpo(comRenavam(get("/api/veiculos/ABC1234/multas"), "11111111111"));

        // Muda so a placa citada na mensagem (que o proprio cliente enviou); a estrutura e o texto sao iguais
        assertThat(semRenavam).isEqualTo(renavamErrado);
        assertThat(renavamErrado.replace(PLACA, "XYZ9876").replace("/ABC1234/", "/XYZ9876/"))
                .isEqualTo(placaInexistente);
    }

    private static MockHttpServletRequestBuilder comRenavam(MockHttpServletRequestBuilder req, String renavam) {
        return req.header(VeiculoController.HEADER_RENAVAM, renavam);
    }

    private String corpo(MockHttpServletRequestBuilder req) throws Exception {
        return mockMvc.perform(req).andExpect(status().isNotFound()).andReturn().getResponse().getContentAsString();
    }
}
