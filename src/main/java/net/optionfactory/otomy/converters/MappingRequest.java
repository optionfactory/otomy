package net.optionfactory.otomy.converters;

import net.optionfactory.otomy.types.Typed;

public class MappingRequest {

    public final Typed source;
    public final Typed target;
    private final int hash;

    public MappingRequest(Typed source, Typed target) {
        this.source = source;
        this.target = target;
        this.hash = source.hashCode() + 31 * target.hashCode();
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object rhs) {
        if (this == rhs) {
            return true;
        }
        if (rhs instanceof MappingRequest == false) {
            return false;
        }
        final MappingRequest other = (MappingRequest) rhs;
        return this.source.equals(other.source) && this.target.equals(other.target);
    }

}
