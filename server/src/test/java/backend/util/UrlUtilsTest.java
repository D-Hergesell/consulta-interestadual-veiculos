package backend.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class UrlUtilsTest {

    @Test
    void mantemHttpsEHttpComHost() {
        assertThat(UrlUtils.httpOuNula("https://www.detran.sc.gov.br")).isEqualTo("https://www.detran.sc.gov.br");
        assertThat(UrlUtils.httpOuNula("https://www.detran.sp.gov.br/servicos?id=1#topo"))
                .isEqualTo("https://www.detran.sp.gov.br/servicos?id=1#topo");
        assertThat(UrlUtils.httpOuNula("http://detran.ac.gov.br")).isEqualTo("http://detran.ac.gov.br");
        assertThat(UrlUtils.httpOuNula("HTTPS://DETRAN.RS.GOV.BR")).isEqualTo("HTTPS://DETRAN.RS.GOV.BR");
    }

    @Test
    void removeEspacosDasPontas() {
        assertThat(UrlUtils.httpOuNula("  https://www.detran.sc.gov.br \n")).isEqualTo("https://www.detran.sc.gov.br");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "javascript:alert(1)",
            "JavaScript:alert(document.cookie)",
            " javascript:alert(1)",
            "data:text/html,<script>alert(1)</script>",
            "vbscript:msgbox(1)",
            "ftp://detran.sc.gov.br",
            "//evil.example.com",
            "/servicos/1",
            "www.detran.sc.gov.br",
            "https://",
            "https:///caminho",
            "https://detran sc.gov.br",
            "http:alert(1)"
    })
    void descartaTudoQueNaoForHttpAbsolutaComHost(String url) {
        assertThat(UrlUtils.httpOuNula(url)).isNull();
    }
}
