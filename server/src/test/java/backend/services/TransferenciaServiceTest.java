package backend.services;

import backend.dto.InstrucaoTransferenciaResponse;
import backend.entities.Estado;
import backend.entities.InstrucaoTransferencia;
import backend.entities.ProcedimentoPadraoTransferencia;
import backend.entities.RegraEspecialEstado;
import backend.enums.TipoRegra;
import backend.exception.OrigemDestinoIguaisException;
import backend.exception.RegraTransferenciaNaoEncontradaException;
import backend.mapper.TransferenciaMapper;
import backend.repository.InstrucaoTransferenciaRepository;
import backend.repository.ProcedimentoPadraoTransferenciaRepository;
import backend.repository.RegraEspecialEstadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransferenciaServiceTest {

    private final InstrucaoTransferenciaRepository instrucaoRepository = mock(InstrucaoTransferenciaRepository.class);
    private final ProcedimentoPadraoTransferenciaRepository padraoRepository =
            mock(ProcedimentoPadraoTransferenciaRepository.class);
    private final RegraEspecialEstadoRepository regraRepository = mock(RegraEspecialEstadoRepository.class);
    private final EstadoService estadoService = mock(EstadoService.class);

    private final TransferenciaService service = new TransferenciaService(instrucaoRepository, padraoRepository,
            regraRepository, estadoService, new TransferenciaMapper());

    @BeforeEach
    void setUp() {
        when(estadoService.validarUf(anyString()))
                .thenAnswer(inv -> inv.getArgument(0, String.class).trim().toUpperCase(Locale.ROOT));
        when(instrucaoRepository.findByUfOrigemUfAndUfDestinoUf(anyString(), anyString())).thenReturn(Optional.empty());
    }

    @Test
    void usaInstrucaoEspecificaQuandoExiste() {
        // Mocks criados antes do when(): o Mockito nao aceita stubbing aninhado
        Estado sc = estado("SC");
        Estado sp = estado("SP");
        InstrucaoTransferencia instrucao = mock(InstrucaoTransferencia.class);
        when(instrucao.getUfOrigem()).thenReturn(sc);
        when(instrucao.getUfDestino()).thenReturn(sp);
        when(instrucao.getPassoAPasso()).thenReturn("passos SC->SP");
        when(instrucao.getTaxaEstimada()).thenReturn(new BigDecimal("150.00"));
        when(instrucaoRepository.findByUfOrigemUfAndUfDestinoUf("SC", "SP")).thenReturn(Optional.of(instrucao));

        InstrucaoTransferenciaResponse resposta = service.buscarInstrucao("sc", "sp");

        assertThat(resposta.procedimentoPadrao()).isFalse();
        assertThat(resposta.passoAPasso()).isEqualTo("passos SC->SP");
        assertThat(resposta.taxaEstimada()).isEqualByComparingTo("150.00");
        assertThat(resposta.regrasEspeciaisDestino()).isEmpty();
        verify(padraoRepository, never()).findFirstByOrderByIdAsc();
        verify(regraRepository, never()).findByEstadoUfAndAtivaTrueOrderByIdAsc(anyString());
    }

    @Test
    void semInstrucaoEspecificaUsaPadraoComRegrasDoDestino() {
        ProcedimentoPadraoTransferencia padrao = padrao();
        when(padraoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(padrao));
        RegraEspecialEstado regra = mock(RegraEspecialEstado.class);
        when(regra.getTipoRegra()).thenReturn(TipoRegra.VISTORIA);
        when(regra.getTitulo()).thenReturn("Vistoria feita por empresa credenciada");
        when(regraRepository.findByEstadoUfAndAtivaTrueOrderByIdAsc("SC")).thenReturn(List.of(regra));

        InstrucaoTransferenciaResponse resposta = service.buscarInstrucao("RJ", "SC");

        assertThat(resposta.procedimentoPadrao()).isTrue();
        assertThat(resposta.ufOrigem()).isEqualTo("RJ");
        assertThat(resposta.ufDestino()).isEqualTo("SC");
        assertThat(resposta.passoAPasso()).isEqualTo("passos padrao");
        assertThat(resposta.taxaEstimada()).isEqualByComparingTo("220.00");
        assertThat(resposta.prazoDias()).isEqualTo(30);
        assertThat(resposta.observacaoLegal()).isEqualTo("CTB art. 123");
        assertThat(resposta.regrasEspeciaisDestino()).singleElement()
                .satisfies(r -> {
                    assertThat(r.tipoRegra()).isEqualTo(TipoRegra.VISTORIA);
                    assertThat(r.titulo()).isEqualTo("Vistoria feita por empresa credenciada");
                });
    }

    @Test
    void destinoSemRegrasEspeciaisDevolveListaVazia() {
        ProcedimentoPadraoTransferencia padrao = padrao();
        when(padraoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(padrao));
        when(regraRepository.findByEstadoUfAndAtivaTrueOrderByIdAsc("AM")).thenReturn(List.of());

        assertThat(service.buscarInstrucao("RJ", "AM").regrasEspeciaisDestino()).isEmpty();
    }

    @Test
    void semInstrucaoNemPadraoLanca404() {
        when(padraoRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarInstrucao("RJ", "SC"))
                .isInstanceOf(RegraTransferenciaNaoEncontradaException.class);
    }

    @Test
    void origemIgualAoDestinoELancadoAntesDeConsultarOBanco() {
        assertThatThrownBy(() -> service.buscarInstrucao("sc", "SC"))
                .isInstanceOf(OrigemDestinoIguaisException.class);
        verify(instrucaoRepository, never()).findByUfOrigemUfAndUfDestinoUf(anyString(), anyString());
    }

    private static ProcedimentoPadraoTransferencia padrao() {
        ProcedimentoPadraoTransferencia padrao = mock(ProcedimentoPadraoTransferencia.class);
        when(padrao.getPassoAPasso()).thenReturn("passos padrao");
        when(padrao.getTaxaMediaEstimada()).thenReturn(new BigDecimal("220.00"));
        when(padrao.getPrazoMedioDias()).thenReturn(30);
        when(padrao.getObservacaoLegal()).thenReturn("CTB art. 123");
        return padrao;
    }

    private static Estado estado(String uf) {
        Estado estado = mock(Estado.class);
        when(estado.getUf()).thenReturn(uf);
        return estado;
    }
}
