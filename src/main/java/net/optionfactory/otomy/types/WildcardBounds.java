package net.optionfactory.otomy.types;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * Internal helper to handle bounds from {@link WildcardType}s.
 */
public class WildcardBounds {

    private final Kind kind;
    private final Typed[] bounds;

    /**
     * Internal constructor to create a new {@link WildcardBounds} instance.
     * @param kind the kind of bounds
     * @param bounds the bounds
     * @see #get(ResolvableType)
     */
    public WildcardBounds(Kind kind, Typed[] bounds) {
        this.kind = kind;
        this.bounds = bounds;
    }

    /**
     * Return {@code true} if this bounds is the same kind as the specified bounds.
     */
    public boolean isSameKind(WildcardBounds bounds) {
        return this.kind == bounds.kind;
    }

    /**
     * Return {@code true} if this bounds is assignable to all the specified types.
     * @param types the types to test against
     * @return {@code true} if this bounds is assignable to all types
     */
    public boolean isAssignableFrom(Typed... types) {
        for (Typed bound : this.bounds) {
            for (Typed type : types) {
                if (!isAssignable(bound, type)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isAssignable(Typed source, Typed from) {
        return this.kind == Kind.UPPER ? source.isAssignableFrom(from) : from.isAssignableFrom(source);
    }

    /**
     * Return the underlying bounds.
     */
    public Typed[] getBounds() {
        return this.bounds;
    }

    /**
     * Get a {@link WildcardBounds} instance for the specified type, returning
     * {@code null} if the specified type cannot be resolved to a {@link WildcardType}.
     * @param type the source type
     * @return a {@link WildcardBounds} instance or {@code null}
     */
    public static WildcardBounds get(Typed type) {
        Typed resolveToWildcard = type;
        while (!(resolveToWildcard.getType() instanceof WildcardType)) {
            if (resolveToWildcard == Typed.NONE) {
                return null;
            }
            resolveToWildcard = resolveToWildcard.resolveType();
        }
        WildcardType wildcardType = (WildcardType) resolveToWildcard.type;
        Kind boundsType = wildcardType.getLowerBounds().length > 0 ? Kind.LOWER : Kind.UPPER;
        Type[] bounds = boundsType == Kind.UPPER ? wildcardType.getUpperBounds() : wildcardType.getLowerBounds();
        Typed[] resolvableBounds = new Typed[bounds.length];
        for (int i = 0; i < bounds.length; i++) {
            resolvableBounds[i] = Typed.type(bounds[i], type.resolutionContext);
        }
        return new WildcardBounds(boundsType, resolvableBounds);
    }

    /**
     * The various kinds of bounds.
     */
    enum Kind {
        UPPER, LOWER
    }

}
