package backend.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** tb_multas.status_pagamento */
@Getter
@RequiredArgsConstructor
public enum StatusPagamento implements EnumPersistivel {
    PENDENTE("Pendente"),
    PAGO("Pago"),
    EM_RECURSO("Em recurso");

    private final String valor;
}
