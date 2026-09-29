package backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "tb_estados")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Estado {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "uf", length = 2)
    private String uf;

    @Column(name = "nome", nullable = false, length = 50)
    private String nome;

    /** Ex.: 'Detran-SC'. */
    @Column(name = "sigla_orgao", length = 20)
    private String siglaOrgao;

    @Column(name = "url_detran", length = 150)
    private String urlDetran;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "whatsapp", length = 20)
    private String whatsapp;
}
