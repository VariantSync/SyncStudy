package de.variantsync.studies.evolution.repository;

import de.variantsync.studies.evolution.variants.VariantCommit;
import de.variantsync.studies.evolution.variants.VariantsRevision;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Repository that mimics the de.variantsync.studies.evolution of an ISPLRepository in terms of variants instead of an integrated code base.
 */
public abstract class AbstractVariantsRepository extends Repository<VariantCommit> {

    public AbstractVariantsRepository(final Path path){
        super(path);
    }
    /**
     * Commits the current state of the repository with the given message.
     * @param message Message for the commit to make.
     * @return A handle for the commit that was just created. Returns empty if there were no changes to commit.
     */
    public abstract Optional<VariantCommit> commit(String message) throws GitAPIException, IOException;

    /**
     * Returns a branch handle form a branches name.
     * @param name Name of the branch to obtained metadata of.
     * @return Branch object representing the branch with the given name.
     */
    public abstract Branch getBranchByName(String name);

    /**
     * @return The first revision of variants of this repository. Empty, iff there is nothing to generate.
     */
    public abstract Optional<VariantsRevision> getStartRevision();
}
