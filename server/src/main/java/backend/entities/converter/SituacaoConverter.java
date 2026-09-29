package backend.entities.converter;

import backend.enums.Situacao;
import jakarta.persistence.Converter;

// autoApply: aplicado automaticamente a todo atributo do tipo Situacao nas entidades
@Converter(autoApply = true)
public class SituacaoConverter extends EnumPersistivelConverter<Situacao> {

    public SituacaoConverter() {
        super(Situacao.class);
    }
}
