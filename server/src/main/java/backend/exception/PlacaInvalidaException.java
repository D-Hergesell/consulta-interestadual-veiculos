package backend.exception;

import org.springframework.http.HttpStatus;

public class PlacaInvalidaException extends ApiException {

    public PlacaInvalidaException() {
        super(HttpStatus.BAD_REQUEST, "Placa inválida. Use o formato antigo (ABC1234) ou Mercosul (ABC1D23).");
    }
}
