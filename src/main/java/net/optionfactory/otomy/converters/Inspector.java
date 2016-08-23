package net.optionfactory.otomy.converters;

import java.util.List;
import java.util.Map;
import net.optionfactory.otomy.types.Typed;

public interface Inspector {

    Map<String, Accessor> accessors(Typed type);

    List<Mapping> mappings(Typed sourcetype, Typed targetType);

    Map<String, Mutator> mutators(Typed type);

}
