package de.variantsync.studies.evolution.variants;

import de.variantsync.studies.evolution.repository.AbstractSPLRepository;
import de.variantsync.studies.evolution.repository.AbstractVariantsRepository;
import de.variantsync.studies.evolution.repository.Branch;
import de.variantsync.studies.evolution.util.Logger;
import de.variantsync.studies.evolution.util.list.ListHeadTailView;
import de.variantsync.studies.evolution.util.list.NonEmptyList;
import de.variantsync.studies.evolution.variants.blueprints.VariantsRevisionBlueprint;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Default implementation for AbstractVariantsRepository.
 * Given a history of VariantsRevisionBlueprints, this class will generate a git repository with variants from
 * the original ISPLRepository.
 */
public class VariantsRepository extends AbstractVariantsRepository {
    private Map<String, Branch> branchesByName;
    public final Optional<VariantsRevision> revision0;

    /**
     * Creates a new VariantsRepository that will generate to the given directory.
     * @param localPath The directory where the VariantsRepository is already located or should be located.
     * @param splRepo The ISPLRepository from whose artefacts, variants should be generated.
     * @param blueprintHistory A history of blueprints on how to generate the variants.
     *                         The variants revisions will be generated in the order the blueprints appear in the list.
     */
    public VariantsRepository(
            final Path localPath,
            final AbstractSPLRepository splRepo,
            final NonEmptyList<VariantsRevisionBlueprint> blueprintHistory)
    {
        super(localPath);
        parseRepoMetadata();

        final ListHeadTailView<VariantsRevisionBlueprint> history = filterExistingRevisions(new ListHeadTailView<>(blueprintHistory));
        revision0 = history.safehead().map(blueprint -> new VariantsRevision(splRepo, this, blueprint, history.tail()));
    }

    /**
     * Checks if the repository already exists and localPath.
     * If it does, reads all relevant metadata from it, such as the currently checked out commit and the existing branches.
     */
    private void parseRepoMetadata() {
        // TODO: Implement Issue 11 here.
        // If we start repo generation from the beginning, nothing has to be done, thus we have no branches in the beginning:
        branchesByName = new HashMap<>();
    }

    /**
     * Discards all blueprints from the given history for which revisions where already generated.
     * This means, this removes blueprints from the head of the given history until the current head was not already generated.
     * @param history The history to filter already generated revisions from.
     * @return A list of blueprints that still have to be generated.
     */
    private ListHeadTailView<VariantsRevisionBlueprint> filterExistingRevisions(final ListHeadTailView<VariantsRevisionBlueprint> history) {
        // TODO: Implement Issue 11 here.
        // E.g. if we see that the first blueprint was already processed then we could return history.tail().
        // 1.) Find the last valid revision.
        // 2.) If there were commits after that revision they were incomplete (e.g., we generated some but not all variants).
        //     Thus, delete all changes after the last valid revision. This might require manipulating the git history.
        // 3.) Cut the first n blueprints in `history` such that in the end `history.head()` is the first blueprint whose revision has to be generated.
        //     (The blueprint before `history.head()` would be the blueprint that produced the last valid revision from 1).
        return history;
    }

    @Override
    public Optional<VariantsRevision> getStartRevision() {
        return revision0;
    }

    @Override
    public Branch getBranchByName(final String name) {
        return branchesByName.computeIfAbsent(name, Branch::new);
    }

    // TODO: Make sure that the new VariantCommit uses the correct branch
    //  (At the moment it uses the current branch because it is only used for getCurrentCommit)

    @Override
    public VariantCommit idToCommit(final String id) throws IOException {
        try {
            final Branch branch = getCurrentBranch();
            return new VariantCommit(id, branch);
        } catch(final IOException e){
            Logger.error("Failed get variant commit for id " + id, e);
            close();
            throw e;
        }
    }

    // TODO: Make sure that commit() behaves correctly.
    // At the moment, it returns null in the case of an empty commit, and the VariantCommit otherwise (as described in AbstractVariantRepository)
    // But in VariantsRevisionFromErrorBluePrint, an empty commit would lead to throwing a RuntimeException. Is that wanted?
    // If empty commits should be possible, commit() could be adapted to allow those. (referring to TODO note in VariantsRevisionFromErrorBlueprint)

    @Override
    public Optional<VariantCommit> commit(final String message) throws GitAPIException, IOException {
        Optional<VariantCommit> result = Optional.empty();

        try {
            final VariantCommit commit= commit(".", message);
            if(commit != null){
                result = Optional.of(commit);
            }
        } catch (final IOException | GitAPIException e) {
            Logger.error("Failed to commit with message: " + message, e);
            close();
            throw e;
        }

        return result;
    }

    private Branch getCurrentBranch() throws IOException {
        try {
            final String branch = git().getRepository().getBranch();
            return new Branch(branch);
        } catch(final IOException e){
            Logger.error("Failed to get current branch", e);
            throw e;
        }
    }

    private VariantCommit commit(final String pattern, final String message) throws IOException, GitAPIException {
        git().add().addFilepattern(pattern).call();

        try{
            final RevCommit rev = git().commit().setMessage(message).setAllowEmpty(false).call();
            final String commitId = rev.getId().toString();
            return idToCommit(commitId);
        } catch(final EmptyCommitException e){
            return null;
        }
    }
}
