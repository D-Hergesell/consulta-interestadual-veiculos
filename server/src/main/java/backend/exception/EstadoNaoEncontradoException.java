package backend.exception;

import org.springframework.http.HttpStatus;

public class EstadoNaoEncontradoException extends ApiException {

    public EstadoNaoEncontradoException(String uf) {
        super(HttpStatus.NOT_FOUND, "Estado " + uf + " não encontrado.");
    }
}
