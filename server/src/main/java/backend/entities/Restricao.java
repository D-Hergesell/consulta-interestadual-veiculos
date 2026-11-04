package backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;

@Entity
@Table(name = "tb_restricoes")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Restricao {

    @Id
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    /** Valores no banco: 'Alienacao Fiduciaria', 'Judicial', 'Administrativa', 'Tributaria' (gravados com acento). */
    @Column(name = "tipo_restricao", nullable = false, length = 40)
    private String tipoRestricao;

    @Column(name = "descricao", columnDefinition = "text")
    private String descricao;

    @Column(name = "orgao_responsavel", length = 60)
    private String orgaoResponsavel;

    @Column(name = "data_inclusao")
    private LocalDate dataInclusao;

    @Column(name = "ativa")
    private Boolean ativa;
}
