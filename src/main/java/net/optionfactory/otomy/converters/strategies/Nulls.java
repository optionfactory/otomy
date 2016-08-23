package net.optionfactory.otomy.converters.strategies;

import net.optionfactory.otomy.converters.Conversion;
import net.optionfactory.otomy.converters.Converter;
import net.optionfactory.otomy.converters.MappingContext;

/**
 *
 * @author rferranti
 */
public class Nulls implements Converter {

    @Override
    public Conversion<?> convert(MappingContext ctx, Object source) {
        return source != null || ctx.target.type.resolve().isPrimitive() ? Conversion.no() : Conversion.nil();
    }

}
