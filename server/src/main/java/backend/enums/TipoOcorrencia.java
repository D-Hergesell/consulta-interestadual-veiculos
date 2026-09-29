package backend.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** tb_ocorrencias_furto_roubo.tipo_ocorrencia */
@Getter
@RequiredArgsConstructor
public enum TipoOcorrencia implements EnumPersistivel {
    FURTO("Furto"),
    ROUBO("Roubo"),
    RECUPERACAO("Recuperacao");

    private final String valor;
}
