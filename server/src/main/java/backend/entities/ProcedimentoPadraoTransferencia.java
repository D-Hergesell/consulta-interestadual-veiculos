package backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/**
 * Procedimento padrao de transferencia entre UFs, usado quando nao existe instrucao especifica
 * em tb_instrucoes_transferencia para o par origem/destino.
 */
@Entity
@Table(name = "tb_procedimento_padrao_transferencia")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcedimentoPadraoTransferencia {

    @Id
    @Column(name = "id")
    private Integer id;

    /** Valor no banco: 'Padrao Nacional' (gravado com acento; tambem e o padrao da coluna). */
    @Column(name = "tipo", nullable = false, length = 40)
    private String tipo;

    @Column(name = "passo_a_passo", nullable = false, columnDefinition = "text")
    private String passoAPasso;

    @Column(name = "documentos_necessarios", nullable = false, columnDefinition = "text")
    private String documentosNecessarios;

    @Column(name = "taxa_media_estimada", precision = 10, scale = 2)
    private BigDecimal taxaMediaEstimada;

    @Column(name = "prazo_medio_dias")
    private Integer prazoMedioDias;

    @Column(name = "observacao_legal", columnDefinition = "text")
    private String observacaoLegal;
}
