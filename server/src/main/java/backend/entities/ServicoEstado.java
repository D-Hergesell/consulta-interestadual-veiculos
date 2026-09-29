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

/** Instancia de um servico generico em uma UF (taxa, prazo e link variam por estado). */
@Entity
@Table(name = "tb_servicos_estado",
        uniqueConstraints = @UniqueConstraint(columnNames = {"servico_id", "uf"}))
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServicoEstado {

    @Id
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servico_id")
    private Servico servico;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uf")
    private Estado estado;

    @Column(name = "taxa_estimada", precision = 10, scale = 2)
    private BigDecimal taxaEstimada;

    @Column(name = "prazo_dias")
    private Integer prazoDias;

    @Column(name = "url_servico", length = 200)
    private String urlServico;

    @Column(name = "observacoes", columnDefinition = "text")
    private String observacoes;
}
