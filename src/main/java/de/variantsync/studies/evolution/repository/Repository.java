package de.variantsync.studies.evolution.repository;

import de.variantsync.studies.evolution.util.GitUtil;
import de.variantsync.studies.evolution.util.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public abstract class Repository<C extends Commit> implements IRepository<C> {
    private final Path path;
    private Git git;

    public Repository(final Path path) {
        this.path = path;
    }

    @Override
    public C checkoutCommit(final C c, boolean forced) throws GitAPIException, IOException {
        try {
            final C previous = getCurrentCommit();
            git().checkout().setName(c.id()).setForced(forced).call();
            return previous;
        } catch (final GitAPIException | IOException e) {
            Logger.error("Failed to checkout commit " + c, e);
            close();
            throw e;
        }
    }

    @Override
    public void checkoutBranch(final Branch branch) throws GitAPIException, IOException {
        try {
            git().checkout().setName(branch.name()).call();
        } catch (final GitAPIException | IOException e) {
            Logger.error("Failed to checkout branch " + branch.name(), e);
            close();
            throw e;
        }
    }

    @Override
    public C getCurrentCommit() throws IOException {
        try {
            return idToCommit(getCurrentCommitId());
        } catch (final IOException e) {
            Logger.error("Failed to get current commit.", e);
            close();
            throw e;
        }
    }

    public Optional<C> stashCreate(boolean includeUntracked) throws IOException, GitAPIException {
        try {
            RevCommit commit = git().stashCreate().setIncludeUntracked(includeUntracked).call();
            return Optional.ofNullable(commit == null ? null : idToCommit(commit.getName()));
        } catch (GitAPIException | IOException e) {
            Logger.error("Failed to stash changes.", e);
            close();
            throw e;
        }
    }

    public void dropStash(int refID) throws GitAPIException, IOException {
        try {
            git().stashDrop().setStashRef(refID).call();
        } catch (GitAPIException | IOException e) {
            Logger.error("Failed to drop stash with id " + refID, e);
            close();
            throw e;
        }
    }
    
    public void dropStash() throws GitAPIException, IOException {
        try {
            git().stashDrop().setAll(true).call();
        } catch (GitAPIException | IOException e) {
            Logger.error("Failed to drop stash.", e);
            close();
            throw e;
        }
    }

    public abstract C idToCommit(String id) throws IOException;


    private String getCurrentCommitId() throws IOException {
        final String commitId;

        try {
            final ObjectId head = git().getRepository().resolve(Constants.HEAD);
            commitId = ObjectId.toString(head);
            return commitId;
        } catch (final IOException e) {
            Logger.error("Failed to get current commit ID", e);
            close();
            throw e;
        }
    }

    @Override
    public Path getPath() {
        return path;
    }

    /**
     * Grants access to the underlying git object.
     *
     * @return The wrapped git object.
     * @throws IOException iff the repository could not be loaded for some reason.
     */
    public Git git() throws IOException {
        if (git == null) {
            git = GitUtil.loadGitRepo(path.toFile());
        }

        return git;
    }

    public void close() {
        if (git != null) {
            git.getRepository().close();
            git.close();
            git = null;
        }
    }
}
