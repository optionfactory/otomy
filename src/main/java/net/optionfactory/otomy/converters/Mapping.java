package net.optionfactory.otomy.converters;

public class Mapping {

    public final Accessor accessor;
    public final Mutator mutator;

    public Mapping(Accessor accessor, Mutator mutator) {
        this.accessor = accessor;
        this.mutator = mutator;
    }

}
