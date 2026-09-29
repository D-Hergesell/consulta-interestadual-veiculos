package backend.mapper;

import backend.dto.EstadoResponse;
import backend.entities.Estado;
import org.springframework.stereotype.Component;

@Component
public class EstadoMapper {

    public EstadoResponse toResponse(Estado estado) {
        return new EstadoResponse(
                estado.getUf(),
                estado.getNome(),
                estado.getSiglaOrgao(),
                estado.getUrlDetran(),
                estado.getTelefone(),
                estado.getWhatsapp());
    }
}
