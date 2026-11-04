package backend.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resposta de GET /api/transferencias. passoAPasso, documentosNecessarios e observacaoLegal sao texto puro
 * (com quebras de linha): o front deve exibir como texto, nunca interpretar como HTML.
 *
 * @param procedimentoPadrao     false: instrucao especifica do par origem/destino (tb_instrucoes_transferencia);
 *                               true: nao ha instrucao especifica e vale o procedimento padrao nacional
 *                               (tb_procedimento_padrao_transferencia), com taxa e prazo medios
 * @param observacaoLegal        so no procedimento padrao
 * @param regrasEspeciaisDestino regras ativas da UF de destino; preenchido so no procedimento padrao,
 *                               lista vazia na instrucao especifica
 */
@Builder
public record InstrucaoTransferenciaResponse(
        String ufOrigem,
        String ufDestino,
        boolean procedimentoPadrao,
        String passoAPasso,
        BigDecimal taxaEstimada,
        Integer prazoDias,
        String documentosNecessarios,
        String observacaoLegal,
        List<RegraEspecialResponse> regrasEspeciaisDestino
) {
}
