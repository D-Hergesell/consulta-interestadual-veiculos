package backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_instrucoes_transferencia",
        uniqueConstraints = @UniqueConstraint(columnNames = {"uf_origem", "uf_destino"}))
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InstrucaoTransferencia {

    @Id
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uf_origem")
    private Estado ufOrigem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uf_destino")
    private Estado ufDestino;

    @Column(name = "passo_a_passo", nullable = false, columnDefinition = "text")
    private String passoAPasso;

    @Column(name = "taxa_estimada", precision = 10, scale = 2)
    private BigDecimal taxaEstimada;

    @Column(name = "prazo_dias")
    private Integer prazoDias;

    @Column(name = "documentos_necessarios", nullable = false, columnDefinition = "text")
    private String documentosNecessarios;
}
