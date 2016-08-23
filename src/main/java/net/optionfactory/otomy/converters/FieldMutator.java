package net.optionfactory.otomy.converters;

import java.lang.reflect.Field;
import net.optionfactory.otomy.types.Typed;

/**
 *
 * @author rferranti
 */
public class FieldMutator implements Mutator {

    private final Field field;

    public FieldMutator(Field field) {
        this.field = field;
    }

    @Override
    public void mutate(MappingContext mappingContext, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException|IllegalArgumentException ex) {
            throw new MappingException(mappingContext, ex);            
        }
    }

    @Override
    public String label() {
        return field.getName();
    }

    @Override
    public Typed type(MappingContext mappingContext) {
        return Typed.field(field, mappingContext.target.type);
    }

}
