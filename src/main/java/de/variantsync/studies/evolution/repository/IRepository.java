package de.variantsync.studies.evolution.repository;

import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Models a repository of a version control system such as Git.
 * @param <C> Type of commits of this repository.
 */
public interface IRepository<C extends Commit> extends AutoCloseable {
    /**
     * Check out the given commit in the local copy of the repository.
     * (Method is designed for operating on a single branch only).
     * @param c The commit to check out. Will be the @see getCurrentCommitAfterwards.
     * @return The commit that was previously the current commit.
     */
    default C checkoutCommit(C c) throws GitAPIException, IOException {
        return checkoutCommit(c, false);
    }

    /**
     * Check out the given commit in the local copy of the repository.
     * (Method is designed for operating on a single branch only).
     * @param c The commit to check out. Will be the @see getCurrentCommitAfterwards.
     * @param forced If true, allow a checkout even if the workingtree or index differs from HEAD.
     * @return The commit that was previously the current commit.
     */
    C checkoutCommit(C c, boolean forced) throws GitAPIException, IOException;

    /**
     * Check out the given branch (`git checkout <branch name>`).
     * Afterwards, the contents of the given branch will be on disk.
     * @param branch The branch to check out.
     */
    void checkoutBranch(Branch branch) throws GitAPIException, IOException;

    /**
     * @return The commit HEAD is currently pointing to (i.e., the commit whose state of the repository we see on disk currently).
     */
    C getCurrentCommit() throws IOException;

    /**
     * Stash changes in the working directory and index in a commit.
     * @param includeUntracked Whether to include untracked files in the stash.
     * @return The current commit after stashing
     */
    Optional<C> stashCreate(boolean includeUntracked) throws IOException, GitAPIException;

    /**
     * Delete a stashed commit by reference
     * @param refID the stash reference to drop (0-based)
     */
    void dropStash(int refID) throws GitAPIException, IOException;

    /**
     * Drop all stashed commits.
     */
    void dropStash() throws GitAPIException, IOException;

    /**
     * @return The path in the file system to a local copy of this repository.
     */
    Path getPath();
}
