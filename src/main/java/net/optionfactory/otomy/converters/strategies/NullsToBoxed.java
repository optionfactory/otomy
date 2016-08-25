package net.optionfactory.otomy.converters.strategies;

import java.lang.ref.PhantomReference;
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

public class NullsToBoxed implements Converter {

    @Override
    public Conversion<?> convert(MappingContext ctx, Object source) {
        if (source != null) {
            return Conversion.no();
        }
        final Class<?> tt = ctx.target.type.resolve();
        if (tt == Optional.class) {
            return Conversion.of(Optional.empty());
        }
        if (tt == AtomicReference.class) {
            return Conversion.of(new AtomicReference<>());
        }
        if (tt == AtomicBoolean.class) {
            return Conversion.of(new AtomicBoolean());
        }
        if (tt == AtomicInteger.class) {
            return Conversion.of(new AtomicInteger());
        }
        if (tt == AtomicIntegerArray.class) {
            return Conversion.of(new AtomicIntegerArray(0));
        }
        if (tt == AtomicLong.class) {
            return Conversion.of(new AtomicLong());
        }
        if (tt == AtomicLongArray.class) {
            return Conversion.of(new AtomicLongArray(0));
        }
        if (tt == SoftReference.class) {
            return Conversion.of(new SoftReference<>(null));
        }
        if (tt == WeakReference.class) {
            return Conversion.of(new WeakReference<>(null));
        }
        return Conversion.no();
    }

}
