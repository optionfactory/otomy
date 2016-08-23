package net.optionfactory.otomy.converters;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.optionfactory.otomy.types.Typed;

public class CachingInspector implements Inspector {

    public Map<Typed, Map<String, Accessor>> accessors = new ConcurrentHashMap<>();
    public Map<Typed, Map<String, Mutator>> mutators = new ConcurrentHashMap<>();
    public Map<MappingRequest, List<Mapping>> mappings = new ConcurrentHashMap<>();

    @Override
    public Map<String, Accessor> accessors(Typed type) {
        if (accessors.containsKey(type)) {
            return accessors.get(type);
        }
        final Map<String, Accessor> r = new ConcurrentHashMap<>();
        for (Field field : type.getFields()) {
            final FieldAccessor fa = new FieldAccessor(field);
            r.put(fa.label(), fa);
        }
        for (Method method : type.getMethods()) {
            if (method.isSynthetic()) {
                continue;
            }
            if (method.getParameterCount() != 0) {
                continue;
            }
            final Class<?> returnType = method.getReturnType();
            if (returnType == void.class) {
                continue;
            }
            final String name = method.getName();
            if (!name.startsWith("get") && !(name.startsWith("is") && (returnType == boolean.class || returnType == Boolean.class))) {
                continue;
            }
            final MethodAccessor ma = new MethodAccessor(method);
            r.put(ma.label(), ma);
        }
        accessors.put(type, r);
        return r;
    }

    @Override
    public Map<String, Mutator> mutators(Typed type) {
        if (mutators.containsKey(type)) {
            return mutators.get(type);
        }
        final Map<String, Mutator> r = new ConcurrentHashMap<>();
        for (Field field : type.getFields()) {
            if (Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            final FieldMutator fm = new FieldMutator(field);
            r.put(fm.label(), fm);
        }
        for (Method method : type.getMethods()) {
            if (method.isSynthetic()) {
                continue;
            }
            if (method.getParameterCount() != 1) {
                continue;
            }
            if (!method.getName().startsWith("set")) {
                continue;
            }
            final MethodMutator mm = new MethodMutator(method);
            r.put(mm.label(), mm);
        }
        mutators.put(type, r);
        return r;
    }

    @Override
    public List<Mapping> mappings(Typed sourcetype, Typed targetType) {
        final MappingRequest req = new MappingRequest(sourcetype, targetType);
        if (mappings.containsKey(req)) {
            return mappings.get(req);
        }
        final Map<String, Mutator> targetMutators = mutators(targetType);
        final Map<String, Accessor> sourceAccessors = accessors(sourcetype);
        final List<Mapping> result = new ArrayList<>();
        for (Map.Entry<String, Accessor> entry : sourceAccessors.entrySet()) {
            final String field = entry.getKey();
            if (!targetMutators.containsKey(field)) {
                continue;
            }
            result.add(new Mapping(entry.getValue(), targetMutators.get(field)));
        }
        mappings.put(req, result);
        return result;
    }

}
