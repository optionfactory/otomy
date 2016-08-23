package net.optionfactory.otomy.converters;

import java.lang.reflect.Field;
import net.optionfactory.otomy.types.Typed;

/**
 *
 * @author rferranti
 */
public class FieldAccessor implements Accessor {

    private final Field field;

    public FieldAccessor(Field field) {
        this.field = field;
    }

    @Override
    public Object access(MappingContext mappingContext, Object source) {
        try {
            return field.get(source);
        } catch (IllegalAccessException ex) {
            throw new MappingException(mappingContext, ex);
        }
    }

    @Override
    public String label() {
        return field.getName();
    }

    @Override
    public Typed type(MappingContext mappingContext) {
        return Typed.field(field, mappingContext.source.type);
    }

}
