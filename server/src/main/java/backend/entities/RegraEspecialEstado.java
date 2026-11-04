package backend.entities;

import backend.enums.TipoRegra;
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

/** Regra de transferencia propria de uma UF (ex.: vistoria por empresa credenciada em SC). */
@Entity
@Table(name = "tb_regras_especiais_estado")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegraEspecialEstado {

    @Id
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uf")
    private Estado estado;

    @Column(name = "tipo_regra", nullable = false, length = 40)
    private TipoRegra tipoRegra;

    @Column(name = "titulo", nullable = false, length = 120)
    private String titulo;

    @Column(name = "descricao", nullable = false, columnDefinition = "text")
    private String descricao;

    @Column(name = "fonte_referencia", length = 200)
    private String fonteReferencia;

    /** Padrao da coluna: true. */
    @Column(name = "ativa")
    private Boolean ativa;
}
