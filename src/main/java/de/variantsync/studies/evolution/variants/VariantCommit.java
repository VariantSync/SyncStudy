package de.variantsync.studies.evolution.variants;

import de.variantsync.studies.evolution.repository.Branch;
import de.variantsync.studies.evolution.repository.Commit;

/**
 * Represents a commit to an AbstractVariantsRepository.
 */
public class VariantCommit extends Commit {
    private final Branch branch;

    public VariantCommit(final String commitId, final Branch branch) {
        super(commitId);
        this.branch = branch;
    }

    public Branch branch() {
        return branch;
    }
}
