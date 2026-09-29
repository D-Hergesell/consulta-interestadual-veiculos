package backend.dto;

import backend.enums.StatusPagamento;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record MultaResponse(
        LocalDate dataInfracao,
        String codigoInfracao,
        String descricao,
        BigDecimal valor,
        Integer pontos,
        StatusPagamento statusPagamento
) {
}
