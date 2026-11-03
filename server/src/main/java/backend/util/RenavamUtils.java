package backend.util;

import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@UtilityClass
public class RenavamUtils {

    private static final int TAMANHO = 11;

    /**
     * Confere o RENAVAM informado com o cadastrado. Ignora pontuacao e completa com zeros a esquerda
     * (RENAVAM antigo tem 9 digitos). A comparacao e em tempo constante para nao vazar quantos digitos batem.
     *
     * @return false se algum dos dois for nulo, vazio ou tiver mais de 11 digitos
     */
    public boolean confere(String informado, String cadastrado) {
        String a = normalizar(informado);
        String b = normalizar(cadastrado);
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.US_ASCII), b.getBytes(StandardCharsets.US_ASCII));
    }

    private static String normalizar(String renavam) {
        if (renavam == null) {
            return null;
        }
        String digitos = renavam.replaceAll("\\D", "");
        if (digitos.isEmpty() || digitos.length() > TAMANHO) {
            return null;
        }
        return "0".repeat(TAMANHO - digitos.length()) + digitos;
    }
}
