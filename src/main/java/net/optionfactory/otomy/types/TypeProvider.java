package net.optionfactory.otomy.types;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * A {@link Serializable} interface providing access to a {@link Type}.
 */
public interface TypeProvider {

    /**
     * Return the (possibly non {@link Serializable}) {@link Type}.
     */
    Type getType();

}
