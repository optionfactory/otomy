package net.optionfactory.otomy.converters.factories;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import net.optionfactory.otomy.converters.MappingContext;

public class CollectionFactories implements CollectionFactory {

    private final Collection<CollectionFactory> factories;

    public CollectionFactories(Collection<CollectionFactory> factories) {
        this.factories = factories;
    }

    @Override
    public Optional<Collection<?>> collection(MappingContext ctx) {
        for (CollectionFactory factory : factories) {
            final Optional<Collection<?>> candidate = factory.collection(ctx);
            if (candidate.isPresent()) {
                return candidate;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Map<?, ?>> map(MappingContext ctx) {
        for (CollectionFactory factory : factories) {
            final Optional<Map<?, ?>> candidate = factory.map(ctx);
            if (candidate.isPresent()) {
                return candidate;
            }
        }
        return Optional.empty();

    }

}
