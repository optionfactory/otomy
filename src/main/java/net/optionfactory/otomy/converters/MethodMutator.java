package net.optionfactory.otomy.converters;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.optionfactory.otomy.types.Typed;

/**
 *
 * @author rferranti
 */
public class MethodMutator implements Mutator {

    private final Method method;

    public MethodMutator(Method method) {
        this.method = method;
    }

    @Override
    public void mutate(MappingContext mappingContext, Object target, Object value) {
        try {
            method.invoke(target, value);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new MappingException(mappingContext, ex);
        }
    }

    @Override
    public String label() {
        final String methodName = method.getName();
        return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
    }

    @Override
    public Typed type(MappingContext mappingContext) {
        return Typed.parameter(method, 0, mappingContext.target.type);
    }
}
