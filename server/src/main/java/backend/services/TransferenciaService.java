package backend.services;

import backend.dto.InstrucaoTransferenciaResponse;
import backend.exception.RegraTransferenciaNaoEncontradaException;
import backend.mapper.TransferenciaMapper;
import backend.repository.InstrucaoTransferenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransferenciaService {

    private final InstrucaoTransferenciaRepository instrucaoRepository;
    private final EstadoService estadoService;
    private final TransferenciaMapper mapper;

    public InstrucaoTransferenciaResponse buscarInstrucao(String origem, String destino) {
        String ufOrigem = estadoService.validarUf(origem);
        String ufDestino = estadoService.validarUf(destino);

        // TODO: se existir tabela de regra padrao, usa-la aqui como fallback antes de lancar a excecao
        return instrucaoRepository.findByUfOrigemUfAndUfDestinoUf(ufOrigem, ufDestino)
                .map(mapper::toResponse)
                .orElseThrow(() -> new RegraTransferenciaNaoEncontradaException(ufOrigem, ufDestino));
    }
}
