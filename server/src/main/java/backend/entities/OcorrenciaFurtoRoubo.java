package backend.entities;

import backend.enums.TipoOcorrencia;
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
@Table(name = "tb_ocorrencias_furto_roubo")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OcorrenciaFurtoRoubo {

    @Id
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    @Column(name = "tipo_ocorrencia", nullable = false, length = 20)
    private TipoOcorrencia tipoOcorrencia;

    @Column(name = "data_ocorrencia", nullable = false)
    private LocalDate dataOcorrencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uf_ocorrencia")
    private Estado ufOcorrencia;

    @Column(name = "municipio_ocorrencia", length = 60)
    private String municipioOcorrencia;

    @Column(name = "boletim_ocorrencia", length = 30)
    private String boletimOcorrencia;

    /** Ex.: 'PC-SC', 'PM-SP'. */
    @Column(name = "orgao_registro", length = 60)
    private String orgaoRegistro;

    @Column(name = "observacoes", columnDefinition = "text")
    private String observacoes;

    /** true = ainda consta como furtado/roubado. */
    @Column(name = "ativo")
    private Boolean ativo;
}
