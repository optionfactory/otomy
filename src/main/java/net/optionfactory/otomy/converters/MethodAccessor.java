package net.optionfactory.otomy.converters;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.optionfactory.otomy.types.Typed;

/**
 *
 * @author rferranti
 */
public class MethodAccessor implements Accessor {

    private final Method method;

    public MethodAccessor(Method method) {
        this.method = method;
    }

    @Override
    public Object access(MappingContext mappingContext, Object source) {
        try {
            return method.invoke(source);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new MappingException(mappingContext, ex);
        }
    }

    @Override
    public String label() {
        final String methodName = method.getName();
        final int offset = methodName.charAt(0) == 'i' ? 2 : 3;
        return Character.toLowerCase(methodName.charAt(offset)) + methodName.substring(offset + 1);
    }

    @Override
    public Typed type(MappingContext mappingContext) {
        return Typed.returnType(method, mappingContext.source.type);
    }

}
