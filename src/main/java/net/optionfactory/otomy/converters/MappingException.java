package net.optionfactory.otomy.converters;

public class MappingException extends IllegalArgumentException {

    public final MappingContext ctx;

    public MappingException(MappingContext ctx, Throwable cause) {
        super(String.format("%s[ctx: %s]", cause.getMessage(), ctx), cause);
        this.ctx = ctx;
    }

    public MappingException(MappingContext ctx, String reason) {
        super(String.format("%s[ctx: %s]", reason, ctx));
        this.ctx = ctx;
    }

}
