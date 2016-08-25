package net.optionfactory.otomy;

import net.optionfactory.otomy.converters.Context;
import net.optionfactory.otomy.converters.Converter;
import net.optionfactory.otomy.converters.Inspector;
import net.optionfactory.otomy.converters.MappingContext;
import net.optionfactory.otomy.types.Typed;

public class TypedMapper implements Mapper {

    public enum Tracing {
        Disabled, Enabled
    }

    private final Converter converter;
    private final Inspector inspector;
    private final Tracing tracing;

    public TypedMapper(Inspector inspector, Converter converter, Tracing tracing) {
        this.converter = converter;
        this.inspector = inspector;
        this.tracing = tracing;
    }

    @Override
    public <R> R map(Typed sourceType, Object source, Typed targetType) {
        final Context srcCtx = new Context(sourceType, tracing);
        final Context dstCtx = new Context(targetType, tracing);
        final MappingContext ctx = new MappingContext(srcCtx, dstCtx, inspector, converter);
        return (R) converter.convert(ctx, source).value;
    }

}
