package net.optionfactory.otomy.converters;

public class MappingException extends IllegalArgumentException {

    public final MappingContext ctx;

    public MappingException(MappingContext ctx, Throwable cause) {
        super(cause.getMessage() + String.format("[ctx: %s]", ctx), cause);
        this.ctx = ctx;
    }

}
