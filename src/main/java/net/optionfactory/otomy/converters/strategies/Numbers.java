package net.optionfactory.otomy.converters.strategies;

import java.math.BigDecimal;
import java.math.BigInteger;
import net.optionfactory.otomy.converters.Conversion;
import net.optionfactory.otomy.converters.Converter;
import net.optionfactory.otomy.converters.MappingContext;

public class Numbers implements Converter {

    @Override
    public Conversion<?> convert(MappingContext ctx, Object source) {
        final Class<?> tc = ctx.target.type.resolve();
        if(!Number.class.isAssignableFrom(tc) && !tc.isPrimitive() || tc == char.class) {
            return Conversion.no();
        }
        if(source instanceof CharSequence){
            if(tc == Long.class || tc == long.class){
                return Conversion.of(Long.parseLong(source.toString()));
            }
            if(tc == Integer.class || tc == int.class){
                return Conversion.of(Integer.parseInt(source.toString()));
            
            }
            if(tc == Short.class || tc == short.class){
                return Conversion.of(Short.parseShort(source.toString()));
            
            }
            if(tc == Byte.class || tc == byte.class){
                return Conversion.of(Byte.parseByte(source.toString()));
            
            }
            if(tc == Double.class || tc == double.class){
                return Conversion.of(Double.parseDouble(source.toString()));
            
            }
            if(tc == Float.class || tc == float.class){
                return Conversion.of(Float.parseFloat(source.toString()));
            
            }
            if(tc == BigInteger.class){
                return Conversion.of(new BigInteger(source.toString()));
            
            }
            if(tc == BigDecimal.class){
                return Conversion.of(new BigDecimal(source.toString()));
            }
            //TODO: Atomic*
        }
        if(source instanceof Number){
            final Number n = (Number)source;
            if(tc == Long.class || tc == long.class){
                return Conversion.of(n.longValue());
            }
            if(tc == Integer.class || tc == int.class){
                return Conversion.of(n.intValue());
            }
            if(tc == Short.class || tc == short.class){
                return Conversion.of(n.shortValue());
            }
            if(tc == Byte.class || tc == byte.class){
                return Conversion.of(n.byteValue());
            }
            if(tc == Double.class || tc == double.class){
                return Conversion.of(n.doubleValue());
            }
            if(tc == Float.class || tc == float.class){
                return Conversion.of(n.floatValue());
            }
            if(tc == BigInteger.class && source == BigDecimal.class){
                return Conversion.of(((BigDecimal)n).toBigInteger());                
            }
            if(tc == BigInteger.class){
                return Conversion.of(BigInteger.valueOf(n.longValue()));
            }
            if(tc == BigDecimal.class){
                return Conversion.of(new BigDecimal(source.toString()));
            }            

        }
        return Conversion.no();
    }
}
