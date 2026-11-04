package backend.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** tb_regras_especiais_estado.tipo_regra (valores do CHECK da coluna) */
@Getter
@RequiredArgsConstructor
public enum TipoRegra implements EnumPersistivel {
    VISTORIA("Vistoria"),
    ISENCAO_VISTORIA("Isencao_Vistoria"),
    PRAZO_DIFERENCIADO("Prazo_Diferenciado"),
    TAXA_DIFERENCIADA("Taxa_Diferenciada"),
    PROCESSO_DIGITAL_EXCLUSIVO("Processo_Digital_Exclusivo"),
    OUTRO("Outro");

    private final String valor;
}
