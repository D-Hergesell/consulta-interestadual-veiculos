package backend.repository;

import backend.entities.InstrucaoTransferencia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstrucaoTransferenciaRepository extends JpaRepository<InstrucaoTransferencia, Integer> {

    @EntityGraph(attributePaths = {"ufOrigem", "ufDestino"})
    Optional<InstrucaoTransferencia> findByUfOrigemUfAndUfDestinoUf(String ufOrigem, String ufDestino);
}
