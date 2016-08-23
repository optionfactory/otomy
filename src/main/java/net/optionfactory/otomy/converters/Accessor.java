package net.optionfactory.otomy.converters;

import net.optionfactory.otomy.types.Typed;

/**
 *
 * @author rferranti
 */
public interface Accessor {

    public Object access(MappingContext mappingContext, Object source);

    public String label();

    public Typed type(MappingContext mapping);
}
