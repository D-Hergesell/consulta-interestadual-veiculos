package backend.repository;

import backend.entities.ServicoEstado;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicoEstadoRepository extends JpaRepository<ServicoEstado, Integer> {

    @EntityGraph(attributePaths = {"servico", "servico.categoria", "estado"})
    List<ServicoEstado> findByEstadoUf(String uf);
}
