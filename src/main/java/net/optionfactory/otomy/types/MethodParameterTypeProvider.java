package net.optionfactory.otomy.types;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class MethodParameterTypeProvider implements TypeProvider {

    private final Method method;
    private final Typed containingClass;
    private final int index;

    public MethodParameterTypeProvider(Method method, Typed containingClass, int index) {
        this.method = method;
        this.containingClass = containingClass;
        this.index = index;
    }

    @Override
    public Type getType() {
        return method.getGenericParameterTypes()[index];
    }

    @Override
    public boolean equals(Object rhs) {
        if (this == rhs) {
            return true;
        }        
        if (rhs instanceof MethodParameterTypeProvider == false) {
            return false;
        }
        final MethodParameterTypeProvider other = (MethodParameterTypeProvider) rhs;
        return this.method.equals(other.method)
                && this.index == other.index
                && this.containingClass.equals(other.containingClass);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.method.hashCode();
        hash = 29 * hash + this.containingClass.hashCode();
        hash = 29 * hash + this.index;
        return hash;
    }

}
