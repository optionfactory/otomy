package net.optionfactory.otomy.converters;

import java.util.LinkedList;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.optionfactory.otomy.TypedMapper.Tracing;
import net.optionfactory.otomy.types.Typed;

public class Context {

    public final Typed type;
    public final Tracing tracing;
    public final Cons<String> path;

    public Context(Typed type, Tracing tracing) {
        this.type = type;
        this.tracing = tracing;
        this.path = Cons.nil();
    }

    private Context(Typed type, Tracing tracing, Cons<String> path) {
        this.type = type;
        this.tracing = tracing;
        this.path = path;
    }

    public Context dependent(Typed dep, String field) {
        return new Context(dep, this.tracing, Tracing.Enabled == tracing ? Cons.of(field, this.path) : this.path);
    }

    @Override
    public String toString() {
        if (Tracing.Enabled == tracing && path != Cons.<String>nil()) {
            final LinkedList<String> fs = path.stream().collect(Collectors.toCollection(LinkedList::new));
            final Spliterator<String> fsSpliterator = Spliterators.spliterator(fs.descendingIterator(), fs.size(), Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL);
            return String.format("%s::%s", StreamSupport.stream(fsSpliterator, false).collect(Collectors.joining(".")), type);
        }
        return type.toString();
    }

    public static class Cons<T> {

        private final T car;
        private final Cons<T> cdr;

        public Cons(T car, Cons<T> cdr) {
            this.car = car;
            this.cdr = cdr;
        }

        public static <T> Cons<T> of(T car, Cons<T> cdr) {
            return new Cons<>(car, cdr);
        }

        public static <T> Cons<T> nil() {
            return null;
        }

        public Stream<T> stream() {
            return StreamSupport.stream(new ConsSpliterator<>(this), false);
        }

        public static class ConsSpliterator<T> implements Spliterator<T> {

            private Cons<T> state;

            public ConsSpliterator(Cons<T> state) {
                this.state = state;
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                if (state == null) {
                    return false;
                }
                action.accept(state.car);
                state = state.cdr;
                return true;
            }

            @Override
            public Spliterator<T> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return -1;
            }

            @Override
            public int characteristics() {
                return ORDERED | IMMUTABLE;
            }

        }
    }

}
