package org.variantsync.studies.evolution.simulation.experiment;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureModelElement;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.variantsync.functjonal.Result;
import org.variantsync.functjonal.list.NonEmptyList;
import org.variantsync.studies.evolution.simulation.diff.DiffParser;
import org.variantsync.studies.evolution.simulation.diff.components.FileDiff;
import org.variantsync.studies.evolution.simulation.diff.components.FineDiff;
import org.variantsync.studies.evolution.simulation.diff.components.OriginalDiff;
import org.variantsync.studies.evolution.simulation.diff.filter.CachedPCBasedFilter;
import org.variantsync.studies.evolution.simulation.diff.filter.IFileDiffFilter;
import org.variantsync.studies.evolution.simulation.diff.filter.ILineFilter;
import org.variantsync.studies.evolution.simulation.diff.splitting.DefaultContextProvider;
import org.variantsync.studies.evolution.simulation.diff.splitting.DiffSplitter;
import org.variantsync.studies.evolution.simulation.diff.splitting.IContextProvider;
import org.variantsync.studies.evolution.simulation.error.Panic;
import org.variantsync.studies.evolution.simulation.error.ShellException;
import org.variantsync.studies.evolution.simulation.shell.*;
import org.variantsync.vevos.simulation.feature.Variant;
import org.variantsync.vevos.simulation.feature.config.FeatureIDEConfiguration;
import org.variantsync.vevos.simulation.feature.sampling.FeatureIDESampler;
import org.variantsync.vevos.simulation.feature.sampling.Sample;
import org.variantsync.vevos.simulation.feature.sampling.Sampler;
import org.variantsync.vevos.simulation.io.Resources;
import org.variantsync.vevos.simulation.io.data.VariabilityDatasetLoader;
import org.variantsync.vevos.simulation.repository.SPLRepository;
import org.variantsync.vevos.simulation.util.LogLevel;
import org.variantsync.vevos.simulation.util.Logger;
import org.variantsync.vevos.simulation.util.fide.FeatureModelUtils;
import org.variantsync.vevos.simulation.util.io.CaseSensitivePath;
import org.variantsync.vevos.simulation.variability.SPLCommit;
import org.variantsync.vevos.simulation.variability.VariabilityDataset;
import org.variantsync.vevos.simulation.variability.VariabilityHistory;
import org.variantsync.vevos.simulation.variability.pc.Artefact;
import org.variantsync.vevos.simulation.variability.pc.SourceCodeFile;
import org.variantsync.vevos.simulation.variability.pc.groundtruth.GroundTruth;
import org.variantsync.vevos.simulation.variability.pc.options.ArtefactFilter;
import org.variantsync.vevos.simulation.variability.pc.options.VariantGenerationOptions;
import org.variantsync.vevos.simulation.variability.sequenceextraction.Domino;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.variantsync.vevos.simulation.VEVOS.Initialize;

/**
 * This class contains the core workflow of our study as described in our paper.
 */
public class SynchronizationStudy {
    // Working directory
    protected final Path workDir;
    // Directory for storing results
    protected final Path resultsDir;
    // Directory for saving debug files
    protected final Path debugDir;
    // Flag whether the study is executed in debug mode
    protected final boolean inDebug;
    // Path to the result file
    protected final Path resultFile;
    // Path to the first copy of the SPL. We require copy to consider different versions
    protected final Path splCopyA;
    // Path to the second copy of the SPL
    protected final Path splCopyB;
    // Path to the directory containing the variants generated for the parent commit
    protected final CaseSensitivePath variantsDirV0;
    // Path to the directory containing the variants generated for the child commit
    protected final CaseSensitivePath variantsDirV1;
    // The directory to which patches are applied. A copy of the target variant is created in this directory.
    protected final Path patchDir;
    // Path to the patch file containing the patches without filtering
    protected final Path normalPatchFile;
    // Path to the patch file containing the patches with filtering
    protected final Path filteredPatchFile;
    // Path to the rejects file created by patching without filtering
    protected final Path rejectsNormalFile;
    // Path to the rejects file created by patching with filtering
    protected final Path rejectsFilteredFile;
    // ShellExecutor for executing shell commands
    protected final ShellExecutor shell;
    // Configured log level
    protected final LogLevel logLevel;
    // Number of random repetitions for each commit pair. New variants are sampled for each repetition
    protected final int randomRepeats;
    // Number of sampled variants
    protected final int numVariants;
    // The id of the first run that is to be executed. Required for the short installation validation.
    protected final int startID;
    // Name of the experimental subject
    protected final String experimentalSubject;
    // Path to the subject's sources
    protected final Path splRepositoryPath;
    // Path to the ground truth dataset
    protected final Path datasetPath;
    // Evolution history of the subject
    protected final VariabilityHistory history;
    // The variant sampler
    private final Sampler sampler;
    // The feature model for which variants are sampled
    private IFeatureModel currentModel;
    // The considered parent commit
    private SPLCommit commitV0Current;
    // The considered child commit
    private SPLCommit commitV1Current;

