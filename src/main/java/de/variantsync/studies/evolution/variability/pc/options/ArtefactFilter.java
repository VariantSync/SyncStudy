package de.variantsync.studies.evolution.variability.pc.options;

import de.variantsync.studies.evolution.variability.pc.Artefact;

import java.util.Arrays;
import java.util.Collection;

/**
 * Predicate to filter artefacts upon certain operations.
 * @param <A> The type of artefact that should be filtered.
 */
@FunctionalInterface
public interface ArtefactFilter<A extends Artefact> {
    /**
     * Determines whether a given artefact should be considered in or dropped from subsequent computations.
     * @param a The artefact that should be filtered.
     * @return True iff the given artefact should be kept and used in further computations.
     */
    boolean shouldKeep(final A a);

    /**
     * @return A filter that accepts all artifacts. Formally, its value is `a -> true`.
     */
    static <A extends Artefact> ArtefactFilter<A> KeepAll() {
        return a -> true;
    }

    /**
     * Folds all given filters into a single filter.
     * The returned filter will run all filters in the order they are returned by the given collection.
     * The returned filter returns false and stops computation as soon as one of the given filters
     * rejects the input artefact to test.
     * @param filters A collection of filters to collapse.
     * @param <A> The type of artefacts to filter.
     * @return A single filter that returns true for a given artifact iff all of the given filters return true for it.
     *         Formally, if `f` is the output filter `f.shouldKeep(a) iff (forall fi in filters: fi.shouldKeep(a))`.
     */
    static <A extends Artefact> ArtefactFilter<A> Fold(final Collection<ArtefactFilter<A>> filters) {
        return a -> {
            for (final ArtefactFilter<A> filter : filters) {
                if (!filter.shouldKeep(a)) {
                    return false;
                }
            }

            return true;
        };
    }

    /**
     * @see #Fold(Collection)
     */
    @SafeVarargs
    static <A extends Artefact> ArtefactFilter<A> Fold(final ArtefactFilter<A>... filters) {
        return Fold(Arrays.asList(filters));
    }
}
