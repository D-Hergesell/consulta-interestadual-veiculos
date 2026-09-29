package backend.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DocumentoUtils {

    /**
     * Mascara CPF/CNPJ para exibicao, mantendo so os digitos do meio.
     * CPF:  12345678901    -> ***.456.789-**
     * CNPJ: 12345678000195 -> **.345.678/0001-**
     * Qualquer outro formato e totalmente mascarado.
     */
    public String mascarar(String documento) {
        if (documento == null || documento.isBlank()) {
            return null;
        }
        String digitos = documento.replaceAll("\\D", "");

        if (digitos.length() == 11) {
            return "***." + digitos.substring(3, 6) + "." + digitos.substring(6, 9) + "-**";
        }
        if (digitos.length() == 14) {
            return "**." + digitos.substring(2, 5) + "." + digitos.substring(5, 8)
                    + "/" + digitos.substring(8, 12) + "-**";
        }
        return "*".repeat(digitos.length());
    }
}
