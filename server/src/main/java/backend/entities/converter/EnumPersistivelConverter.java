package backend.entities.converter;

import backend.enums.EnumPersistivel;
import jakarta.persistence.AttributeConverter;

/**
 * Base dos conversores JPA: grava getValor() no banco e, na leitura, volta para a constante do enum.
 */
public abstract class EnumPersistivelConverter<E extends Enum<E> & EnumPersistivel>
        implements AttributeConverter<E, String> {

    private final Class<E> tipo;

    protected EnumPersistivelConverter(Class<E> tipo) {
        this.tipo = tipo;
    }

    @Override
    public String convertToDatabaseColumn(E atributo) {
        return atributo == null ? null : atributo.getValor();
    }

    @Override
    public E convertToEntityAttribute(String valor) {
        return EnumPersistivel.fromValor(tipo, valor);
    }
}
