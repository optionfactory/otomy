package net.optionfactory.otomy.types;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class MethodReturnTypeProvider implements TypeProvider {

    private final Method method;
    private final Typed containingClass;

    public MethodReturnTypeProvider(Method method, Typed containingClass) {
        this.method = method;
        this.containingClass = containingClass;
    }

    @Override
    public Type getType() {
        return method.getGenericReturnType();
    }

    @Override
    public boolean equals(Object rhs) {
        if (this == rhs) {
            return true;
        }        
        if (rhs instanceof MethodReturnTypeProvider == false) {
            return false;
        }
        final MethodReturnTypeProvider other = (MethodReturnTypeProvider) rhs;
        return this.method.equals(other.method) && this.containingClass.equals(other.containingClass);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.method.hashCode();
        hash = 89 * hash + this.containingClass.hashCode();
        return hash;
    }


}
