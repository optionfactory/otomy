package net.optionfactory.otomy.converters;

import net.optionfactory.otomy.types.Typed;

/**
 *
 * @author rferranti
 */
public interface Mutator {

    public void mutate(MappingContext mappingContext, Object self, Object value);

    public String label();

    public Typed type(MappingContext mappingContext);
}
