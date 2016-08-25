package net.optionfactory.otomy.converters.strategies;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import net.optionfactory.otomy.converters.Conversion;
import net.optionfactory.otomy.converters.Converter;
import net.optionfactory.otomy.converters.MappingContext;
import net.optionfactory.otomy.types.Types;

public class Unboxing implements Converter {

    @Override
    public Conversion<?> convert(MappingContext ctx, Object source) {
        final Class<?> st = ctx.source.type.resolve();
        final Class<?> tt = ctx.target.type.resolve();
        if (!Types.isBoxType(st) || Types.isBoxType(tt)) {
            return Conversion.no();
        }
        if (Types.isReferenceType(st)) {
            final MappingContext depCtx = ctx.dependentSource(ctx.source.type.getGeneric(0), "value");
            return ctx.converter.convert(depCtx, Types.referenceValue(st, source));
        }
        if (st == Optional.class) {
            final MappingContext depCtx = ctx.dependentSource(ctx.source.type.getGeneric(0), "value");
            return ctx.converter.convert(depCtx, ((Optional<?>) source).orElse(null));
        }
        if (st == AtomicBoolean.class) {
            final MappingContext depCtx = ctx.dependentSource(ctx.source.type.getGeneric(0), "value");
            return ctx.converter.convert(depCtx, ((AtomicBoolean) source).get());
        }
        if (st == AtomicLong.class) {
            final MappingContext depCtx = ctx.dependentSource(ctx.source.type.getGeneric(0), "value");
            return ctx.converter.convert(depCtx, ((AtomicLong) source).get());
        }
        if (st == AtomicInteger.class) {
            final MappingContext depCtx = ctx.dependentSource(ctx.source.type.getGeneric(0), "value");
            return ctx.converter.convert(depCtx, ((AtomicInteger) source).get());
        }
        if (st == AtomicLongArray.class) {
            final MappingContext depCtx = ctx.dependentSource(ctx.source.type.getGeneric(0), "value");            
            final AtomicLongArray alr = (AtomicLongArray) source;
            final long[] copy = new long[alr.length()];
            for(int i=0;i!= copy.length;++i){
                copy[i] = alr.get(i);
            }
            return ctx.converter.convert(depCtx, copy);
        }
        if (st == AtomicIntegerArray.class) {
            final MappingContext depCtx = ctx.dependentSource(ctx.source.type.getGeneric(0), "value");
            final AtomicIntegerArray alr = (AtomicIntegerArray) source;
            final int[] copy = new int[alr.length()];
            for(int i=0;i!= copy.length;++i){
                copy[i] = alr.get(i);
            }
            return ctx.converter.convert(depCtx, copy);
        }
        return Conversion.no();
    }

}
