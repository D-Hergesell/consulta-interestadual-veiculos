package backend.repository;

import backend.entities.ProcedimentoPadraoTransferencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcedimentoPadraoTransferenciaRepository extends JpaRepository<ProcedimentoPadraoTransferencia, Integer> {

    /** A tabela tem um unico registro ('Padrao Nacional'); se houver mais, vale o de menor id. */
    Optional<ProcedimentoPadraoTransferencia> findFirstByOrderByIdAsc();
}