    /**
     * Initialize the study from the given configuration
     *
     * @param config The study's configuration
     */
    public SynchronizationStudy(final StudyConfiguration config) {
        // Initialize the VEVOS Simulation library
        Initialize();
        final Path mainDir = Path.of(config.EXPERIMENT_DIR_MAIN());
        try {
            if (mainDir.toFile().mkdirs()) {
                Logger.status("Created main directory " + mainDir);
            }
            workDir = Files.createTempDirectory(mainDir, "workdir");
        } catch (final IOException e) {
            Logger.error("Was not able to initialize workdir", e);
            throw new UncheckedIOException(e);
        }
        resultsDir = Path.of(config.EXPERIMENT_DIR_RESULTS());
        debugDir = resultsDir.resolve("DEBUG");
        inDebug = config.EXPERIMENT_DEBUG();
        resultFile = resultsDir.resolve("results.txt");
        splRepositoryPath = Path.of(config.EXPERIMENT_DIR_SPL());
        datasetPath = Path.of(config.EXPERIMENT_DIR_DATASET());
        splCopyA = workDir.resolve("SPL-A");
        splCopyB = workDir.resolve("SPL-B");
        variantsDirV0 = new CaseSensitivePath(workDir.resolve("V0Variants"));
        variantsDirV1 = new CaseSensitivePath(workDir.resolve("V1Variants"));
        patchDir = workDir.resolve("TARGET/V0");
        normalPatchFile = workDir.resolve("patch.txt");
        filteredPatchFile = workDir.resolve("filtered-patch.txt");
        rejectsNormalFile = workDir.resolve("rejects-normal.txt");
        rejectsFilteredFile = workDir.resolve("rejects-filtered.txt");
        shell = new ShellExecutor(Logger::debug, Logger::debug, workDir);
        logLevel = config.EXPERIMENT_LOGGER_LEVEL();
        randomRepeats = config.EXPERIMENT_REPEATS();
        numVariants = config.EXPERIMENT_VARIANT_COUNT();
        startID = config.EXPERIMENT_START_ID();
        experimentalSubject = config.EXPERIMENT_SUBJECT();
        sampler = FeatureIDESampler.CreateRandomSampler(numVariants);

        history = init();
    }

    // Read a rejects file
    @Nullable
    private static OriginalDiff readRejects(final Path rejectFile) {
        OriginalDiff rejectsDiff = null;
        if (Files.exists(rejectFile)) {
            try {
                final List<String> rejects = Files.readAllLines(rejectFile);
                rejectsDiff = DiffParser.toOriginalDiff(rejects);
            } catch (final IOException e) {
                Logger.error("Was not able to read rejects file.", e);
            }
        }
        return rejectsDiff;
    }

