package net.optionfactory.otomy.converters.strategies;

import java.util.Set;
import net.optionfactory.otomy.converters.Conversion;
import net.optionfactory.otomy.converters.Converter;
import net.optionfactory.otomy.converters.MappingContext;
import net.optionfactory.otomy.types.Typed;
import net.optionfactory.otomy.types.Types;

/**
 *
 * @author rferranti
 */
public class Immutables implements Converter {

    private final Set<Typed> immutables;

    public Immutables(Set<Typed> immutables) {
        this.immutables = immutables;
    }

    @Override
    public Conversion<?> convert(MappingContext ctx, Object source) {
        final Class<?> t = ctx.target.type.resolve();
        if (!Types.isImmutableFromJavaLang(t) && !immutables.contains(ctx.target.type)) {
            return Conversion.no();
        }
        return ctx.target.type.isAssignableFrom(ctx.source.type) ? Conversion.of(source) : Conversion.no();
    }


}
