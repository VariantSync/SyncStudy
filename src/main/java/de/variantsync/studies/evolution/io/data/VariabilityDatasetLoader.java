package de.variantsync.studies.evolution.io.data;

import de.variantsync.studies.evolution.io.ResourceLoader;
import de.variantsync.studies.evolution.io.TextIO;
import de.variantsync.studies.evolution.util.Logger;
import de.variantsync.studies.evolution.util.functional.Result;
import de.variantsync.studies.evolution.variability.SPLCommit;
import de.variantsync.studies.evolution.variability.SPLCommit.*;
import de.variantsync.studies.evolution.variability.VariabilityDataset;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class VariabilityDatasetLoader implements ResourceLoader<VariabilityDataset> {
    private final static String SUCCESS_COMMIT_FILE = "SUCCESS_COMMITS.txt";
    private final static String ERROR_COMMIT_FILE = "ERROR_COMMITS.txt";
    private final static String PARTIAL_SUCCESS_COMMIT_FILE = "PARTIAL_SUCCESS_COMMITS.txt";
    private final static String FEATURE_MODEL_FILE = "variability-model.json";
    private final static String PRESENCE_CONDITIONS_FILE = "code-variability.spl.csv";
    private final static String PARENTS_FILE = "PARENTS.txt";
    private final static String MESSAGE_FILE = "MESSAGE.txt";
    private static final String DATA_DIR_NAME = "data";
    private static final String LOG_DIR_NAME = "log";
    private static final String FILTER_COUNTS_FILE = "FILTERED.txt";

    @Override
    public boolean canLoad(final Path p) {
        try {
            return Files.list(p)
                    .map(Path::toFile)
                    .anyMatch(f -> {
                        final String name = f.getName();
                        return name.equals(SUCCESS_COMMIT_FILE) || name.equals(ERROR_COMMIT_FILE) || name.equals(PARTIAL_SUCCESS_COMMIT_FILE);
                    });
        } catch (final IOException e) {
            Logger.error("Was not able to check the file(s) under " + p, e);
            return false;
        }
    }

    /**
     * Load a dataset containing the extracted variability information of a SPL.
     * <p>
     * The given path should point to the root of the dataset's directory. Assume that the given path to the dataset is
     * `/home/alice/data/extraction-results`. Then, the structure of `extraction-results` should look as follows:
     * <p></p>
     * extraction-results/
     * <p>|- log/</p>
     * <p>|- data/</p>
     * <p>|- SUCCESS_COMMITS.txt</p>
     *
     * @param p path to the root directory of the dataset
     * @return The fully-loaded dataset if loading is successful, otherwise an Exception.
     */
    @Override
    public Result<VariabilityDataset, Exception> load(final Path p) {
        // Read the metadata
        List<String> successIds = new ArrayList<>();
        List<String> errorIds = new ArrayList<>();
        List<String> partialSuccessIds = new ArrayList<>();

        Logger.status("Started loading of dataset under " + p);
        final Path successFile = p.resolve(SUCCESS_COMMIT_FILE);
        if (Files.exists(successFile)) {
            successIds = TextIO.readLinesTrimmed(successFile).expect("Success-commit file exists but could not be loaded.");
        }

        final Path errorFile = p.resolve(ERROR_COMMIT_FILE);
        if (Files.exists(errorFile)) {
            errorIds = TextIO.readLinesTrimmed(errorFile).expect("Error-commit file exists but could not be loaded.");
        }

        final Path partialSuccessFile = p.resolve(PARTIAL_SUCCESS_COMMIT_FILE);
        if (Files.exists(partialSuccessFile)) {
            partialSuccessIds = TextIO.readLinesTrimmed(partialSuccessFile).expect("Partial-success-commit file exists but could not be loaded.");
        }

        Logger.info("Read commit ids.");

        // Create SPLCommit objects for each commit
        final List<SPLCommit> successCommits = initializeSPLCommits(p, successIds);
        final List<SPLCommit> errorCommits = initializeSPLCommits(p, errorIds);
        final List<SPLCommit> partialSuccessCommits = initializeSPLCommits(p, partialSuccessIds);
        Logger.info("Initialized SPL commits.");

        // Retrieve the SPLCommit objects for the parents of each commit
        final Map<String, SPLCommit> idToCommitMap = new HashMap<>();
        successCommits.forEach(c -> idToCommitMap.put(c.id(), c));
        Logger.info("Mapped success commits.");
        errorCommits.forEach(c -> idToCommitMap.put(c.id(), c));
        Logger.info("Mapped error commits.");
        partialSuccessCommits.forEach(c -> idToCommitMap.put(c.id(), c));
        Logger.info("Mapped partial success commits.");

        Logger.info("Mapping commits to parents...");
        for (final Map.Entry<String, SPLCommit> entry : idToCommitMap.entrySet()) {
            final String[] parentIds = loadParentIds(p, entry.getKey());
            if (parentIds == null || parentIds.length == 0) {
                entry.getValue().setParents(null);
            } else {
                entry.getValue().setParents(Arrays.stream(parentIds).map(idToCommitMap::get).toArray(SPLCommit[]::new));
            }
        }
        Logger.info("Done.");
        Logger.info("Found a total of " + idToCommitMap.size() + " commits.");
        // Return the fully-loaded dataset
        return Result.Success(new VariabilityDataset(successCommits, errorCommits, partialSuccessCommits));
    }

    private List<SPLCommit> initializeSPLCommits(final Path p, final List<String> commitIds) {
        final List<SPLCommit> splCommits = new ArrayList<>(commitIds.size());
        for (final String id : commitIds) {
            // Initialize a SPLCommit object for each commit id by resolving all paths to files with data about the commit
            final SPLCommit splCommit = new SPLCommit(
                    id, 
                    resolvePathToCommitOutputDir(p, id), 
                    resolvePathToLogFile(p, id), 
                    resolvePathToFeatureModel(p, id), 
                    resolvePathToPresenceConditions(p, id), 
                    resolvePathToMessageFile(p, id), 
                    resolvePathToFilterCountsFile(p, id));
            splCommits.add(splCommit);
        }
        return splCommits;
    }

    private Path resolvePathToCommitOutputDir(final Path rootDir, final String commitId) {
        return rootDir.resolve(DATA_DIR_NAME).resolve(commitId);
    }

    private FeatureModelPath resolvePathToFeatureModel(final Path rootDir, final String commitId) {
        final Path p = resolvePathToCommitOutputDir(rootDir, commitId).resolve(FEATURE_MODEL_FILE);
        return new FeatureModelPath(p);
    }

    private PresenceConditionPath resolvePathToPresenceConditions(final Path rootDir, final String commitId) {
        final Path p = resolvePathToCommitOutputDir(rootDir, commitId).resolve(PRESENCE_CONDITIONS_FILE);
        return new PresenceConditionPath(p);
    }

    private Path resolvePathToParentsFile(final Path rootDir, final String commitId) {
        return resolvePathToCommitOutputDir(rootDir, commitId).resolve(PARENTS_FILE);
    }

    private CommitMessagePath resolvePathToMessageFile(final Path rootDir, final String commitId) {
        final Path p = resolvePathToCommitOutputDir(rootDir, commitId).resolve(MESSAGE_FILE);
        return new CommitMessagePath(p);
    }

    private KernelHavenLogPath resolvePathToLogFile(final Path rootDir, final String commitId) {
        final Path p = rootDir.resolve(LOG_DIR_NAME).resolve(commitId + ".log");
        return new KernelHavenLogPath(p);
    }

    private FilterCountsPath resolvePathToFilterCountsFile(final Path rootDir, final String commitId) {
        final Path p = resolvePathToCommitOutputDir(rootDir, commitId).resolve(FILTER_COUNTS_FILE);
        return new FilterCountsPath(p);
    }

    private String[] loadParentIds(final Path p, final String commitId) {
        final Path parentsFile = resolvePathToParentsFile(p, commitId);
        if (!Files.exists(parentsFile)) {
            Logger.debug("No PARENTS.txt found, checking archive....");
            final File zipFile = new File(parentsFile.getParent() + ".zip");
            Logger.debug("Checking ZIP file " + zipFile);
            if (zipFile.exists()) {
                try {
                    Logger.debug("Unzipping PARENTS.txt");
                    new ZipFile(zipFile).extractFile(commitId + "/PARENTS.txt", String.valueOf(resolvePathToCommitOutputDir(p, commitId).getParent()));
                } catch (final ZipException e) {
                    // Not all commits have a ZIP file and not all commits with a ZIP file have a PARENTS.txt. So this is
                    // an expected exception
                    Logger.debug("Was not able to unzip commit data." + e.getMessage());
                }
            }
        }
        if (Files.exists(parentsFile)) {
            try {
                return Files.readString(parentsFile).split("\\s");
            } catch (final IOException e) {
                Logger.error("Was not able to load PARENTS.txt " + parentsFile + " even though it exists:", e);
                return null;
            }
        }
        return null;
    }
}