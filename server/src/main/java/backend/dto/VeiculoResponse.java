package backend.dto;

import lombok.Builder;

/** Renavam e chassi ficam de fora propositalmente. */
@Builder
public record VeiculoResponse(
        String placa,
        String marca,
        String modelo,
        Integer anoFabricacao,
        Integer anoModelo,
        String cor,
        String tipoVeiculo,
        String combustivel,
        String categoria,
        String ufRegistro,
        String municipioRegistro
) {
}
