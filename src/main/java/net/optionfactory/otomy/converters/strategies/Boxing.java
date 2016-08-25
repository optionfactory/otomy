package net.optionfactory.otomy.converters.strategies;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReference;
import net.optionfactory.otomy.converters.Conversion;
import net.optionfactory.otomy.converters.Converter;
import net.optionfactory.otomy.converters.MappingContext;
import net.optionfactory.otomy.types.Typed;
import net.optionfactory.otomy.types.Types;

public class Boxing implements Converter {

    @Override
    public Conversion<?> convert(MappingContext ctx, Object source) {
        final Class<?> st = ctx.source.type.resolve();
        final Class<?> tt = ctx.target.type.resolve();
        if (Types.isBoxType(st) || !Types.isBoxType(tt)) {
            return Conversion.no();
        }
        if (tt == AtomicReference.class) {
            final MappingContext depCtx = ctx.dependentTarget(ctx.target.type.getGeneric(0), "value");
            return ctx.converter.convert(depCtx, source).map(AtomicReference::new);
        }
        if (tt == SoftReference.class) {
            final MappingContext depCtx = ctx.dependentTarget(ctx.target.type.getGeneric(0), "value");
            return ctx.converter.convert(depCtx, source).map(SoftReference::new);
        }
        if (tt == WeakReference.class) {
            final MappingContext depCtx = ctx.dependentTarget(ctx.target.type.getGeneric(0), "value");
            return ctx.converter.convert(depCtx, source).map(WeakReference::new);
        }
        if (tt == Optional.class) {
            final MappingContext depCtx = ctx.dependentTarget(ctx.target.type.getGeneric(0), "value");
            return ctx.converter.convert(depCtx, source).map(Optional::ofNullable);
        }
        if (tt == AtomicBoolean.class) {
            final MappingContext depCtx = ctx.dependentTarget(Typed.class_(Boolean.class), "value");
            final Conversion<Boolean> conversion = (Conversion<Boolean>) ctx.converter.convert(depCtx, source);
            return conversion.map(AtomicBoolean::new);
        }
        if (tt == AtomicLong.class) {
            final MappingContext depCtx = ctx.dependentTarget(Typed.class_(Long.class), "value");
            final Conversion<Long> conversion = (Conversion<Long>) ctx.converter.convert(depCtx, source);
            return conversion.map(AtomicLong::new);
        }
        if (tt == AtomicInteger.class) {
            final MappingContext depCtx = ctx.dependentTarget(Typed.class_(Integer.class), "value");
            final Conversion<Integer> conversion = (Conversion<Integer>) ctx.converter.convert(depCtx, source);
            return conversion.map(AtomicInteger::new);
        }
        if (tt == AtomicLongArray.class) {
            final MappingContext depCtx = ctx.dependentTarget(Typed.class_(long[].class), "value");
            final Conversion<long[]> conversion = (Conversion<long[]>) ctx.converter.convert(depCtx, source);
            return conversion.map(AtomicLongArray::new);
        }
        if (tt == AtomicIntegerArray.class) {
            final MappingContext depCtx = ctx.dependentTarget(Typed.class_(int[].class), "value");
            final Conversion<int[]> conversion = (Conversion<int[]>) ctx.converter.convert(depCtx, source);
            return conversion.map(AtomicIntegerArray::new);
        }
        return Conversion.no();

    }

}
