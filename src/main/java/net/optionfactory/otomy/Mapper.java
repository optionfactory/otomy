package net.optionfactory.otomy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.optionfactory.otomy.types.Typed;

/**
 *
 * @author rferranti
 */
public interface Mapper {

    <R> R map(Typed sourceType, Object source, Typed targetType);

    default <R> R map(Object source, Typed targetType) {
        if (source == null) {
            return null;
        }
        return map(Typed.class_(source.getClass()), source, targetType);
    }

    default <R> R map(Object source, Class<R> targetType) {
        if (source == null) {
            return null;
        }
        return map(Typed.class_(source.getClass()), source, Typed.class_(targetType));
    }

    default <T, R> R map(Class<T> sourceType, T source, Class<R> targetType) {
        if (source == null) {
            return null;
        }
        return map(Typed.class_(sourceType), source, Typed.class_(targetType));
    }

    default <R, T> Optional<R> map(Optional<T> source, Class<R> targetElementType) {
        if (source == null) {
            return null;
        }
        return source.map((sourceElement) -> map(sourceElement, targetElementType));
    }

    //streams
    default <R, T> Stream<R> map(Iterator<T> source, Class<R> targetElementType) {
        if (source == null) {
            return null;
        }
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(source, Spliterator.ORDERED), false).map(sourceElement -> map(sourceElement, targetElementType));
    }

    default <R, T> Stream<R> map(Iterator<T> source, Class<R> targetElementType, BiConsumer<R, T> postMappingCallback) {
        if (source == null) {
            return null;
        }
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(source, Spliterator.ORDERED), false).map(sourceElement -> {
            final R targetElement = map(sourceElement, targetElementType);
            postMappingCallback.accept(targetElement, sourceElement);
            return targetElement;
        });
    }

    default <R, T> Stream<R> map(Stream<T> source, Class<R> targetElementType) {
        if (source == null) {
            return null;
        }
        return source.map(sourceElement -> map(sourceElement, targetElementType));
    }

    default <R, T> Stream<R> map(Stream<T> source, Class<R> targetElementType, BiConsumer<R, T> postMappingCallback) {
        if (source == null) {
            return null;
        }
        return source.map(sourceElement -> {
            final R targetElement = map(sourceElement, targetElementType);
            postMappingCallback.accept(targetElement, sourceElement);
            return targetElement;
        });
    }

    //maps
    default <RV, K, V> Map<K, RV> map(Map<K, V> source, Class<RV> targetValueType) {
        if (source == null) {
            return null;
        }
        final Map<K, RV> target = new ConcurrentHashMap<>();
        for (Map.Entry<K, V> sourceEntry : source.entrySet()) {
            target.put(sourceEntry.getKey(), map(sourceEntry.getValue(), targetValueType));
        }
        return target;
    }

    default <RV, K, V> Map<K, RV> map(Map<K, V> source, Class<RV> targetValueType, BiConsumer<RV, V> postMappingCallback) {
        if (source == null) {
            return null;
        }
        final Map<K, RV> target = new ConcurrentHashMap<>();
        for (Map.Entry<K, V> sourceEntry : source.entrySet()) {
            final V sourceValue = sourceEntry.getValue();
            final RV targetValue = map(sourceValue, targetValueType);
            postMappingCallback.accept(targetValue, sourceValue);
            target.put(sourceEntry.getKey(), targetValue);
        }
        return target;
    }

    // collections
    default <R, T> List<R> map(Iterable<T> source, Class<R> targetElementType) {
        if (source == null) {
            return null;
        }
        final List<R> target = new ArrayList<>();
        for (T sourceElement : source) {
            target.add(map(sourceElement, targetElementType));
        }
        return target;
    }

    default <R, T> List<R> map(Iterable<T> source, Class<R> targetElementType, BiConsumer<R, T> postMappingCallback) {
        if (source == null) {
            return null;
        }
        final List<R> target = new ArrayList<>();
        for (T sourceElement : source) {
            final R mapped = map(sourceElement, targetElementType);
            postMappingCallback.accept(mapped, sourceElement);
            target.add(mapped);
        }
        return target;
    }

    default <R, T, C extends Collection<R>> C map(Iterable<T> source, C target, Class<R> targetElementType) {
        if (source == null) {
            return null;
        }
        for (T sourceElement : source) {
            target.add(map(sourceElement, targetElementType));
        }
        return target;
    }

    default <R, T, C extends Collection<R>> C map(Iterable<T> source, C target, Class<R> targetElementType, BiConsumer<R, T> postMappingCallback) {
        if (source == null) {
            return null;
        }
        for (T sourceElement : source) {
            final R targetElement = map(sourceElement, targetElementType);
            postMappingCallback.accept(targetElement, sourceElement);
            target.add(targetElement);
        }
        return target;
    }
}
