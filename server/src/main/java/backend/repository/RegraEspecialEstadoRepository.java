package backend.repository;

import backend.entities.RegraEspecialEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegraEspecialEstadoRepository extends JpaRepository<RegraEspecialEstado, Integer> {

    List<RegraEspecialEstado> findByEstadoUfAndAtivaTrueOrderByIdAsc(String uf);
}
