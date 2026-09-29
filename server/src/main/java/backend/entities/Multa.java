package backend.entities;

import backend.enums.StatusPagamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_multas")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Multa {

    @Id
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    @Column(name = "data_infracao", nullable = false)
    private LocalDate dataInfracao;

    @Column(name = "codigo_infracao", length = 10)
    private String codigoInfracao;

    @Column(name = "descricao", length = 150)
    private String descricao;

    @Column(name = "valor", precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "pontos")
    private Integer pontos;

    @Column(name = "status_pagamento", length = 20)
    private StatusPagamento statusPagamento;
}
