package backend.entities.converter;

import backend.enums.StatusPagamento;
import jakarta.persistence.Converter;

// autoApply: aplicado automaticamente a todo atributo do tipo StatusPagamento nas entidades
@Converter(autoApply = true)
public class StatusPagamentoConverter extends EnumPersistivelConverter<StatusPagamento> {

    public StatusPagamentoConverter() {
        super(StatusPagamento.class);
    }
}
