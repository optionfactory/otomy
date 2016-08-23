package net.optionfactory.otomy.converters.strategies;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import net.optionfactory.otomy.converters.Conversion;
import net.optionfactory.otomy.converters.Converter;
import net.optionfactory.otomy.converters.MappingContext;
import net.optionfactory.otomy.converters.factories.CollectionFactory;
import net.optionfactory.otomy.types.Typed;

/**
 *
 * @author rferranti
 */
public class Iterables implements Converter {

    private final CollectionFactory factory;

    public Iterables(CollectionFactory factory) {
        this.factory = factory;
    }

    @Override
    public Conversion<?> convert(MappingContext ctx, Object source) {
        final Class<?> sourceClass = ctx.source.type.resolve();
        final Class<?> targetClass = ctx.target.type.resolve();
        if (Map.class.isAssignableFrom(targetClass) && Map.class.isAssignableFrom(sourceClass)) {
            final Optional<Map<Object, Object>> maybeResult = (Optional) factory.map(ctx);
            if (!maybeResult.isPresent()) {
                return Conversion.no();
            }
            final Map<Object, Object> result = maybeResult.get();
            long i = 0;
            for (Map.Entry<Object, Object> sourceEntry : ((Map<Object, Object>) source).entrySet()) {
                final String idx = Long.toString(i);
                final MappingContext dependentKey = ctx.dependent(ctx.source.type.getGeneric(0), ctx.target.type.getGeneric(0), "entries", idx, "key");
                final Conversion<?> k = ctx.converter.convert(dependentKey, sourceEntry.getKey());
                final MappingContext dependentValue = ctx.dependent(ctx.source.type.getGeneric(1), ctx.target.type.getGeneric(1), "entries", idx, "value");
                final Conversion<?> v = ctx.converter.convert(dependentValue, sourceEntry.getValue());
                if (!k.valid || !v.valid) {
                    return Conversion.no();
                }
                result.put(k.value, v.value);
                ++i;
            }
            return Conversion.of(result);
        }
        if (!Iterable.class.isAssignableFrom(sourceClass) || !Iterable.class.isAssignableFrom(targetClass)) {
            return Conversion.no();
        }
        final Optional<Collection<Object>> maybeCollection = (Optional) factory.collection(ctx);
        if (!maybeCollection.isPresent()) {
            return Conversion.no();
        }
        final Collection<Object> collection = maybeCollection.get();
        final Typed targetElementType = ctx.target.type.getGeneric(0);
        final Typed sourceElementType = ctx.source.type.getGeneric(0);
        long i = 0;
        for (Object sourceElement : (Iterable<?>) source) {
            final String idx = Long.toString(i);
            final MappingContext dependent = ctx.dependent(sourceElementType, idx, targetElementType, idx);
            final Conversion<?> el = ctx.converter.convert(dependent, sourceElement);
            if (!el.valid) {
                return Conversion.no();
            }
            collection.add(el.value);
            ++i;
        }
        return Conversion.of(collection);
    }

}
