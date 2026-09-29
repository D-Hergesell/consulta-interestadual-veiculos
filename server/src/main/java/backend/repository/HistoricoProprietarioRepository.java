package backend.repository;

import backend.entities.HistoricoProprietario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoProprietarioRepository extends JpaRepository<HistoricoProprietario, Integer> {

    @EntityGraph(attributePaths = {"proprietario", "ufNaEpoca"})
    List<HistoricoProprietario> findByVeiculoIdOrderByDataInicioDesc(Integer veiculoId);
}
