package backend.util;

import backend.exception.PlacaInvalidaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlacaUtilsTest {

    @Test
    void normalizaFormatoAntigo() {
        assertThat(PlacaUtils.normalizar(" abc-1234 ")).isEqualTo("ABC1234");
    }

    @Test
    void normalizaFormatoMercosul() {
        assertThat(PlacaUtils.normalizar("abc1d23")).isEqualTo("ABC1D23");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "ABC123", "ABC12345", "1BC1234", "ABCD123", "ABC1DD3", "ABC'1234"})
    void rejeitaPlacaInvalida(String placa) {
        assertThatThrownBy(() -> PlacaUtils.normalizar(placa)).isInstanceOf(PlacaInvalidaException.class);
    }
}
