package backend.services;

import backend.dto.InstrucaoTransferenciaResponse;
import backend.exception.OrigemDestinoIguaisException;
import backend.exception.RegraTransferenciaNaoEncontradaException;
import backend.mapper.TransferenciaMapper;
import backend.repository.InstrucaoTransferenciaRepository;
import backend.repository.ProcedimentoPadraoTransferenciaRepository;
import backend.repository.RegraEspecialEstadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransferenciaService {

    private final InstrucaoTransferenciaRepository instrucaoRepository;
    private final ProcedimentoPadraoTransferenciaRepository procedimentoPadraoRepository;
    private final RegraEspecialEstadoRepository regraEspecialRepository;
    private final EstadoService estadoService;
    private final TransferenciaMapper mapper;

    /**
     * Instrucao especifica do par origem/destino. Se nao existir, cai no procedimento padrao nacional
     * acompanhado das regras especiais ativas da UF de destino.
     *
     * @throws OrigemDestinoIguaisException            se origem e destino forem a mesma UF
     * @throws RegraTransferenciaNaoEncontradaException se nao houver instrucao especifica nem procedimento padrao
     */
    public InstrucaoTransferenciaResponse buscarInstrucao(String origem, String destino) {
        String ufOrigem = estadoService.validarUf(origem);
        String ufDestino = estadoService.validarUf(destino);
        if (ufOrigem.equals(ufDestino)) {
            throw new OrigemDestinoIguaisException(ufOrigem);
        }

        return instrucaoRepository.findByUfOrigemUfAndUfDestinoUf(ufOrigem, ufDestino)
                .map(mapper::toResponse)
                .orElseGet(() -> procedimentoPadrao(ufOrigem, ufDestino));
    }

    private InstrucaoTransferenciaResponse procedimentoPadrao(String ufOrigem, String ufDestino) {
        return procedimentoPadraoRepository.findFirstByOrderByIdAsc()
                .map(padrao -> mapper.toResponse(padrao, ufOrigem, ufDestino,
                        regraEspecialRepository.findByEstadoUfAndAtivaTrueOrderByIdAsc(ufDestino)))
                .orElseThrow(() -> new RegraTransferenciaNaoEncontradaException(ufOrigem, ufDestino));
    }
}
