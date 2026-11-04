package backend.dto;

import backend.enums.TipoRegra;

public record RegraEspecialResponse(
        TipoRegra tipoRegra,
        String titulo,
        String descricao,
        String fonteReferencia
) {
}
