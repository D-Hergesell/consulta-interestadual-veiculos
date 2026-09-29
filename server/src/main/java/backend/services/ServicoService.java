package backend.services;

import backend.dto.ServicoEstadoResponse;
import backend.dto.ServicoResponse;
import backend.mapper.ServicoMapper;
import backend.repository.ServicoEstadoRepository;
import backend.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final ServicoEstadoRepository servicoEstadoRepository;
    private final EstadoService estadoService;
    private final ServicoMapper mapper;

    public List<ServicoResponse> listar() {
        return servicoRepository.findAllByOrderByNomeAsc().stream().map(mapper::toResponse).toList();
    }

    public List<ServicoEstadoResponse> listarPorEstado(String uf) {
        String ufValida = estadoService.validarUf(uf);
        return servicoEstadoRepository.findByEstadoUf(ufValida).stream().map(mapper::toResponse).toList();
    }
}
