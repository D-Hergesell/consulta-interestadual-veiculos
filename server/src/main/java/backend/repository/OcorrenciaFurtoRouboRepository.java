package backend.repository;

import backend.entities.OcorrenciaFurtoRoubo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OcorrenciaFurtoRouboRepository extends JpaRepository<OcorrenciaFurtoRoubo, Integer> {

    @EntityGraph(attributePaths = "ufOcorrencia")
    List<OcorrenciaFurtoRoubo> findByVeiculoIdAndAtivoTrueOrderByDataOcorrenciaDesc(Integer veiculoId);
}
