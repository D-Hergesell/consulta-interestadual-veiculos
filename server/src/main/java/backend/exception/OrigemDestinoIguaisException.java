package backend.exception;

import org.springframework.http.HttpStatus;

public class OrigemDestinoIguaisException extends ApiException {

    public OrigemDestinoIguaisException(String uf) {
        super(HttpStatus.BAD_REQUEST,
                "Origem e destino são o mesmo estado (" + uf + "). Informe estados diferentes para a transferência.");
    }
}
