package backend.dto;

/**
 * @param urlDetran sempre http/https absoluta ou null (validada em UrlUtils), segura para usar em href
 */
public record EstadoResponse(
        String uf,
        String nome,
        String siglaOrgao,
        String urlDetran,
        String telefone,
        String whatsapp
) {
}
