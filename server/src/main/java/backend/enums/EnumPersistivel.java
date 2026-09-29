package backend.enums;

import java.util.Arrays;

/**
 * Enum gravado no banco por um valor textual (ex.: "Regular", "Em recurso", "F")
 * em vez do nome da constante Java.
 */
public interface EnumPersistivel {

    String getValor();

    /**
     * Encontra a constante pelo valor do banco (ou pelo nome da constante), sem diferenciar maiusculas.
     *
     * @throws IllegalArgumentException se o valor nao corresponder a nenhuma constante
     */
    static <E extends Enum<E> & EnumPersistivel> E fromValor(Class<E> tipo, String valor) {
        if (valor == null) {
            return null;
        }
        String normalizado = valor.trim();
        return Arrays.stream(tipo.getEnumConstants())
                .filter(e -> e.getValor().equalsIgnoreCase(normalizado) || e.name().equalsIgnoreCase(normalizado))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Valor '" + valor + "' desconhecido para " + tipo.getSimpleName()));
    }
}
