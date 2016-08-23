package net.optionfactory.otomy.types;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class FieldTypeProvider implements TypeProvider {

    private final Field field;

    public FieldTypeProvider(Field field) {
        this.field = field;
    }

    @Override
    public Type getType() {
        return this.field.getGenericType();
    }

    @Override
    public boolean equals(Object rhs) {
        if (this == rhs) {
            return true;
        }
        if (rhs instanceof FieldTypeProvider == false) {
            return false;
        }
        final FieldTypeProvider other = (FieldTypeProvider) rhs;
        return this.field.equals(other.field);
    }

    @Override
    public int hashCode() {
        return this.field.hashCode();
    }

}
