package backend.dto;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * @param urlServico  sempre http/https absoluta ou null (validada em UrlUtils), segura para usar em href
 * @param observacoes texto puro: o front deve exibir como texto, nunca interpretar como HTML
 */
@Builder
public record ServicoEstadoResponse(
        ServicoResponse servico,
        String uf,
        BigDecimal taxaEstimada,
        Integer prazoDias,
        String urlServico,
        String observacoes
) {
}
