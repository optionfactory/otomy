package net.optionfactory.otomy.converters;

/**
 *
 * @author rferranti
 */
public interface Converter {

    public Conversion<?> convert(MappingContext ctx, Object source);
    
}
