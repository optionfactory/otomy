package net.optionfactory.otomy.converters.strategies;

import java.util.Date;
import net.optionfactory.otomy.converters.Conversion;
import net.optionfactory.otomy.converters.Converter;
import net.optionfactory.otomy.converters.MappingContext;
import net.optionfactory.otomy.types.Typed;

/**
 *
 * @author rferranti
 */
public class Dates implements Converter {

    private static final Typed DATE_TYPE = Typed.class_(Date.class);

    @Override
    public Conversion<?> convert(MappingContext ctx, Object source) {
        final Class<?> sourceClass = ctx.source.type.resolve();
        final Class<?> targetClass = ctx.target.type.resolve();
        if ((targetClass == Long.class || targetClass == long.class) && DATE_TYPE.isAssignableFrom(ctx.source.type)) {
            // date to long
            final Long value = source == null ? (ctx.target.type.resolve() == long.class ? 0l : null) : ((Date) source).getTime();
            return Conversion.of(value);
        }

        //long to dates
        if ((sourceClass == Long.class || sourceClass == long.class) && DATE_TYPE.isAssignableFrom(ctx.target.type)) {
            if (source == null) {
                return Conversion.nil();
            }
            if (ctx.target.type.resolve() == java.util.Date.class) {
                return Conversion.of(new java.util.Date((Long) source));
            }
            return Conversion.of(new java.sql.Date((Long) source));
        }
        return Conversion.no();
    }

}
