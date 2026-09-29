package backend.repository;

import backend.entities.Servico;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicoRepository extends JpaRepository<Servico, Integer> {

    @EntityGraph(attributePaths = "categoria")
    List<Servico> findAllByOrderByNomeAsc();
}
