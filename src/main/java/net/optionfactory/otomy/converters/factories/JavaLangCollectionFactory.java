package net.optionfactory.otomy.converters.factories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.optionfactory.otomy.converters.MappingContext;

public class JavaLangCollectionFactory implements CollectionFactory {

    @Override
    public Optional<Collection<?>> collection(MappingContext context, int initialSize) {
        final Class<?> targetType = context.target.type.resolve();
        if (targetType == List.class || targetType == ArrayList.class) {
            return Optional.of(initialSize == -1 ? new ArrayList<>() : new ArrayList<>(initialSize));
        }
        if (targetType.isAssignableFrom(LinkedList.class)) {
            //Queue, Dequeue, LinkedList
            return Optional.of(new LinkedList<>());
        }
        if (targetType == Set.class || targetType == HashSet.class) {
            return Optional.of(initialSize == -1 ? new HashSet<>() : new HashSet<>(initialSize));
        }
        //TODO: TreeSet and all sortable collections handling comparators
        return Optional.empty();
    }

    @Override
    public Optional<Map<?, ?>> map(MappingContext context, int initialSize) {
        final Class<?> targetType = context.target.type.resolve();

        if(targetType == Map.class || targetType == ConcurrentMap.class){
            return Optional.of(initialSize == -1 ? new ConcurrentHashMap<>(): new ConcurrentHashMap<>(initialSize));            
        }
        if (targetType == EnumMap.class) {
            final Class<?> enumType = context.target.type.getGeneric(0).resolve();
            return Optional.of(new EnumMap(enumType));
        }
        if (targetType == Properties.class) {
            return Optional.of(new Properties());
        }
        if (targetType == LinkedHashMap.class) {
            return Optional.of(initialSize == -1 ? new LinkedHashMap<>(): new LinkedHashMap<>(initialSize));
        }
        if (targetType == IdentityHashMap.class) {
            return Optional.of(initialSize == -1 ? new IdentityHashMap<>() : new IdentityHashMap<>(initialSize));
        }
        if (targetType == WeakHashMap.class) {
            return Optional.of(new WeakHashMap<>());
        }

        if (targetType == HashMap.class) {
            return Optional.of(initialSize == -1 ? new HashMap<>() : new HashMap<>(initialSize));
        }
        if (targetType == Hashtable.class) {
            return Optional.of(initialSize == -1 ? new Hashtable<>(): new Hashtable<>(initialSize));
        }
        //TODO: SortedMaps (ConcurrentSkipListMap and TreeMap handling comparators)
        return Optional.of(initialSize == -1 ?new ConcurrentHashMap<>() : new ConcurrentHashMap<>(initialSize));
    }

}