    /**
     * Execute the study.
     */
    public void run() {
        // Initialize the SPL repositories for different versions
        Logger.status("Initializing SPL repos.");
        SPLRepository parentRepo = new SPLRepository(splCopyA);
        SPLRepository childRepo = new SPLRepository(splCopyB);

        // For each pair
        Logger.status("Starting diffing and patching...");
        long runID = 0;
        int pairCount = 0;
        final long historySize = history.commitSequences().stream().mapToLong(Collection::size).sum();
        Logger.status("There are " + historySize + " commit pairs to work on.");
        for (final NonEmptyList<SPLCommit> relatedCommits : history.commitSequences()) {
            // Increase one extra time for the first parent in the sequence
            pairCount++;
            SPLCommit parentCommit;
            SPLCommit childCommit = relatedCommits.get(0);
            for (int childID = 1; childID < relatedCommits.size(); childID++) {
                // Skip pairs until the start ID has been reached.
                if (pairCount < startID) {
                    Logger.info("Skipped pair " + pairCount);
                    pairCount++;
                    continue;
                }
                // The old child becomes the next parent
                parentCommit = childCommit;
                // The next descendant is selected as new child
                childCommit = relatedCommits.get(childID);

                final SimpleFileFilter fileFilter = splRepoPreparation(parentRepo, childRepo, parentCommit, childCommit);

                // While more random configurations to consider
                for (int i = 0; i < randomRepeats; i++) {
                    Logger.status("Starting repetition " + (i + 1) + " of " + randomRepeats + " with " + numVariants + " variants.");
                    if (inDebug && Files.exists(debugDir)) {
                        shell.execute(new RmCommand(debugDir).recursive());
                    }
                    if (inDebug && debugDir.toFile().mkdirs()) {
                        Logger.debug("Created Debug directory.");
                    }

                    // Sample set of random variants
                    Logger.status("Sampling next set of variants...");
                    final Sample sample = sample(parentCommit, childCommit);
                    Logger.status("Done. Sampled " + sample.variants().size() + " variants.");

                    if (Files.exists(variantsDirV0.path())) {
                        Logger.status("Cleaning variants dir V0.");
                        shell.execute(new RmCommand(variantsDirV0.path()).recursive());
                    }
                    if (Files.exists(variantsDirV1.path())) {
                        Logger.status("Cleaning variants dir V1.");
                        shell.execute(new RmCommand(variantsDirV1.path()).recursive());
                    }

                    // Write information about the commits
                    if (inDebug) {
                        try {
                            final var v0PCs = parentCommit.presenceConditions().run();
                            if (v0PCs.isPresent()) {
                                Resources.Instance().write(Artefact.class, v0PCs.get(), debugDir.resolve("V0.spl.csv"));
                            }

                            final var v1PCs = childCommit.presenceConditions().run();
                            if (v1PCs.isPresent()) {
                                Resources.Instance().write(Artefact.class, v1PCs.get(), debugDir.resolve("V1.spl.csv"));
                            }
                        } catch (final Resources.ResourceIOException e) {
                            panic("Was not able to write PCs", e);
                        }
                    }

                    // Generate the randomly selected variants at both versions
                    final Map<Variant, GroundTruth> groundTruthV0 = new HashMap<>();
                    final Map<Variant, GroundTruth> groundTruthV1 = new HashMap<>();
                    Logger.status("Generating variants...");
                    for (final Variant variant : sample.variants()) {
                        generateVariant(parentCommit, childCommit, groundTruthV0, groundTruthV1, variant, fileFilter);
                    }
                    Logger.status("Done.");

                    // Select each variant once as source
                    for (Variant source : sample.variants()) {
                        Logger.status("Starting diff application for source variant " + source.getName());
                        if (Files.exists(normalPatchFile)) {
                            Logger.status("Cleaning old patch file " + normalPatchFile);
                            shell.execute(new RmCommand(normalPatchFile));
                        }
                        // Apply diff to both versions of source variant
                        Logger.info("Diffing source...");
                        final OriginalDiff originalDiff = getOriginalDiff(variantsDirV0.path().resolve(source.getName()), variantsDirV1.path().resolve(source.getName()));
                        if (originalDiff.isEmpty()) {
                            // There was no change to this variant, so we can skip it as source
                            Logger.status("Skipping " + source.getName() + " as diff source. Diff is empty.");
                            continue;
                        } else if (inDebug) {
                            try {
                                Files.write(debugDir.resolve("diff.txt"), originalDiff.toLines());
                            } catch (final IOException e) {
                                Logger.error("Was not able to save diff", e);
                            }
                        }
                        Logger.info("Converting diff...");
                        // Convert the original diff into a fine diff
                        final FineDiff normalPatch = getFineDiff(originalDiff);
                        saveDiff(normalPatch, normalPatchFile);
                        Logger.info("Saved fine diff.");

                        // For each target variant,
                        Logger.status("Starting patch application for source variant " + source.getName());
                        for (final Variant target : sample.variants()) {
                            if (target == source) {
                                continue;
                            }
                            runID++;
                            Logger.status(source.getName() + " --patch--> " + target.getName());
                            final Path pathToTarget = variantsDirV0.path().resolve(target.getName());
                            final Path pathToExpectedResult = variantsDirV1.path().resolve(target.getName());
                            final FineDiff evolutionDiff = getFineDiff(getOriginalDiff(pathToTarget, pathToExpectedResult));
                            if (inDebug) {
                                saveDiff(evolutionDiff, debugDir.resolve("evolutionDiff.txt"));
                            }

                            /* Application of patches without knowledge about features */
                            Logger.info("Applying patch without knowledge about features...");
                            // Apply the fine diff to the target variant
                            final Set<String> skippedNormal = applyPatch(normalPatchFile, pathToTarget, rejectsNormalFile);
                            // Evaluate the patch result
                            final FineDiff actualVsExpectedNormal = getActualVsExpected(pathToExpectedResult);
                            final OriginalDiff rejectsNormal = readRejects(rejectsNormalFile);

                            /* Application of patches with knowledge about PC of edit only */
                            Logger.info("Applying patch with knowledge about edits' PCs...");
                            // Create target variant specific patch that respects PCs
                            final FineDiff filteredPatch = getFilteredDiff(originalDiff, groundTruthV0.get(source).variant(), groundTruthV1.get(source).variant(), target, variantsDirV0.path(), variantsDirV1.path());
                            final boolean emptyPatch = filteredPatch.content().isEmpty();
                            saveDiff(filteredPatch, filteredPatchFile);
                            // Apply the patch
                            final Set<String> skippedFiltered = applyPatch(filteredPatchFile, pathToTarget, rejectsFilteredFile, emptyPatch);
                            // Evaluate the result
                            final FineDiff actualVsExpectedFiltered = getActualVsExpected(pathToExpectedResult);
                            final OriginalDiff rejectsFiltered = readRejects(rejectsFilteredFile);

                            /* Result Evaluation */
                            final PatchOutcome patchOutcome = ResultAnalysis.processOutcome(
                                    experimentalSubject,
                                    runID,
                                    source.getName(),
                                    target.getName(),
                                    parentCommit, childCommit,
                                    normalPatch, filteredPatch,
                                    actualVsExpectedNormal, actualVsExpectedFiltered,
                                    rejectsNormal, rejectsFiltered,
                                    evolutionDiff,
                                    skippedNormal,
                                    skippedFiltered);

                            try {
                                patchOutcome.writeAsJSON(resultFile, true);
                            } catch (final IOException e) {
                                Logger.error("Was not able to write filtered patch result file for run " + runID, e);
                            }

                            Logger.info("Finished patching for source " + source.getName() + " and target " + target.getName());
                        }
                    }
                }
                pairCount++;
                Logger.status(String.format("Finished commit pair %d of %d.%n", pairCount, historySize));

                // Free memory of parentCommit
                parentCommit.forget();
            }
            // Free memory of commit V1
            childCommit.forget();
        }
        Logger.status("All done.");
    }

