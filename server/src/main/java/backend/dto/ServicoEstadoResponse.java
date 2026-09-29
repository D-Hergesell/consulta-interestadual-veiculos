package backend.dto;

import lombok.Builder;

import java.math.BigDecimal;

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
