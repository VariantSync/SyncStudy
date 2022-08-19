package de.variantsync.studies.evolution.variability.sequenceextraction;

import de.variantsync.studies.evolution.repository.Commit;
import de.variantsync.studies.evolution.util.functional.CachedValue;
import de.variantsync.studies.evolution.variability.EvolutionStep;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Stream for iterating pairs of commits.
 * This stream automatically cleans up after each iteration by invoking {@link CachedValue#forget()} after each
 * iteration step on each value that won't be visited anymore.
 * This stream allows iterating only once!
 * Afterwards it has to be rebuild.
 * @param <C> Type of commits to iterate over and cleanup.
 */
public class CleaningEvolutionStepsStream<C extends Commit & CachedValue> implements
        Iterator<EvolutionStep<C>>,
        Iterable<EvolutionStep<C>> {
    private final Stack<Stack<C>> dominos;
    private final List<C> garbage;

    public CleaningEvolutionStepsStream(final Iterable<EvolutionStep<C>> steps) {
        this.dominos = Domino.Sort(steps);
        this.garbage = new ArrayList<>();
    }

    public void clearCaches() {
        for (final C c : garbage) {
            c.forget();
        }
        garbage.clear();
    }

    @Override
    public boolean hasNext() {
        clearCaches();
        return !dominos.isEmpty();
    }

    @Override
    public EvolutionStep<C> next() {
        clearCaches();

        final Stack<C> currentRow = dominos.peek();

        assert !currentRow.isEmpty();
        final C child = currentRow.pop();
        garbage.add(child);

        assert !currentRow.isEmpty();
        final C parent = currentRow.peek();

        final EvolutionStep<C> result = new EvolutionStep<C>(parent, child);
        if (currentRow.size() == 1) {
            garbage.add(parent);
            dominos.pop();
        }
        return result;
    }

    @NotNull
    @Override
    public Iterator<EvolutionStep<C>> iterator() {
        return this;
    }

    @Override
    public String toString() {
        return "CleaningEvolutionStepsStream{" + dominos + '}';
    }
}
