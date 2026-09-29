package backend.exception;

import org.springframework.http.HttpStatus;

public class PlacaNaoEncontradaException extends ApiException {

    public PlacaNaoEncontradaException(String placa) {
        super(HttpStatus.NOT_FOUND, "Nenhum veículo encontrado para a placa " + placa + ".");
    }
}
