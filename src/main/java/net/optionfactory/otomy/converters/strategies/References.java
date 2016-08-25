package net.optionfactory.otomy.converters.strategies;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;
import net.optionfactory.otomy.converters.Conversion;
import net.optionfactory.otomy.converters.Converter;
import net.optionfactory.otomy.converters.MappingContext;
import net.optionfactory.otomy.types.Types;

public class References implements Converter {

    @Override
    public Conversion<?> convert(MappingContext ctx, Object source) {
        final Class<?> st = ctx.source.type.resolve();
        final Class<?> tt = ctx.target.type.resolve();
        if(!Types.isReferenceType(st) || !Types.isReferenceType(tt)){
            return Conversion.no();
        }
        final Object referenced = Types.referenceValue(st, source);
        final MappingContext depCtx = ctx.dependent(ctx.source.type.getGeneric(0), ctx.target.type.getGeneric(0), "value");
        if(tt == AtomicReference.class){
            return ctx.converter.convert(depCtx, referenced).map(AtomicReference::new);
        }
        if (tt == WeakReference.class) {
            return ctx.converter.convert(depCtx, referenced).map(WeakReference::new);
        }
        if (tt == SoftReference.class) {
            return ctx.converter.convert(depCtx, referenced).map(SoftReference::new);
        }                
        return Conversion.no();
    }
}
