package backend.services;

import backend.dto.ConsultaPlacaResponse;
import backend.dto.MultaResponse;
import backend.entities.Multa;
import backend.entities.OcorrenciaFurtoRoubo;
import backend.entities.Veiculo;
import backend.enums.Situacao;
import backend.enums.StatusPagamento;
import backend.enums.TipoOcorrencia;
import backend.exception.PlacaNaoEncontradaException;
import backend.mapper.VeiculoMapper;
import backend.repository.MultaRepository;
import backend.repository.OcorrenciaFurtoRouboRepository;
import backend.repository.RestricaoRepository;
import backend.repository.VeiculoRepository;
import backend.util.PlacaUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final OcorrenciaFurtoRouboRepository ocorrenciaRepository;
    private final RestricaoRepository restricaoRepository;
    private final MultaRepository multaRepository;
    private final VeiculoMapper mapper;

    public ConsultaPlacaResponse consultarPorPlaca(String placaInformada) {
        Veiculo veiculo = buscarVeiculo(placaInformada);
        Integer id = veiculo.getId();

        List<OcorrenciaFurtoRoubo> ocorrenciasAtivas =
                ocorrenciaRepository.findByVeiculoIdAndAtivoTrueOrderByDataOcorrenciaDesc(id);
        List<Multa> multasPendentes = multaRepository.findNaoPagas(id, StatusPagamento.PAGO);

        boolean constaFurtoRoubo = constaFurtoRoubo(ocorrenciasAtivas);
        registrarDivergencia(veiculo, constaFurtoRoubo);

        return ConsultaPlacaResponse.builder()
                .veiculo(mapper.toResponse(veiculo))
                .situacao(veiculo.getSituacao())
                .constaFurtoRoubo(constaFurtoRoubo)
                .ocorrenciasAtivas(ocorrenciasAtivas.stream().map(mapper::toResponse).toList())
                .restricoesAtivas(restricaoRepository.findByVeiculoIdAndAtivaTrueOrderByDataInclusaoDesc(id)
                        .stream().map(mapper::toResponse).toList())
                .multasPendentes(multasPendentes.stream().map(mapper::toResponse).toList())
                .totalMultasPendentes(multasPendentes.stream()
                        .map(Multa::getValor)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .proprietarioAtual(mapper.toResponse(veiculo.getProprietario()))
                .build();
    }

    public List<MultaResponse> listarMultas(String placaInformada) {
        Veiculo veiculo = buscarVeiculo(placaInformada);
        return multaRepository.findByVeiculoIdOrderByDataInfracaoDesc(veiculo.getId())
                .stream().map(mapper::toResponse).toList();
    }

    private Veiculo buscarVeiculo(String placaInformada) {
        String placa = PlacaUtils.normalizar(placaInformada);
        return veiculoRepository.findByPlaca(placa)
                .orElseThrow(() -> new PlacaNaoEncontradaException(placa));
    }

    /** Ocorrencia ativa de furto ou roubo. Recuperacao ativa nao conta. */
    private static boolean constaFurtoRoubo(List<OcorrenciaFurtoRoubo> ocorrenciasAtivas) {
        return ocorrenciasAtivas.stream().anyMatch(o -> o.getTipoOcorrencia() != TipoOcorrencia.RECUPERACAO);
    }

    /** situacao e tb_ocorrencias_furto_roubo podem divergir; a resposta segue as ocorrencias. */
    private static void registrarDivergencia(Veiculo veiculo, boolean constaFurtoRoubo) {
        boolean situacaoIndicaFurto = veiculo.getSituacao() == Situacao.FURTADO
                || veiculo.getSituacao() == Situacao.ROUBADO;
        if (situacaoIndicaFurto != constaFurtoRoubo) {
            log.warn("Divergência entre situação ({}) e ocorrências ativas do veículo id={}",
                    veiculo.getSituacao(), veiculo.getId());
        }
    }
}
