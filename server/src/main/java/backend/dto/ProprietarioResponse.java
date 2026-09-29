package backend.dto;

import backend.enums.TipoPessoa;

/**
 * @param documento CPF/CNPJ sempre mascarado
 */
public record ProprietarioResponse(
        String nome,
        String documento,
        TipoPessoa tipoPessoa
) {
}
