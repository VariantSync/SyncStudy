package de.variantsync.studies.evolution.util.functional;

import de.variantsync.studies.evolution.util.Logger;
import de.variantsync.studies.evolution.util.functional.interfaces.FragileFunction;
import de.variantsync.studies.evolution.util.functional.interfaces.FragileProcedure;
import de.variantsync.studies.evolution.util.functional.interfaces.FragileSupplier;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Helper class containing methods for functional programming missing in the standard library
 * (or that we could not find).
 * Contains also methods for pattern matching.
 */
public class Functional {
    @SuppressWarnings("unchecked")
    public static <A, B> A uncheckedCast(final B b) {
        return (A) b;
    }

    /// Pattern matching

    public static <A, B> B match(final Optional<A> ma, final Function<A, ? extends B> just, final Supplier<? extends B> nothing) {
        final Optional<B> x = ma.map(just);
        return x.orElseGet(nothing);
    }

    /**
     * Curried version of the above.
     */
    public static <A, B> Function<Optional<A>, B> match(final Function<A, B> just, final Supplier<? extends B> nothing) {
        return ma -> match(ma, just, nothing);
    }

    public static <A, B, C> Function<Result<A, B>, C> match(final Function<A, C> success, final Function<B, C> failure) {
        return ma -> ma.match(success, failure);
    }

    /**
     * Creates a branching function for given condition, then and else case.
     * @param condition The condition choosing whether to run 'then' or 'otherwise'.
     * @param then The function to apply when the given condition is met for a given a.
     * @param otherwise The function to apply when the given condition is not met for a given a.
     * @return A function that for a given a, returns then(a) if the given condition is met, and otherwise returns otherwise(a).
     */
    public static <A, B> Function<A, B> when(final Predicate<A> condition, final Function<A, B> then, final Function<A, B> otherwise) {
        return a -> condition.test(a) ? then.apply(a) : otherwise.apply(a);
    }

    /**
     * The same as @see when but without an else case (i.e., else case function identity).
     */
    public static <A> Function<A, A> when(final Predicate<A> condition, final Function<A, A> then) {
        return when(condition, then, Function.identity());
    }

    /**
     * A variant of @see when with a boolean value instead of a predicate.
     */
    public static <B> Function<Boolean, B> when(final Supplier<B> then, final Supplier<B> otherwise) {
        return condition -> condition ? then.get() : otherwise.get();
    }

    /// Java to FP

    public static <E extends Exception> FragileSupplier<Unit, E> LiftFragile(final FragileProcedure<E> f) {
        return () -> {
            f.run();
            return Unit.Instance();
        };
    }

    /**
     * Maps the given function f onto the given value a if a is not null.
     *
     * @param n A nullable value that should be converted to a value of type B via f.
     * @param f A function that should be mapped onto a. f can safely assume that any arguments passed to it are not null.
     * @param errorMessage Creates an error message in case f threw an exception of type E.
     * @param <Nullable> The type of the nullable value a.
     * @param <B> The result type.
     * @param <E> The type of an exception that might be thrown by f.
     * @return Returns the result of f(a) if a is not null and f(a) did not throw an exception of type E.
     *         Returns Optional.empty() if a is null or f(a) threw an exception of type E.
     */
    public static <Nullable, B, E extends Exception> Optional<B> mapFragile(final Nullable n, final FragileFunction<Nullable, B, E> f, final Supplier<String> errorMessage) {
        return Optional.ofNullable(n).flatMap(a ->
                Result.Try(() -> f.run(a)).match(
                        Optional::ofNullable, // actually the returned B can also be null, thus ofNullable here
                        exception -> {
                            Logger.error(errorMessage.get(), exception);
                            return Optional.empty();
                        })
        );
    }

    public static <A, B, E extends Exception> Lazy<Optional<B>> mapFragileLazily(final A a, final FragileFunction<A, B, E> f, final Supplier<String> errorMessage) {
        return Lazy.of(() -> mapFragile(a, f, errorMessage));
    }
}
