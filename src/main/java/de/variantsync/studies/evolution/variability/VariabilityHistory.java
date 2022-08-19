package de.variantsync.studies.evolution.variability;

import de.variantsync.studies.evolution.util.list.NonEmptyList;
import de.variantsync.studies.evolution.variants.blueprints.VariantsRevisionBlueprint;
import de.variantsync.studies.evolution.variants.blueprints.VariantsRevisionFromErrorBlueprint;
import de.variantsync.studies.evolution.variants.blueprints.VariantsRevisionFromVariabilityBlueprint;
import de.variantsync.studies.evolution.variants.sampling.SamplingStrategy;

import java.util.ArrayList;

/**
 * An ordered list of coherent sub-histories of an AbstractVariabilityRepository.
 * The history of the analysed ISPLRepository may contain commits that cannot or should not be analysed
 * (e.g., error or merge commits).
 * Such commits do not allow for generating a single continuous history in the target AbstractVariantsRepository.
 * Thus, a VariabilityHistory to model contains all continuous sub-histories between error commits.
 */
public record VariabilityHistory(NonEmptyList<NonEmptyList<SPLCommit>> commitSequences) {
    /**
     * Default implementation to generate instructions (blueprints) for generating an AbstractVariantsRepository for this
     * VariabilityHistory.
     *
     * @return Blueprints to generate an AbstractVariantsRepository from.
     *         The list will contain exactly one VariantsRevisionFromVariabilityBlueprint for each VariabilityCommit
     *         in the sub-histories.
     *         After each continuous sub-history an explicit error commit will be introduced via a
     *         VariantsRevisionFromErrorBlueprint.
     */
    public NonEmptyList<VariantsRevisionBlueprint> toBlueprints(SamplingStrategy samplingStrategy)
    {
        final int lengthOfList = commitSequences.stream()
                .map(subHistory -> subHistory.size() + 1 /* because of error blueprints*/)
                .reduce(0, Integer::sum);
        final ArrayList<VariantsRevisionBlueprint> blueprints = new ArrayList<>(lengthOfList);
        VariantsRevisionFromVariabilityBlueprint lastVariabilityBlueprint = null;

        for (final NonEmptyList<SPLCommit> coherentSubHistory : commitSequences) {
            for (final SPLCommit splCommit : coherentSubHistory) {
                lastVariabilityBlueprint = new VariantsRevisionFromVariabilityBlueprint(splCommit, lastVariabilityBlueprint, samplingStrategy);
                blueprints.add(lastVariabilityBlueprint);
            }

            // We can be sure here that lastVariabilityBlueprint != null because coherentSubHistory is NonEmpty.
            blueprints.add(new VariantsRevisionFromErrorBlueprint(lastVariabilityBlueprint));
        }

        return new NonEmptyList<>(blueprints);
    }
}
