package de.variantsync.studies.evolution.util.functional;

import java.util.Optional;
import java.util.function.Function;

/**
 * A utility class containing functions of monad transformers.
 * As java does not support higher-kinded types it is not possible to implement generic MonadTransformers
 * (such as MaybeT or StateT from haskell) without a decent amount of janky hacks.
 * Thus, this class contains the implementations of monad transformers fixed the the input monad type.
 */
public class MonadTransformer {
    private MonadTransformer() {}

    /// Lazy<Optional<T>>

    public static <A, B> Lazy<Optional<B>> bind(final Lazy<Optional<A>> m, final Function<A, Lazy<Optional<B>>> f) {
        return m.bind(Functional.match(
                /* Just a  */ f,
                /* Nothing */ () -> Lazy.of(Optional::empty)
            ));
    }

    public static <A> Lazy<Optional<A>> pure(final A a) {
        return Lazy.pure(Optional.ofNullable(a));
    }
}
