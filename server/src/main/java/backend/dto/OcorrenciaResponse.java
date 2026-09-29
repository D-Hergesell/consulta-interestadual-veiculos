package backend.dto;

import backend.enums.TipoOcorrencia;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record OcorrenciaResponse(
        TipoOcorrencia tipoOcorrencia,
        LocalDate dataOcorrencia,
        String ufOcorrencia,
        String municipioOcorrencia,
        String boletimOcorrencia,
        String orgaoRegistro
) {
}
