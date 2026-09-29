package backend.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** tb_proprietarios.tipo_pessoa */
@Getter
@RequiredArgsConstructor
public enum TipoPessoa implements EnumPersistivel {
    FISICA("F"),
    JURIDICA("J");

    private final String valor;
}