    /**
     * Get the difference between the target variant after patching and the target variant in the next de.variantsync.studies.evolution step.
     * Then, filter all differences that do not belong to the source variant and could have therefore not been synchronized
     * in any case.
     */
    private FineDiff getActualVsExpected(final Path pathToExpectedResult) {
        final OriginalDiff resultDiff = getOriginalDiff(patchDir, pathToExpectedResult);
        if (inDebug) {
            try {
                Files.write(debugDir.resolve("resultDiffOriginal.txt"), resultDiff.toLines());
            } catch (final IOException e) {
                Logger.error("Was not able to save resultDiffOriginal", e);
            }
        }
        FineDiff fineResult = getFineDiff(resultDiff);
        if (inDebug) {
            try {
                Files.write(debugDir.resolve("resultDiffFine.txt"), fineResult.toLines());
            } catch (final IOException e) {
                Logger.error("Was not able to save resultDiffFiltered", e);
            }
        }

        return fineResult;
    }

    /**
     * Randomly sample a set of variants valid in both commits.
     *
     * @param commitV0 The id of the parent commit
     * @param commitV1 The id of the child commit
     * @return The sampled variants
     */
    protected Sample sample(final SPLCommit commitV0, final SPLCommit commitV1) {
        if (currentModel == null || commitV0Current != commitV0 || commitV1Current != commitV1) {
            Logger.status("Loading feature models.");
            commitV0Current = commitV0;
            commitV1Current = commitV1;
            final IFeatureModel modelV0 = commitV0.featureModel().run().orElseThrow();
            final IFeatureModel modelV1 = commitV1.featureModel().run().orElseThrow();
            // We use the union of both models to sample configurations, so that all features are included
            Logger.status("Creating model union.");
            currentModel = FeatureModelUtils.UnionModel(modelV0, modelV1);

            featureModelDebug(modelV0, modelV1);
        }
        return sampler.sample(currentModel);
    }

