package backend.dto;

import backend.enums.TipoRegra;

/**
 * @param descricao       texto puro: o front deve exibir como texto, nunca interpretar como HTML
 * @param fonteReferencia nome da fonte (ex.: 'Portal do Transito'), nao e URL
 */
public record RegraEspecialResponse(
        TipoRegra tipoRegra,
        String titulo,
        String descricao,
        String fonteReferencia
) {
}
