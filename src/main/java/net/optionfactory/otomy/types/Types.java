package net.optionfactory.otomy.types;

import java.lang.ref.Reference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Types {

    private static final Map<Class<?>, Class<?>> BOXED_TO_UNBOXED = new IdentityHashMap<>(8);
    private static final Map<Class<?>, Class<?>> UNBOXED_TO_BOXED = new IdentityHashMap<>(8);

    static {
        BOXED_TO_UNBOXED.put(Boolean.class, boolean.class);
        BOXED_TO_UNBOXED.put(Byte.class, byte.class);
        BOXED_TO_UNBOXED.put(Character.class, char.class);
        BOXED_TO_UNBOXED.put(Double.class, double.class);
        BOXED_TO_UNBOXED.put(Float.class, float.class);
        BOXED_TO_UNBOXED.put(Integer.class, int.class);
        BOXED_TO_UNBOXED.put(Long.class, long.class);
        BOXED_TO_UNBOXED.put(Short.class, short.class);

        for (Map.Entry<Class<?>, Class<?>> entry : BOXED_TO_UNBOXED.entrySet()) {
            UNBOXED_TO_BOXED.put(entry.getValue(), entry.getKey());
        }

    }

    public static boolean isImmutableFromJavaLang(Class<?> t) {
        return t.isPrimitive()
                || isBoxedNumeric(t)
                || t == String.class
                || t == BigInteger.class
                || t == BigDecimal.class
                || t == Locale.class
                || t == UUID.class
                || t == URI.class
                || t == URL.class
                || t == Inet4Address.class
                || t == Inet6Address.class
                || t == InetSocketAddress.class
                || t.isEnum();
    }

    public static boolean isAssignable(Class<?> lhsType, Class<?> rhsType) {
        if (lhsType.isAssignableFrom(rhsType)) {
            return true;
        }
        if (lhsType.isPrimitive()) {
            Class<?> resolvedPrimitive = BOXED_TO_UNBOXED.get(rhsType);
            if (lhsType == resolvedPrimitive) {
                return true;
            }
        } else {
            Class<?> resolvedWrapper = UNBOXED_TO_BOXED.get(rhsType);
            if (resolvedWrapper != null && lhsType.isAssignableFrom(resolvedWrapper)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBoxedNumeric(Class<?> t) {
        return t == Boolean.class
                || t == Byte.class
                || t == Character.class
                || t == Double.class
                || t == Float.class
                || t == Integer.class
                || t == Long.class
                || t == Short.class
                || t == String.class
                || t == BigInteger.class
                || t == BigDecimal.class;
    }

    public static boolean isReferenceType(Class<?> t) {
        return Reference.class.isAssignableFrom(t) || t == AtomicReference.class;
    }

    public static Object referenceValue(Class<?> t, Object source) {
        if (AtomicReference.class == t) {
            return ((AtomicReference) source).get();
        }
        return ((Reference) source).get();

    }
    
    public static boolean isBoxType(Class<?> t){
        return Types.isReferenceType(t) 
                || t == AtomicInteger.class
                || t == AtomicLong.class
                || t == AtomicIntegerArray.class
                || t == AtomicLongArray.class;
    }

}