    /**
     * Preprocess BusyBox' files by cleaning the repo before the next commit pair is checked out.
     *
     * @param splRepositoryV0 The path to BusyBox at the parent commit
     * @param splRepositoryV1 The path to BusyBox at the child commit
     */
    protected void preprocessSPLRepositories(final SPLRepository splRepositoryV0, final SPLRepository splRepositoryV1) {
        // Stash all changes and drop the stash. This is a workaround as the JGit API does not support restore.
        Logger.status("Cleaning state of V0 repo.");
        try {
            splRepositoryV0.stashCreate(true);

            splRepositoryV0.dropStash();
            Logger.status("Cleaning state of V1 repo.");
            splRepositoryV1.stashCreate(true);
            splRepositoryV1.dropStash();
        } catch (final IOException | GitAPIException e) {
            panic("Was not able to preprocess SPL repository.", e);
        }
    }

    /**
     * Postprocess BusyBox files after the next commit pair has been checked out. This is required by KernelHaven.
     *
     * @param splRepositoryV0 The path to BusyBox at the parent commit
     * @param splRepositoryV1 The path to BusyBox at the child commit
     */
    protected void postprocessSPLRepositories(final SPLRepository splRepositoryV0, final SPLRepository splRepositoryV1) {
        Logger.status("Normalizing BusyBox files...");
        try {
            BusyboxPreparation.normalizeDir(splRepositoryV0.getPath().toFile());
            BusyboxPreparation.normalizeDir(splRepositoryV1.getPath().toFile());
        } catch (final IOException e) {
            Logger.error("", e);
            panic("Was not able to normalize BusyBox.", e);
        }
    }

    // Save the features in the feature models
    private void featureModelDebug(final IFeatureModel modelV0, final IFeatureModel modelV1) {
        if (inDebug) {
            final Collection<String> featuresInDifference = FeatureModelUtils.getSymmetricFeatureDifference(modelV0, modelV1);
            try {
                Files.write(debugDir.resolve("features-V0.txt"), modelV0.getFeatures().stream().map(IFeatureModelElement::getName).collect(Collectors.toSet()));
                Files.write(debugDir.resolve("features-V1.txt"), modelV1.getFeatures().stream().map(IFeatureModelElement::getName).collect(Collectors.toSet()));
                Files.write(debugDir.resolve("variables-in-difference.txt"), featuresInDifference);
            } catch (final IOException e) {
                Logger.error("Was not able to write commit data.", e);
            }
        }
    }

    // Generate the two versions of a variant
    private void generateVariant(final SPLCommit parentCommit,
                                 final SPLCommit childCommit,
                                 final Map<Variant, GroundTruth> groundTruthV0,
                                 final Map<Variant, GroundTruth> groundTruthV1,
                                 final Variant variant,
                                 final SimpleFileFilter filter) {
        Logger.status("Generating variant " + variant.getName());
        if (inDebug && variant.getConfiguration() instanceof FeatureIDEConfiguration config) {
            try {
                Files.write(debugDir.resolve(variant.getName() + ".config"), config.toAssignment().entrySet().stream().map(entry -> entry.getKey() + " : " + entry.getValue()).collect(Collectors.toList()));
            } catch (final IOException e) {
                Logger.error("Was not able to write configuration of " + variant.getName(), e);
            }
        }

        try {
            Files.createDirectories(variantsDirV0.path().resolve(variant.getName()));
            Files.createDirectories(variantsDirV1.path().resolve(variant.getName()));
        } catch (final IOException e) {
            e.printStackTrace();
            panic("Was not able to create directory for variant: " + variant.getName());
        }

        final GroundTruth gtV0 = parentCommit
                .presenceConditions()
                .run()
                .orElseThrow()
                .generateVariant(
                        variant,
                        new CaseSensitivePath(splCopyA),
                        variantsDirV0.resolve(variant.getName()),
                        VariantGenerationOptions.ExitOnErrorButAllowNonExistentFiles(false, filter))
                .expect("Was not able to generate V0 of " + variant);
        if (inDebug) {
            try {
                Resources.Instance().write(Artefact.class, gtV0.variant(), debugDir.resolve("V0-" + variant.getName() + ".variant.csv"));
            } catch (final Resources.ResourceIOException e) {
                Logger.error("Was not able to write ground truth.");
            }
        }
        groundTruthV0.put(variant, gtV0);

        final GroundTruth gtV1 = childCommit
                .presenceConditions()
                .run()
                .orElseThrow()
                .generateVariant(
                        variant,
                        new CaseSensitivePath(splCopyB),
                        variantsDirV1.resolve(variant.getName()),
                        VariantGenerationOptions.ExitOnErrorButAllowNonExistentFiles(false, filter))
                .expect("Was not able to generate V1 of " + variant);
        if (inDebug) {
            try {
                Resources.Instance().write(Artefact.class, gtV1.variant(), debugDir.resolve("V1-" + variant.getName() + ".variant.csv"));
            } catch (final Resources.ResourceIOException e) {
                Logger.error("Was not able to write ground truth.", e);
            }
        }
        groundTruthV1.put(variant, gtV1);
    }

