package net.optionfactory.otomy.converters.strategies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.optionfactory.otomy.converters.Conversion;
import net.optionfactory.otomy.converters.Converter;
import net.optionfactory.otomy.converters.MappingContext;
import net.optionfactory.otomy.converters.factories.CollectionFactories;
import net.optionfactory.otomy.converters.factories.CollectionFactory;
import net.optionfactory.otomy.converters.factories.JavaLangCollectionFactory;
import net.optionfactory.otomy.types.Typed;

/**
 *
 * @author rferranti
 */
public class Strategies implements Converter {

    private final Collection<Converter> converters;

    public Strategies(Collection<Converter> converters) {
        this.converters = converters;
    }

    @Override
    public Conversion<?> convert(MappingContext ctx, Object source) {
        for (Converter converter : converters) {
            Conversion<?> conversion = converter.convert(ctx, source);
            if (conversion.valid) {
                return conversion;
            }
        }
        return Conversion.no();
    }

    public static Strategies composite(Collection<Converter> converters) {
        return new Strategies(converters);
    }

    public static Strategies composite(Converter... converters) {
        return new Strategies(Arrays.asList(converters));
    }

    public static Strategies defaults(Set<Typed> customImmutables, Collection<CollectionFactory> customCollectionFactories, Collection<Converter> customNullConverters, Collection<Converter> customConverters) {
        final List<CollectionFactory> factories = new ArrayList<>();
        factories.addAll(customCollectionFactories);
        factories.add(new JavaLangCollectionFactory());

        final List<Converter> builtin = Arrays.<Converter>asList(
                new NullsToBoxed(),
                new Nulls(),
                new Immutables(customImmutables),
                new Optionals(),
                new Iterables(new CollectionFactories(factories)),
                new Dates(),
                new Numbers(),
                new References()
        );

        final List<Converter> generic = Arrays.<Converter>asList(
                new Unboxing(),
                new Boxing(),
                new Strings(),
                new Beans()
        );

        final Stream<Converter> cs = Stream.concat(
                Stream.concat(
                        Stream.concat(customNullConverters.stream(), builtin.stream()),
                        customConverters.stream()), generic.stream());
        return new Strategies(cs.collect(Collectors.toList()));

    }

    public static Strategies defaults() {
        return defaults(Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

}
