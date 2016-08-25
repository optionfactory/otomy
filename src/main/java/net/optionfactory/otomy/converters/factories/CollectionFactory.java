package net.optionfactory.otomy.converters.factories;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import net.optionfactory.otomy.converters.MappingContext;

public interface CollectionFactory {

    public Optional<Collection<?>> collection(MappingContext ctx, int initialSize);

    public Optional<Map<?, ?>> map(MappingContext ctx, int initialSize);
}
