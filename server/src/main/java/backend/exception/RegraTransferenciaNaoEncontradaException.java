package backend.exception;

import org.springframework.http.HttpStatus;

public class RegraTransferenciaNaoEncontradaException extends ApiException {

    public RegraTransferenciaNaoEncontradaException(String ufOrigem, String ufDestino) {
        super(HttpStatus.NOT_FOUND,
                "Nenhuma regra de transferência cadastrada de " + ufOrigem + " para " + ufDestino + ".");
    }
}
