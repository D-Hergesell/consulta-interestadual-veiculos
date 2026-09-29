package backend.entities.converter;

import backend.enums.TipoPessoa;
import jakarta.persistence.Converter;

// autoApply: aplicado automaticamente a todo atributo do tipo TipoPessoa nas entidades
@Converter(autoApply = true)
public class TipoPessoaConverter extends EnumPersistivelConverter<TipoPessoa> {

    public TipoPessoaConverter() {
        super(TipoPessoa.class);
    }
}
