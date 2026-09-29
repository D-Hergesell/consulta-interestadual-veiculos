package backend.dto;

public record ServicoResponse(
        Integer id,
        String nome,
        String descricao,
        String categoria,
        Boolean disponivelOnline
) {
}
