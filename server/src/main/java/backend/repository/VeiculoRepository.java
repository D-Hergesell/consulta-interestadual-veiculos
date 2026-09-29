package backend.repository;

import backend.entities.Veiculo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Integer> {

    @EntityGraph(attributePaths = {"ufRegistro", "proprietario"})
    Optional<Veiculo> findByPlaca(String placa);

    boolean existsByPlaca(String placa);
}
