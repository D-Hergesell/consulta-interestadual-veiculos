package backend.dto;

import backend.enums.Situacao;
import lombok.Builder;

/**
 * Resposta publica de GET /api/veiculos/{placa}: so o que interessa a quem vai comprar o veiculo.
 * Multas, numero do BO e descricao das restricoes ficam em ConsultaDetalhadaResponse, que exige RENAVAM.
 *
 * @param constaFurtoRoubo     calculado a partir das ocorrencias ativas, e nao do campo situacao
 * @param possuiRestricaoAtiva existe ao menos uma restricao ativa (sem detalhar qual)
 *
 * Nao inclui dados pessoais do proprietario (nome, CPF/CNPJ) por causa da LGPD.
 */
@Builder
public record ConsultaPlacaResponse(
        VeiculoResponse veiculo,
        Situacao situacao,
        boolean constaFurtoRoubo,
        boolean possuiRestricaoAtiva
) {
}
