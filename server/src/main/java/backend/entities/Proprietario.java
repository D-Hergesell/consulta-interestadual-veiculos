package backend.entities;

import backend.enums.TipoPessoa;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "tb_proprietarios")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Proprietario {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    /** Pode vir com ou sem pontuacao. Dado pessoal: nao deve ser exposto pela API publica. */
    @Column(name = "cpf_cnpj", nullable = false, unique = true, length = 18)
    private String cpfCnpj;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "tipo_pessoa", nullable = false, length = 1)
    private TipoPessoa tipoPessoa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uf_residencia")
    private Estado ufResidencia;
}
