package backend.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** tb_veiculos.situacao */
@Getter
@RequiredArgsConstructor
public enum Situacao implements EnumPersistivel {
    REGULAR("Regular"),
    BLOQUEADO("Bloqueado"),
    SUCATA("Sucata"),
    FURTADO("Furtado"),
    ROUBADO("Roubado"),
    APREENDIDO("Apreendido");

    private final String valor;
}
