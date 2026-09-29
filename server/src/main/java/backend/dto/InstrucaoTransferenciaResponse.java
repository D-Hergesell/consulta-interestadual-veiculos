package backend.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record InstrucaoTransferenciaResponse(
        String ufOrigem,
        String ufDestino,
        String passoAPasso,
        BigDecimal taxaEstimada,
        Integer prazoDias,
        String documentosNecessarios
) {
}
