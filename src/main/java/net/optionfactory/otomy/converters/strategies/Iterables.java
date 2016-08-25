package net.optionfactory.otomy.converters.strategies;

import java.lang.reflect.Array;
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
            //map to map
            final Map<Object, Object> sourceMap = (Map<Object, Object>) source;
            final Optional<Map<Object, Object>> maybeResult = (Optional) factory.map(ctx, sourceMap.size());
            if (!maybeResult.isPresent()) {
                return Conversion.no();
            }
            final Map<Object, Object> result = maybeResult.get();
            long i = 0;
            for (Map.Entry<Object, Object> sourceEntry : sourceMap.entrySet()) {
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
        if (Iterable.class.isAssignableFrom(sourceClass) && Iterable.class.isAssignableFrom(targetClass)) {
            //iterable to iterable
            final Iterable<?> sourceIterable = (Iterable<?>) source;
            final int maybeSize = (int) sourceIterable.spliterator().getExactSizeIfKnown();
            final Optional<Collection<Object>> maybeCollection = (Optional) factory.collection(ctx, maybeSize);
            if (!maybeCollection.isPresent()) {
                return Conversion.no();
            }
            final Collection<Object> collection = maybeCollection.get();
            final Typed targetElementType = ctx.target.type.getGeneric(0);
            final Typed sourceElementType = ctx.source.type.getGeneric(0);
            long i = 0;
            for (Object sourceElement : sourceIterable) {
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
        if (Iterable.class.isAssignableFrom(sourceClass) && targetClass.isArray()) {
            //iterable to array
            final Typed targetElementType = ctx.target.type.getComponentType();
            final Typed sourceElementType = ctx.source.type.getGeneric(0);
            final Iterable<?> sourceAsIterable = (Iterable<?>) source;
            final int size = size(sourceAsIterable);
            final Object targetArray = Array.newInstance(targetElementType.resolve(), size);
            int i = 0;
            for (Object sourceElement : (Iterable<?>) source) {
                final String idx = Long.toString(i);
                final MappingContext dependent = ctx.dependent(sourceElementType, idx, targetElementType, idx);
                final Conversion<?> el = ctx.converter.convert(dependent, sourceElement);
                if (!el.valid) {
                    return Conversion.no();
                }
                Array.set(targetArray, i, el.value);
                ++i;
            }
            return Conversion.of(targetArray);
        }
        if (sourceClass.isArray() && Iterable.class.isAssignableFrom(targetClass)) {
            // array to iterable
            final int len = Array.getLength(source);
            final Optional<Collection<Object>> maybeCollection = (Optional) factory.collection(ctx, len);
            if (!maybeCollection.isPresent()) {
                return Conversion.no();
            }
            final Collection<Object> collection = maybeCollection.get();
            final Typed sourceElementType = ctx.source.type.getComponentType();
            final Typed targetElementType = ctx.target.type.getGeneric(0);
            for (int i = 0; i != len; ++i) {
                final MappingContext depCtx = ctx.dependent(sourceElementType, targetElementType, Long.toString(i));
                final Conversion<?> el = ctx.converter.convert(depCtx, Array.get(source, i));
                if (!el.valid) {
                    return Conversion.no();
                }
                collection.add(el.value);
            }
        }
        if (sourceClass.isArray() && targetClass.isArray()) {
            // array to array
            final Typed targetElementType = ctx.target.type.getComponentType();
            final Typed sourceElementType = ctx.source.type.getComponentType();
            final int len = Array.getLength(source);
            final Object targetArray = Array.newInstance(targetElementType.resolve(), len);
            for (int i = 0; i != len; ++i) {
                final MappingContext depCtx = ctx.dependent(sourceElementType, targetElementType, Long.toString(i));
                final Conversion<?> el = ctx.converter.convert(depCtx, Array.get(source, i));
                if (!el.valid) {
                    return Conversion.no();
                }
                Array.set(targetArray, i, el);
            }

        }
        return Conversion.no();
    }

    private int size(Iterable<?> iterable) {
        final long maybeSize = iterable.spliterator().getExactSizeIfKnown();
        if (maybeSize != -1) {
            return (int) maybeSize;
        }
        long i = 0;
        for (Object object : iterable) {
            ++i;
        }
        //TODO: MAX_INT
        return (int) i;
    }
}
