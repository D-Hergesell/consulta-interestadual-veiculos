package backend.dto;

import backend.enums.Situacao;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resposta de GET /api/veiculos/{placa}.
 *
 * @param constaFurtoRoubo calculado a partir das ocorrencias ativas, e nao do campo situacao
 * @param multasPendentes  multas nao pagas ('Pendente' e 'Em recurso')
 */
@Builder
public record ConsultaPlacaResponse(
        VeiculoResponse veiculo,
        Situacao situacao,
        boolean constaFurtoRoubo,
        List<OcorrenciaResponse> ocorrenciasAtivas,
        List<RestricaoResponse> restricoesAtivas,
        List<MultaResponse> multasPendentes,
        BigDecimal totalMultasPendentes,
        ProprietarioResponse proprietarioAtual
) {
}
