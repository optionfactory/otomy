package net.optionfactory.otomy.converters.strategies;

import net.optionfactory.otomy.converters.Conversion;
import net.optionfactory.otomy.converters.Converter;
import net.optionfactory.otomy.converters.MappingContext;

/**
 *
 * @author rferranti
 */
public class Strings implements Converter {

    @Override
    public Conversion<?> convert(MappingContext ctx, Object source) {
        return ctx.target.type.resolve() == String.class ? Conversion.of(source.toString()) : Conversion.no();
    }

    
}
