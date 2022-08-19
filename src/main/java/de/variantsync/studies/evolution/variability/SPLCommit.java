package de.variantsync.studies.evolution.variability;

import de.variantsync.studies.evolution.io.Resources;
import de.variantsync.studies.evolution.repository.Commit;
import de.variantsync.studies.evolution.util.Logger;
import de.variantsync.studies.evolution.util.functional.CachedValue;
import de.variantsync.studies.evolution.util.functional.Functional;
import de.variantsync.studies.evolution.util.functional.Lazy;
import de.variantsync.studies.evolution.util.functional.interfaces.FragileFunction;
import de.variantsync.studies.evolution.util.io.TypedPath;
import de.variantsync.studies.evolution.variability.pc.Artefact;
import de.variantsync.studies.evolution.variability.pc.EFilterOutcome;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SPLCommit extends Commit implements CachedValue {
    private final Lazy<Optional<String>> kernelHavenLog;
    private final Lazy<Optional<IFeatureModel>> featureModel;
    private final Lazy<Optional<Artefact>> presenceConditions;
    private final Lazy<Optional<String>> message;
    private final Lazy<Optional<Map<EFilterOutcome, Integer>>> filterCounts;
    private final Path dataDir;
    private final Path kernelHavenLogPath;
    private final Path featureModelPath;
    private final Path presenceConditionsPath;
    private final Path commitMessagePath;
    private final Path filterCountsPath;
    private SPLCommit[] parents;

    /**
     * Constructor for commits that should only contain information about the commit id.
     * TODO: Document params.
     *
     * @param commitId The id of the commit
     */
    public SPLCommit(final String commitId) {
        this(commitId, null, null, null, null, null, null);
    }

    public SPLCommit(
            final String commitId,
            final Path dataDir,
            final KernelHavenLogPath kernelHavenLog,
            final FeatureModelPath featureModel,
            final PresenceConditionPath presenceConditions,
            final CommitMessagePath commitMessage,
            final FilterCountsPath filterCounts) {
        super(commitId);
        this.dataDir = dataDir;

        this.kernelHavenLogPath = TypedPath.unwrapNullable(kernelHavenLog);
        this.featureModelPath = TypedPath.unwrapNullable(featureModel);
        this.presenceConditionsPath = TypedPath.unwrapNullable(presenceConditions);
        this.commitMessagePath = TypedPath.unwrapNullable(commitMessage);
        this.filterCountsPath = TypedPath.unwrapNullable(filterCounts);

        final FragileFunction<Path, Path, ZipException> tryUnzip = SPLCommit::tryUnzip;

        // Lazy loading of log file
        this.kernelHavenLog = Functional.mapFragileLazily(
                kernelHavenLogPath,
                tryUnzip.andThen(Files::readString),
                () -> "Was not able to load KernelHaven log for commit " + commitId);
        // Lazy loading of feature model
        this.featureModel = Functional.mapFragileLazily(
                featureModelPath,
                tryUnzip.andThen(path -> Resources.Instance().load(IFeatureModel.class, path)),
                () -> "Was not able to load feature model for id " + commitId);
        // Lazy loading of presence condition
        this.presenceConditions = Functional.mapFragileLazily(
                presenceConditionsPath,
                tryUnzip.andThen(path -> Resources.Instance().load(Artefact.class, path)),
                () -> "Was not able to load presence conditions for id " + commitId);
        // Lazy loading of commit message
        this.message = Functional.mapFragileLazily(
                commitMessagePath,
                tryUnzip.andThen(Files::readString),
                () -> "Was not able to load commit message for id " + commitId);
        // Lazy loading of filter counts
        this.filterCounts = Functional.mapFragileLazily(
                filterCountsPath,
                tryUnzip.andThen(path -> {
                    final Map<EFilterOutcome, Integer> countsMap = new HashMap<>();
                    Files.readAllLines(path).stream().map(l -> l.split(":")).forEach(parts -> countsMap.put(EFilterOutcome.valueOf(parts[0]), Integer.parseInt(parts[1].trim())));
                    return countsMap;
                }),
                () -> "Was not able to load filter counts for id " + commitId);
    }

    private static Path tryUnzip(final Path path) throws ZipException {
        if (!Files.exists(path)) {
            final Path zippedParent = Path.of(path.getParent() + ".zip");
            Logger.debug("Checking whether there is an archive " + zippedParent);
            if (Files.exists(zippedParent)) {
                Logger.debug("Archive " + zippedParent.getFileName() + " found.");
                try {
                    new ZipFile(zippedParent.toFile()).extractAll(String.valueOf(zippedParent.getParent()));
                } catch (final ZipException e) {
                    Logger.error("Was not able to unzip " + zippedParent, e);
                    throw e;
                }
            } else {
                Logger.warning("Path " + path + " does not exist and no ZIP file found.");
                return null;
            }
        } else {
            Logger.debug("Path " + path + " exists. No unzip required.");
        }
        return path;
    }

    /**
     * Clears all cached values by calling {@link Lazy#forget()} on all fields.
     */
    @Override
    public void forget() {
        kernelHavenLog.forget();
        featureModel.forget();
        presenceConditions.forget();
        message.forget();
        filterCounts.forget();
    }

    /**
     * Return the parents of this commit. As the dataset only contains the data about non-error commits,
     * not all <code>SPLCommit</code> objects are associated with their parents. Therefore, an <code>Optional</code> is returned.
     *
     * @return An <code>Optional</code> containing the <code>SPLCommit</code> objects of the parent commits, if there are any.
     */
    public Optional<SPLCommit[]> parents() {
        return Optional.ofNullable(parents);
    }

    public void setParents(final SPLCommit[] parents) {
        this.parents = parents;
    }

    /**
     * @return A Lazy that loads the commit message of this commit.
     */
    public Lazy<Optional<String>> message() {
        return message;
    }

    /**
     * @return A Lazy that loads the KernelHaven log associated with this commit.
     */
    public Lazy<Optional<String>> kernelHavenLog() {
        return kernelHavenLog;
    }

    /**
     * @return A Lazy that loads the feature model associated with this commit.
     */
    public Lazy<Optional<IFeatureModel>> featureModel() {
        return featureModel;
    }

    /**
     * @return A Lazy that loads the presence conditions associated with this commit.
     */
    public Lazy<Optional<Artefact>> presenceConditions() {
        return presenceConditions;
    }

    /**
     * @return A Lazy that loads the filter counts associated with this commit.
     */
    public Lazy<Optional<Map<EFilterOutcome, Integer>>> filterCounts() {
        return filterCounts;
    }

    public Path getCommitDataDirectory() {
        return dataDir;
    }

    public Path getKernelHavenLogPath() {
        return kernelHavenLogPath;
    }

    public Path getFeatureModelPath() {
        return featureModelPath;
    }

    public Path getPresenceConditionsPath() {
        return presenceConditionsPath;
    }

    public Path getCommitMessagePath() {
        return commitMessagePath;
    }

    public Path getFilterCountsPath() {
        return filterCountsPath;
    }

    public record KernelHavenLogPath(Path path) implements TypedPath {

    }

    public record FeatureModelPath(Path path) implements TypedPath {

    }

    public record PresenceConditionPath(Path path) implements TypedPath {

    }

    public record CommitMessagePath(Path path) implements TypedPath {

    }

    public record FilterCountsPath(Path path) implements TypedPath {

    }
}