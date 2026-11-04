package backend.mapper;

import backend.entities.CategoriaServico;
import backend.entities.Estado;
import backend.entities.Servico;
import backend.entities.ServicoEstado;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Garante que as URLs do banco passam pelo UrlUtils antes de sair na resposta. */
class UrlNosMappersTest {

    @Test
    void estadoComUrlJavascriptSaiSemUrl() {
        Estado estado = mock(Estado.class);
        when(estado.getUf()).thenReturn("SC");
        when(estado.getUrlDetran()).thenReturn("javascript:alert(1)");

        assertThat(new EstadoMapper().toResponse(estado).urlDetran()).isNull();
    }

    @Test
    void estadoComUrlHttpsSaiIgual() {
        Estado estado = mock(Estado.class);
        when(estado.getUrlDetran()).thenReturn("https://www.detran.sc.gov.br");

        assertThat(new EstadoMapper().toResponse(estado).urlDetran()).isEqualTo("https://www.detran.sc.gov.br");
    }

    @Test
    void servicoEstadoComUrlJavascriptSaiSemUrl() {
        // Mocks criados antes do when(): o Mockito nao aceita stubbing aninhado
        CategoriaServico categoria = mock(CategoriaServico.class);
        Servico servico = mock(Servico.class);
        when(servico.getCategoria()).thenReturn(categoria);
        Estado estado = mock(Estado.class);
        ServicoEstado servicoEstado = mock(ServicoEstado.class);
        when(servicoEstado.getServico()).thenReturn(servico);
        when(servicoEstado.getEstado()).thenReturn(estado);
        when(servicoEstado.getUrlServico()).thenReturn("JavaScript:fetch('//evil')");

        assertThat(new ServicoMapper().toResponse(servicoEstado).urlServico()).isNull();
    }
}
