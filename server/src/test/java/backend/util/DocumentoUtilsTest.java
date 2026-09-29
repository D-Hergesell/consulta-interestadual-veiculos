package backend.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentoUtilsTest {

    @Test
    void mascaraCpf() {
        assertThat(DocumentoUtils.mascarar("123.456.789-01")).isEqualTo("***.456.789-**");
    }

    @Test
    void mascaraCnpj() {
        assertThat(DocumentoUtils.mascarar("12345678000195")).isEqualTo("**.345.678/0001-**");
    }

    @Test
    void mascaraTudoQuandoFormatoDesconhecido() {
        assertThat(DocumentoUtils.mascarar("12345")).isEqualTo("*****");
    }
}
