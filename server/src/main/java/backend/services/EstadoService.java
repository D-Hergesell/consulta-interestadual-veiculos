package backend.services;

import backend.dto.EstadoResponse;
import backend.exception.EstadoNaoEncontradoException;
import backend.mapper.EstadoMapper;
import backend.repository.EstadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EstadoService {

    private final EstadoRepository estadoRepository;
    private final EstadoMapper mapper;

    public List<EstadoResponse> listar() {
        return estadoRepository.findAllByOrderByNomeAsc().stream().map(mapper::toResponse).toList();
    }

    /**
     * Normaliza a UF para maiuscula e garante que ela existe.
     *
     * @throws EstadoNaoEncontradoException se a UF nao estiver cadastrada
     */
    public String validarUf(String uf) {
        String normalizada = uf.trim().toUpperCase(Locale.ROOT);
        if (!estadoRepository.existsById(normalizada)) {
            throw new EstadoNaoEncontradoException(normalizada);
        }
        return normalizada;
    }
}
