package backend.mapper;

import backend.dto.InstrucaoTransferenciaResponse;
import backend.entities.InstrucaoTransferencia;
import org.springframework.stereotype.Component;

@Component
public class TransferenciaMapper {

    public InstrucaoTransferenciaResponse toResponse(InstrucaoTransferencia instrucao) {
        return InstrucaoTransferenciaResponse.builder()
                .ufOrigem(instrucao.getUfOrigem().getUf())
                .ufDestino(instrucao.getUfDestino().getUf())
                .passoAPasso(instrucao.getPassoAPasso())
                .taxaEstimada(instrucao.getTaxaEstimada())
                .prazoDias(instrucao.getPrazoDias())
                .documentosNecessarios(instrucao.getDocumentosNecessarios())
                .build();
    }
}
