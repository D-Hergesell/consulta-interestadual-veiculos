package backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base das excecoes de negocio. A mensagem vai para o cliente, entao nunca deve conter dados internos.
 */
@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }
}
