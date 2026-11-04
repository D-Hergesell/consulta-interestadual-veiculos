package backend.entities.converter;

import backend.enums.TipoRegra;
import jakarta.persistence.Converter;

// autoApply: aplicado automaticamente a todo atributo do tipo TipoRegra nas entidades
@Converter(autoApply = true)
public class TipoRegraConverter extends EnumPersistivelConverter<TipoRegra> {

    public TipoRegraConverter() {
        super(TipoRegra.class);
    }
}
