package de.variantsync.studies.evolution.variants;

import de.variantsync.studies.evolution.repository.AbstractSPLRepository;
import de.variantsync.studies.evolution.repository.AbstractVariantsRepository;
import de.variantsync.studies.evolution.repository.Branch;
import de.variantsync.studies.evolution.util.functional.Lazy;
import de.variantsync.studies.evolution.util.functional.MonadTransformer;
import de.variantsync.studies.evolution.util.functional.Unit;
import de.variantsync.studies.evolution.util.list.ListHeadTailView;
import de.variantsync.studies.evolution.variants.blueprints.VariantsRevisionBlueprint;

import java.util.Map;
import java.util.Optional;

/**
 * Models a cut in the de.variantsync.studies.evolution history of the IVariantsRevision.
 * Each commit to the SPL is reproduced on variants as a commit for each variant.
 * Thus, several variants co-evolve at the same time.
 * A VariantsRevision runs and captures this co-de.variantsync.studies.evolution for a single commit (i.e., modelled as a blueprint).
 */
public class VariantsRevision {
    public static record Branches(Map<Branch, VariantCommit> commitOf) {}

    private final AbstractSPLRepository splRepo;
    private final AbstractVariantsRepository variantsRepo;
    private final Lazy<Branches> generate;
    private final Lazy<Optional<VariantsRevision>> evolve;

    /**
     * Creates a VariantsRevision.
     * @param splRepo The ISPLRepository from whose code variants should be generated.
     * @param variantsRepo The AbstractVariantsRepository to which variants should be generated and committed.
     * @param blueprint A blueprint giving instructions on how to generate the variants.
     * @param remainingHistory The remaining history that has to be generated after this revision was generated.
     *                         This list is used to return the next VariantsRevision to generate once the constructed
     *                         VariantsRevision was generated.
     */
    VariantsRevision(
            final AbstractSPLRepository splRepo,
            final AbstractVariantsRepository variantsRepo,
            final VariantsRevisionBlueprint blueprint,
            final ListHeadTailView<VariantsRevisionBlueprint> remainingHistory)
    {
        this.splRepo = splRepo;
        this.variantsRepo = variantsRepo;

        generate = blueprint.generateArtefactsFor(this);

        /*
         * evolve generates the current variants and then returns the next revision to generate
         * (i.e., it evolves into the next revision).
         * The next revision can only be generated though, after the current revision was generated.
         * Otherwise, it would be possible to access the next revision and generate it without having
         * generated the current revision first.
         * This would screw up the output git repository.
         * By chaining the production of the next revision with the generation of the current one, we force the users to
         * always generate the current variants before they can reason about the next revision (i.e., de.variantsync.studies.evolution step).
         * Thus, there is no room for bugs regarding the order of revisions.
         */
        evolve = generate.then(() ->
                remainingHistory.safehead().map(nextBlueprint ->
                        new VariantsRevision(splRepo, variantsRepo, nextBlueprint, remainingHistory.tail())));
    }

    /**
     * Generates all variants of this revision and commits them to respective branches.
     * @return The revision of the branches that were committed in this revision.
     */
    public Lazy<Branches> generate() {
        return generate;
    }

    /**
     * First, generates all variants (i.e., calls generate) and returns the next revision.
     * @return The next de.variantsync.studies.evolution step. Empty, when this was the last de.variantsync.studies.evolution step.
     */
    public Lazy<Optional<VariantsRevision>> evolve() {
        return evolve;
    }

    /**
     * Runs evolve and repeats that for all subsequent VariantsRevisions.
     * @return A lazy computation holding the entire generation process.
     */
    public Lazy<Unit> evolveAll() {
        // We know that the result of evolveAll is Lazy.of(Optional::empty) so we don't have to return that.
        return evolveAll(MonadTransformer.pure(this)).map(l -> Unit.Instance());
    }

    /**
     * This is a special fold to generate all revision starting from the given one.
     * The returned lazy will generate all VariantsRevisions once run.
     * @param firstRevision The revision that denotes the start of the history to generate.
     * @return A lazy that will generate all VariantsRevisions once run.
     */
    private static Lazy<Optional<VariantsRevision>> evolveAll(final Lazy<Optional<VariantsRevision>> firstRevision) {
        return MonadTransformer.bind(firstRevision, r  -> evolveAll(r.evolve()));
    }

    public AbstractSPLRepository getSPLRepo() {
        return splRepo;
    }

    public AbstractVariantsRepository getVariantsRepo() {
        return variantsRepo;
    }
}
