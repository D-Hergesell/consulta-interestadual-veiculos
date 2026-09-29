package backend.util;

import backend.exception.PlacaInvalidaException;
import lombok.experimental.UtilityClass;

import java.util.Locale;
import java.util.regex.Pattern;

@UtilityClass
public class PlacaUtils {

    /** Formato antigo: ABC1234. */
    private static final Pattern FORMATO_ANTIGO = Pattern.compile("^[A-Z]{3}[0-9]{4}$");
    /** Formato Mercosul: ABC1D23. */
    private static final Pattern FORMATO_MERCOSUL = Pattern.compile("^[A-Z]{3}[0-9][A-Z][0-9]{2}$");

    /**
     * Normaliza a placa (maiuscula, sem hifen/espacos) e valida os formatos antigo e Mercosul.
     *
     * @throws PlacaInvalidaException se a placa nao estiver em nenhum dos formatos
     */
    public String normalizar(String placa) {
        if (placa == null) {
            throw new PlacaInvalidaException();
        }
        String normalizada = placa.trim()
                .replace("-", "")
                .replace(" ", "")
                .toUpperCase(Locale.ROOT);

        if (normalizada.length() != 7
                || !(FORMATO_ANTIGO.matcher(normalizada).matches() || FORMATO_MERCOSUL.matcher(normalizada).matches())) {
            throw new PlacaInvalidaException();
        }
        return normalizada;
    }
}
