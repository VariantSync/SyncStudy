package de.variantsync.studies.evolution.variants.blueprints;

import de.variantsync.studies.evolution.feature.sampling.Sample;
import de.variantsync.studies.evolution.util.functional.Lazy;
import de.variantsync.studies.evolution.variants.VariantsRevision;

import java.util.Optional;

/**
 * A VariantsRevisionBlueprint holds instructions on how to generate variants and commits
 * for a VariantsRevision.
 * The main purpose of blueprints is to distinguish if variants should be generated as usual
 * (VariantsRevisionFormVariabilityBlueprint) or if we have to generate an artificial commit
 * for each variant indicating that an de.variantsync.studies.evolution history ends (VariantsRevisionFromErrorBlueprint).
 */
public abstract class VariantsRevisionBlueprint {
    // We wrap computeSample in another Lazy to cache the result of computeSample (which is yet another lazy).
    // Assume computeSample returns a new object each time it is called (which could happen).
    // Then whenever we do computeSample().run(), the sample would be calculated anew which could be costly.
    // Thus, we cache the lazy that computes the sample (i.e., computeSample()) by putting it into another
    // lazy (i.e., Lazy.of(this::computeSample)). We flatten the result with join.
    private final Lazy<Sample> sample = Lazy.join(Lazy.of(this::computeSample));

    private final Optional<VariantsRevisionBlueprint> predecessor;

    public VariantsRevisionBlueprint(VariantsRevisionBlueprint predecessor) {
        this.predecessor = Optional.ofNullable(predecessor);
    }

    /**
     * @return The sample of variants that should be generated for target VariantsRevision.
     */
    public Lazy<Sample> getSample() {
        return sample;
    }

    public Optional<VariantsRevisionBlueprint> getPredecessor() {
        return predecessor;
    }

    /**
     * @return A computation yielding the sample. The returned lazy is cached and returned whenever getSample is called.
     */
    protected abstract Lazy<Sample> computeSample();

    /**
     * Generates the variants for the given VariantsRevision and commits them to the corresponding AbstractVariantsRepository.
     * @param revision The revision for which variants should be generated to disk.
     * @return A computation that when run, generates the variants and returns the generated commits.
     */
    public abstract Lazy<VariantsRevision.Branches> generateArtefactsFor(VariantsRevision revision);
}
