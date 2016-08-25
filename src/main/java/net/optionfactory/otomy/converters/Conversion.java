package net.optionfactory.otomy.converters;

import java.util.function.Function;

public class Conversion<T> {

    private static final Conversion<?> NO = new Conversion<>(null, false);
    private static final Conversion<?> NULL = new Conversion<>(null, true);
    public final T value;
    public final boolean valid;

    private Conversion(T value, boolean valid) {
        this.value = value;
        this.valid = valid;
    }

    public static <T> Conversion<T> no() {
        return (Conversion<T>) NO;
    }

    public static <T> Conversion<T> nil() {
        return (Conversion<T>) NULL;
    }

    public static <T> Conversion<T> of(T value) {
        return new Conversion<>(value, true);
    }

    public <R> Conversion<R> map(Function<T, R> fn) {
        if (!valid) {
            return (Conversion<R>) this;
        }
        return Conversion.of(fn.apply(value));
    }

}
