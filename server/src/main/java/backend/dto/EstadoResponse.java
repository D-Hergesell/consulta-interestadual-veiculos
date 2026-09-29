package backend.dto;

public record EstadoResponse(
        String uf,
        String nome,
        String siglaOrgao,
        String urlDetran,
        String telefone,
        String whatsapp
) {
}
