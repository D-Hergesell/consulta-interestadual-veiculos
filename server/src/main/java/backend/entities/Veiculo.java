package backend.entities;

import backend.enums.Situacao;
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
@Table(name = "tb_veiculos")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Veiculo {

    @Id
    @Column(name = "id")
    private Integer id;

    /** Normalizada: maiuscula, sem hifen, 7 caracteres. */
    @Column(name = "placa", nullable = false, unique = true, length = 7)
    private String placa;

    @Column(name = "placa_anterior", length = 7)
    private String placaAnterior;

    @Column(name = "renavam", nullable = false, unique = true, length = 11)
    private String renavam;

    @Column(name = "chassi", nullable = false, unique = true, length = 17)
    private String chassi;

    @Column(name = "marca", nullable = false, length = 50)
    private String marca;

    @Column(name = "modelo", nullable = false, length = 50)
    private String modelo;

    @Column(name = "ano_fabricacao", nullable = false)
    private Integer anoFabricacao;

    @Column(name = "ano_modelo", nullable = false)
    private Integer anoModelo;

    @Column(name = "cor", nullable = false, length = 20)
    private String cor;

    /** Automovel, Motocicleta, Caminhao etc. */
    @Column(name = "tipo_veiculo", length = 30)
    private String tipoVeiculo;

    @Column(name = "combustivel", length = 30)
    private String combustivel;

    /** Particular, Aluguel, Oficial, Aprendizagem. */
    @Column(name = "categoria", length = 30)
    private String categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uf_registro")
    private Estado ufRegistro;

    @Column(name = "municipio_registro", length = 60)
    private String municipioRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietario_id")
    private Proprietario proprietario;

    @Column(name = "situacao", nullable = false, length = 30)
    private Situacao situacao;

    @Column(name = "data_registro")
    private LocalDate dataRegistro;
}