    /**
     * Prepare the two copies of the SPL repository by cleaning them and checking out the next commit pair
     */
    protected SimpleFileFilter splRepoPreparation(final SPLRepository parentRepo, final SPLRepository childRepo, final SPLCommit parentCommit, final SPLCommit childCommit) {
        Logger.info("Next V0 commit: " + parentCommit);
        Logger.info("Next V1 commit: " + childCommit);
        // Checkout the commits in the SPL repository
        try {
            preprocessSPLRepositories(parentRepo, childRepo);

            Logger.status("Checkout of commits in SPL repo.");
            parentRepo.checkoutCommit(parentCommit, true);
            childRepo.checkoutCommit(childCommit, true);

        } catch (final GitAPIException | IOException e) {
            panic("Was not able to checkout commit for SPL repository.", e);
        }
        Logger.info("Done.");

        Logger.info("Diffing SPL commits for find changed files.");
        final OriginalDiff diff = getOriginalDiff(splCopyA, splCopyB);
        final Set<Path> filesToKeep = new HashSet<>();
        for (final FileDiff fileDiff : diff.fileDiffs()) {
            if (!fileDiff.oldFile().getName(1).startsWith(".git")) {
                filesToKeep.add(fileDiff.oldFile().subpath(1, fileDiff.oldFile().getNameCount()));
            }
        }

        postprocessSPLRepositories(parentRepo, childRepo);

        return new SimpleFileFilter(filesToKeep);
    }


    // Initialize the study by loading the required data
    private VariabilityHistory init() {
        Logger.status("Starting experiment initialization.");
        Logger.setLogLevel(logLevel);
        // Clean old SPL repo files
        Logger.status("Cleaning old repo files.");
        if (Files.exists(splCopyA)) {
            shell.execute(new RmCommand(splCopyA).recursive()).expect("Was not able to remove SPL-V0.");
        }
        if (Files.exists(splCopyB)) {
            shell.execute(new RmCommand(splCopyB).recursive()).expect("Was not able to remove SPL-V1.");
        }
        // Copy the SPL repo
        Logger.status("Creating new SPL repo copies.");
        shell.execute(new CpCommand(splRepositoryPath, splCopyA).recursive()).expect("Was not able to copy SPL-V0.");
        shell.execute(new CpCommand(splRepositoryPath, splCopyB).recursive()).expect("Was not able to copy SPL-V1.");


        // Load VariabilityDataset
        Logger.status("Loading variability dataset.");
        VariabilityDataset dataset = null;
        try {
            final Resources instance = Resources.Instance();
            final VariabilityDatasetLoader datasetLoader = new VariabilityDatasetLoader();
            instance.registerLoader(VariabilityDataset.class, datasetLoader);
            dataset = instance.load(VariabilityDataset.class, datasetPath);
            Logger.status("Dataset loaded.");
        } catch (final Resources.ResourceIOException e) {
            panic("Was not able to load dataset.", e);
        }

        // Retrieve pairs/sequences of usable commits
        Logger.status("Retrieving commit pairs");
        return Objects.requireNonNull(dataset).getVariabilityHistory(new Domino());
    }

