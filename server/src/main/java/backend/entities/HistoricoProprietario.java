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
@Table(name = "tb_historico_proprietarios")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistoricoProprietario {

    @Id
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proprietario_id")
    private Proprietario proprietario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uf_na_epoca")
    private Estado ufNaEpoca;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    /** null = proprietario atual. */
    @Column(name = "data_fim")
    private LocalDate dataFim;
}
