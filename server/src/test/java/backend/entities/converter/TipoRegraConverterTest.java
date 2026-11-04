package backend.entities.converter;

import backend.enums.TipoRegra;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TipoRegraConverterTest {

    private final TipoRegraConverter converter = new TipoRegraConverter();

    @Test
    void leTodosOsValoresDoCheckDoBanco() {
        assertThat(converter.convertToEntityAttribute("Vistoria")).isEqualTo(TipoRegra.VISTORIA);
        assertThat(converter.convertToEntityAttribute("Isencao_Vistoria")).isEqualTo(TipoRegra.ISENCAO_VISTORIA);
        assertThat(converter.convertToEntityAttribute("Prazo_Diferenciado")).isEqualTo(TipoRegra.PRAZO_DIFERENCIADO);
        assertThat(converter.convertToEntityAttribute("Taxa_Diferenciada")).isEqualTo(TipoRegra.TAXA_DIFERENCIADA);
        assertThat(converter.convertToEntityAttribute("Processo_Digital_Exclusivo"))
                .isEqualTo(TipoRegra.PROCESSO_DIGITAL_EXCLUSIVO);
        assertThat(converter.convertToEntityAttribute("Outro")).isEqualTo(TipoRegra.OUTRO);
    }

    @Test
    void gravaOValorDoBancoENaoONomeDaConstante() {
        assertThat(converter.convertToDatabaseColumn(TipoRegra.ISENCAO_VISTORIA)).isEqualTo("Isencao_Vistoria");
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void valorForaDoCheckFalha() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("Inexistente"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
