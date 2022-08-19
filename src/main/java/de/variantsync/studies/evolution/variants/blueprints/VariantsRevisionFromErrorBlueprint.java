package de.variantsync.studies.evolution.variants.blueprints;

import de.variantsync.studies.evolution.feature.Variant;
import de.variantsync.studies.evolution.feature.sampling.Sample;
import de.variantsync.studies.evolution.repository.AbstractVariantsRepository;
import de.variantsync.studies.evolution.repository.Branch;
import de.variantsync.studies.evolution.util.functional.Lazy;
import de.variantsync.studies.evolution.variants.VariantCommit;
import de.variantsync.studies.evolution.variants.VariantsRevision;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A blueprint that produces an artificial commit on each current variant.
 * These commits are intended to denote ends of continuous commit histories from
 * the original ISPLRepository.
 * For instance, when there would be a single commit in the mid of the history of the ISPLRepository,
 * that could not be parsed (we have no feature model and no presence conditions), this commit would split
 * the history of the ISPLRepository we can model in half - all commits before that error commit and all commits afterwards.
 * A VariantsRevisionFromErrorBlueprint is devoted to model such an error commit on the AbstractVariantsRepository to mark
 * the end of continuous analysable history.
 */
public class VariantsRevisionFromErrorBlueprint extends VariantsRevisionBlueprint {
    public final String COMMIT_MESSAGE = "SUB_HISTORY_END";

    /**
     * Creates a new error blueprint for a VariantsRevision.
     * An error revision has always to be preceded by a normal blueprint.
     * (Otherwise the error blueprint would be pointless).
     * @param predecessor The blueprint for the previous revision. Cannot be null.
     */
    public VariantsRevisionFromErrorBlueprint(final VariantsRevisionFromVariabilityBlueprint predecessor) {
        super(predecessor);
        Objects.requireNonNull(predecessor);
    }

    @Override
    protected Lazy<Sample> computeSample() {
        // We don't have any variability information but instead want to introduce an artificial error commit.
        // Thus, we just have to operate on the variants already present.
        // We know that we have a predecessor because it is necessary to have one in the constructor.
        return getPredecessor().orElseThrow().getSample();
    }

    @Override
    public Lazy<VariantsRevision.Branches> generateArtefactsFor(final VariantsRevision revision) {
        return getSample().map(sample -> {
            final AbstractVariantsRepository variantsRepo = revision.getVariantsRepo();
            final Map<Branch, VariantCommit> commits = new HashMap<>(sample.size());

            for (final Variant variant : sample.variants()) {
                final Branch branch = variantsRepo.getBranchByName(variant.getName());
                final Optional<VariantCommit> variantCommit;

                try {
                    variantsRepo.checkoutBranch(branch);
                    // TODO: We cannot commit no changes. So we have to change something. What could that be?
                    //       A simple text file might really be all we need here. Either an empty file or a file with the hashes of the associated commits.
                    //       Idea: Remember in the error blueprint the number of the sub-history it ends. As initial commit to each branch,
                    //             add that file and change its content to the remembered number here.
                    variantCommit = variantsRepo.commit(COMMIT_MESSAGE);
                } catch (final GitAPIException | IOException e) {
                    throw new RuntimeException("Failed when using the VariantsRepository.");
                }

                if (variantCommit.isPresent()) {
                    commits.put(branch, variantCommit.get());
                } else {
                    throw new RuntimeException("Failed to add error commit.");
                }
            }

            return new VariantsRevision.Branches(commits);
        });
    }
}
