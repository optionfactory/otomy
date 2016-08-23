package net.optionfactory.otomy.converters.strategies;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import net.optionfactory.otomy.converters.Accessor;
import net.optionfactory.otomy.converters.Conversion;
import net.optionfactory.otomy.converters.Converter;
import net.optionfactory.otomy.converters.Mapping;
import net.optionfactory.otomy.converters.MappingContext;
import net.optionfactory.otomy.converters.MappingException;
import net.optionfactory.otomy.converters.Mutator;
import net.optionfactory.otomy.types.Typed;

/**
 *
 * @author rferranti
 */
public class Beans implements Converter {

    @Override
    public Conversion<?> convert(MappingContext ctx, Object source) {
        final Optional<Constructor> maybeConstructor = ctx.target.type.constructor();
        if (!maybeConstructor.isPresent()) {
            return Conversion.no();
        }
        final Object target = makeTargetBean(ctx, maybeConstructor);
        for (Mapping mapping : ctx.inspector.mappings(ctx.source.type, ctx.target.type)) {
            final Accessor accessor = mapping.accessor;
            final Mutator mutator = mapping.mutator;
            final Typed accessorType = accessor.type(ctx);
            final String accessorLabel = accessor.label();
            final Typed mutatorType = mutator.type(ctx);
            final String mutatorLabel = mutator.label();
            final MappingContext depCtx = ctx.dependent(accessorType, accessorLabel, mutatorType, mutatorLabel);
            final Conversion<?> converted = ctx.converter.convert(depCtx, accessor.access(depCtx, source));
            if (converted.valid) {
                mutator.mutate(depCtx, target, converted.value);
            }
        }
        return Conversion.of(target);
    }

    private static Object makeTargetBean(MappingContext ctx, Optional<Constructor> maybeConstructor) {
        try {
            return maybeConstructor.get().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new MappingException(ctx, ex);
        }
    }

}
