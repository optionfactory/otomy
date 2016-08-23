package net.optionfactory.otomy.converters;

import net.optionfactory.otomy.types.Typed;

public class MappingContext {

    public final Context source;
    public final Context target;
    public final Converter converter;
    public final Inspector inspector;

    public MappingContext(Context source, Context target, Inspector inspector, Converter converter) {
        this.source = source;
        this.target = target;
        this.inspector = inspector;
        this.converter = converter;
    }

    public MappingContext dependent(Typed sourceType, String sourceField, Typed targetType, String targetField) {
        final Context newSource = source.dependent(sourceType, sourceField);
        final Context newTarget = target.dependent(targetType, targetField);
        return new MappingContext(newSource, newTarget, inspector, converter);
    }

    public MappingContext dependentTarget(Typed targetType, String targetField) {
        final Context newTarget = target.dependent(targetType, targetField);
        return new MappingContext(source, newTarget, inspector, converter);
    }

    public MappingContext dependentSource(Typed sourceType, String sourceField) {
        final Context newSource = source.dependent(sourceType, sourceField);
        return new MappingContext(newSource, target, inspector, converter);
    }

    public MappingContext dependent(Typed sourceType, Typed targetType, String field, String... rest) {
        Context newSource = source.dependent(sourceType, field);
        Context newTarget = target.dependent(targetType, field);
        for (String f : rest) {
            newSource = newSource.dependent(sourceType, f);
            newTarget = newTarget.dependent(targetType, f);
        }
        return new MappingContext(newSource, newTarget, inspector, converter);
    }

    @Override
    public String toString() {
        return String.format("source:%s, target:%s", source, target);
    }

}
