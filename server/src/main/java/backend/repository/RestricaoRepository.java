package backend.repository;

import backend.entities.Restricao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestricaoRepository extends JpaRepository<Restricao, Integer> {

    List<Restricao> findByVeiculoIdAndAtivaTrueOrderByDataInclusaoDesc(Integer veiculoId);
}
