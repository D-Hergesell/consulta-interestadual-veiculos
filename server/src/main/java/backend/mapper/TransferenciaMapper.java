package backend.mapper;

import backend.dto.InstrucaoTransferenciaResponse;
import backend.dto.RegraEspecialResponse;
import backend.entities.InstrucaoTransferencia;
import backend.entities.ProcedimentoPadraoTransferencia;
import backend.entities.RegraEspecialEstado;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransferenciaMapper {

    public InstrucaoTransferenciaResponse toResponse(InstrucaoTransferencia instrucao) {
        return InstrucaoTransferenciaResponse.builder()
                .ufOrigem(instrucao.getUfOrigem().getUf())
                .ufDestino(instrucao.getUfDestino().getUf())
                .procedimentoPadrao(false)
                .passoAPasso(instrucao.getPassoAPasso())
                .taxaEstimada(instrucao.getTaxaEstimada())
                .prazoDias(instrucao.getPrazoDias())
                .documentosNecessarios(instrucao.getDocumentosNecessarios())
                .regrasEspeciaisDestino(List.of())
                .build();
    }

    /** Procedimento padrao nacional aplicado ao par informado; taxa e prazo sao os valores medios. */
    public InstrucaoTransferenciaResponse toResponse(ProcedimentoPadraoTransferencia padrao, String ufOrigem,
                                                     String ufDestino, List<RegraEspecialEstado> regrasDestino) {
        return InstrucaoTransferenciaResponse.builder()
                .ufOrigem(ufOrigem)
                .ufDestino(ufDestino)
                .procedimentoPadrao(true)
                .passoAPasso(padrao.getPassoAPasso())
                .taxaEstimada(padrao.getTaxaMediaEstimada())
                .prazoDias(padrao.getPrazoMedioDias())
                .documentosNecessarios(padrao.getDocumentosNecessarios())
                .observacaoLegal(padrao.getObservacaoLegal())
                .regrasEspeciaisDestino(regrasDestino.stream().map(this::toResponse).toList())
                .build();
    }

    public RegraEspecialResponse toResponse(RegraEspecialEstado regra) {
        return new RegraEspecialResponse(
                regra.getTipoRegra(),
                regra.getTitulo(),
                regra.getDescricao(),
                regra.getFonteReferencia());
    }
}