    // Save the difference as a patch file
    private void saveDiff(final FineDiff fineDiff, final Path file) {
        // Save the fine diff to a file
        try {
            Files.write(file, fineDiff.toLines());
        } catch (final IOException e) {
            panic("Was not able to save diff to file " + file);
        }
    }

    // Apply a patch file to a target variant
    private Set<String> applyPatch(final Path patchFile, final Path targetVariant, final Path rejectFile) {
        return applyPatch(patchFile, targetVariant, rejectFile, false);
    }

    // Apply a patch file to a target variant
    private Set<String> applyPatch(final Path patchFile, final Path targetVariant, final Path rejectFile, final boolean emptyPatch) {
        // Clean patch directory
        if (Files.exists(patchDir.toAbsolutePath())) {
            shell.execute(new RmCommand(patchDir.toAbsolutePath()).recursive());
        }

        try {
            Files.createDirectories(patchDir.getParent());
        } catch (IOException e) {
            e.printStackTrace();
            panic("Was not able to create patch directories: ", e);
        }

        if (Files.exists(rejectFile)) {
            Logger.info("Cleaning old rejects file " + rejectFile);
            shell.execute(new RmCommand(rejectFile));
        }

        // copy target variant
        shell.execute(new CpCommand(targetVariant, patchDir).recursive()).expect("Was not able to copy variant " + targetVariant);

        // apply patch to copied target variant
        final Set<String> skipped = new HashSet<>();
        if (!emptyPatch) {
            final Result<List<String>, ShellException> result = shell.execute(PatchCommand.Recommended(patchFile).strip(2).rejectFile(rejectFile).force(), patchDir);
            if (result.isSuccess()) {
                result.getSuccess().forEach(Logger::info);
            } else {
                final List<String> lines = result.getFailure().getOutput();
                Logger.info("Failed to apply part of patch. See debug log and rejects file for more information");
                String oldFile;
                for (final String nextLine : lines) {
                    Logger.debug(nextLine);
                    if (nextLine.startsWith("|---")) {
                        oldFile = nextLine.split("\\s+")[1];
                        skipped.add(oldFile);
                    }
                }
            }
        }
        return skipped;
    }

    // Get the line-level patches for a given difference
    @NotNull
    private FineDiff getFineDiff(final OriginalDiff originalDiff) {
        final DefaultContextProvider contextProvider = new DefaultContextProvider(workDir);
        return DiffSplitter.split(originalDiff, contextProvider);
    }

    // Get the filtered line-level patches for a given difference
    private FineDiff getFilteredDiff(final OriginalDiff originalDiff, final Artefact tracesV0, final Artefact tracesV1, final Variant target, Path oldVersionRoot, Path newVersionRoot) {
        final CachedPCBasedFilter cachedPCBasedFilter = new CachedPCBasedFilter(tracesV0, tracesV1, target, oldVersionRoot, newVersionRoot, 2);
        return getFilteredDiff(originalDiff, cachedPCBasedFilter);
    }

    // Get the filtered line-level patches for a given difference
    private <T extends IFileDiffFilter & ILineFilter> FineDiff getFilteredDiff(final OriginalDiff originalDiff, final T filter) {
        // Create target variant specific patch that respects PCs
        final IContextProvider contextProvider = new DefaultContextProvider(workDir);
        return DiffSplitter.split(originalDiff, filter, filter, contextProvider);
    }

    // Get the difference between two directories using UNIX diff
    protected OriginalDiff getOriginalDiff(final Path v0Path, final Path v1Path) {
        final DiffCommand diffCommand = DiffCommand.Recommended(workDir.relativize(v0Path), workDir.relativize(v1Path));
        final List<String> output = shell.execute(diffCommand, workDir).expect("Was not able to diff variants.");
        return DiffParser.toOriginalDiff(output);
    }

    // Abort the program
    protected void panic(final String message) {
        Logger.error(message);
        throw new Panic(message);
    }

    // Abort the program
    protected void panic(final String message, final Exception e) {
        Logger.error(message, e);
        e.printStackTrace();
        throw new Panic(message);
    }

    // Simple filter used during the generation of variants. Only changed files are generated.
    private record SimpleFileFilter(
            Set<Path> filesToKeep) implements ArtefactFilter<SourceCodeFile> {

        @Override
        public boolean shouldKeep(final SourceCodeFile sourceCodeFile) {
            return filesToKeep.contains(sourceCodeFile.getFile().path());
        }
    }


}
