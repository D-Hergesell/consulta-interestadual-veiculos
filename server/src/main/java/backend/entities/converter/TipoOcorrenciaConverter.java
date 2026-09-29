package backend.entities.converter;

import backend.enums.TipoOcorrencia;
import jakarta.persistence.Converter;

// autoApply: aplicado automaticamente a todo atributo do tipo TipoOcorrencia nas entidades
@Converter(autoApply = true)
public class TipoOcorrenciaConverter extends EnumPersistivelConverter<TipoOcorrencia> {

    public TipoOcorrenciaConverter() {
        super(TipoOcorrencia.class);
    }
}
