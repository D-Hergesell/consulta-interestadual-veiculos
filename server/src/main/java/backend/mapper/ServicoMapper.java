package backend.mapper;

import backend.dto.ServicoEstadoResponse;
import backend.dto.ServicoResponse;
import backend.entities.Servico;
import backend.entities.ServicoEstado;
import backend.util.UrlUtils;
import org.springframework.stereotype.Component;

@Component
public class ServicoMapper {

    public ServicoResponse toResponse(Servico servico) {
        return new ServicoResponse(
                servico.getId(),
                servico.getNome(),
                servico.getDescricao(),
                servico.getCategoria().getNome(),
                servico.getDisponivelOnline());
    }

    public ServicoEstadoResponse toResponse(ServicoEstado servicoEstado) {
        return ServicoEstadoResponse.builder()
                .servico(toResponse(servicoEstado.getServico()))
                .uf(servicoEstado.getEstado().getUf())
                .taxaEstimada(servicoEstado.getTaxaEstimada())
                .prazoDias(servicoEstado.getPrazoDias())
                .urlServico(UrlUtils.httpOuNula(servicoEstado.getUrlServico()))
                .observacoes(servicoEstado.getObservacoes())
                .build();
    }
}
