package backend.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RenavamUtilsTest {

    @Test
    void confereRenavamIgual() {
        assertThat(RenavamUtils.confere("12345678901", "12345678901")).isTrue();
    }

    @Test
    void ignoraPontuacaoEZerosAEsquerda() {
        assertThat(RenavamUtils.confere("123.456.789", "00123456789")).isTrue();
    }

    @Test
    void rejeitaRenavamDiferente() {
        assertThat(RenavamUtils.confere("12345678900", "12345678901")).isFalse();
    }

    @Test
    void rejeitaNuloVazioOuLongoDemais() {
        assertThat(RenavamUtils.confere(null, "12345678901")).isFalse();
        assertThat(RenavamUtils.confere("", "12345678901")).isFalse();
        assertThat(RenavamUtils.confere("abc", "12345678901")).isFalse();
        assertThat(RenavamUtils.confere("112345678901", "12345678901")).isFalse();
        assertThat(RenavamUtils.confere("12345678901", null)).isFalse();
    }
}
