package net.optionfactory.otomy.converters.strategies;

import java.util.Optional;
import net.optionfactory.otomy.converters.Conversion;
import net.optionfactory.otomy.converters.Converter;
import net.optionfactory.otomy.converters.MappingContext;
import net.optionfactory.otomy.types.Typed;

public class Optionals implements Converter {

    @Override
    public Conversion<?> convert(MappingContext ctx, Object source) {
        final Class<?> resolvedSource = ctx.source.type.resolve();
        final Class<?> resolvedTarget = ctx.target.type.resolve();
        final boolean targetIsOptional = Optional.class == resolvedTarget;
        final boolean sourceIsOptional = Optional.class == resolvedSource;
        if (sourceIsOptional && targetIsOptional) {
            // optional to optional
            final Optional<?> m = (Optional<?>) source;
            if (!m.isPresent()) {
                return Conversion.of(Optional.empty());
            }
            final Typed elSourceType = ctx.source.type.getGeneric(0);
            final Typed elTargetType = ctx.target.type.getGeneric(0);
            final MappingContext dependent = ctx.dependent(elSourceType, elTargetType, "value");
            final Conversion<?> conversion = ctx.converter.convert(dependent, m.get());
            return conversion.valid ? Conversion.of(Optional.of(conversion.value)) : Conversion.no();
        }
        if (sourceIsOptional && !resolvedTarget.isPrimitive()) {
            //optional to nullable
            final Optional<?> m = (Optional<?>) source;
            if (!m.isPresent()) {
                return Conversion.nil();
            }
            final Typed elSourceType = ctx.source.type.getGeneric(0);
            final MappingContext dependent = ctx.dependentSource(elSourceType, "value");
            return ctx.converter.convert(dependent, m.get());
        }
        if (targetIsOptional) {
            //non-optional to optional
            if(source == null){
                return Conversion.of(Optional.empty());
            }
            final Typed elTargetType = ctx.target.type.getGeneric(0);
            final MappingContext dependent = ctx.dependentTarget(elTargetType, "value");
            final Conversion<?> nakedValue = ctx.converter.convert(dependent, source);
            return nakedValue.valid ? Conversion.of(Optional.ofNullable(nakedValue.value)) : nakedValue;
        }
        return Conversion.no();
    }

}
