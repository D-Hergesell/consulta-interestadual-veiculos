package backend.repository;

import backend.entities.Multa;
import backend.enums.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MultaRepository extends JpaRepository<Multa, Integer> {

    List<Multa> findByVeiculoIdOrderByDataInfracaoDesc(Integer veiculoId);

    /** Tudo que nao esta pago ('Pendente' e 'Em recurso'). status_pagamento e nullable, por isso o IS NULL. */
    @Query("""
            select m from Multa m
            where m.veiculo.id = :veiculoId
              and (m.statusPagamento is null or m.statusPagamento <> :pago)
            order by m.dataInfracao desc
            """)
    List<Multa> findNaoPagas(@Param("veiculoId") Integer veiculoId, @Param("pago") StatusPagamento pago);
}
