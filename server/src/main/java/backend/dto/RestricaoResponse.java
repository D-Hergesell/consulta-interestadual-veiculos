package backend.dto;

import java.time.LocalDate;

public record RestricaoResponse(
        String tipoRestricao,
        String descricao,
        String orgaoResponsavel,
        LocalDate dataInclusao
) {
}
