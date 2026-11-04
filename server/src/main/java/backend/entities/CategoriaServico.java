package backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "tb_categoria_servico")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoriaServico {

    @Id
    @Column(name = "id")
    private Integer id;

    /** Valores no banco: 'Veiculos', 'Infracoes', 'Furto/Roubo' (gravados com acento). */
    @Column(name = "nome", nullable = false, unique = true, length = 50)
    private String nome;
}
