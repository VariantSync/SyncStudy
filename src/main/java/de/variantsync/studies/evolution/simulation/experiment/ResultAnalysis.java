package de.variantsync.studies.evolution.simulation.experiment;

import de.variantsync.studies.evolution.simulation.diff.components.FineDiff;
import de.variantsync.studies.evolution.simulation.diff.lines.AddedLine;
import de.variantsync.studies.evolution.simulation.diff.lines.Change;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.variantsync.studies.evolution.util.Logger;
import de.variantsync.studies.evolution.variability.SPLCommit;
import de.variantsync.studies.evolution.simulation.diff.components.OriginalDiff;
import de.variantsync.studies.evolution.simulation.diff.lines.RemovedLine;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ResultAnalysis {
    static Path resultPath = Path.of("simulation-files").toAbsolutePath().resolve("results.txt");

    public static PatchOutcome processOutcome(final String dataset,
                                              final long runID,
                                              final String sourceVariant,
                                              final String targetVariant,
                                              final SPLCommit commitV0, final SPLCommit commitV1,
                                              final FineDiff normalPatch, final FineDiff filteredPatch,
                                              final FineDiff resultDiffNormal, final FineDiff resultDiffFiltered,
                                              OriginalDiff rejectsNormal, OriginalDiff rejectsFiltered,
                                              final FineDiff evolutionDiff,
                                              final Set<String> skippedFilesNormal,
                                              final Set<String> skippedFilesFiltered) {
        // evaluate patch rejects
        final int fileNormal = new HashSet<>(normalPatch.content().stream().map(fd -> fd.oldFile().toString()).collect(Collectors.toList())).size();
        final int lineNormal = normalPatch.content().stream().mapToInt(fd -> fd.hunks().size()).sum();
        int fileNormalFailed;
        int lineNormalFailed;
        if (rejectsNormal == null) {
            rejectsNormal = new OriginalDiff(new LinkedList<>());
        }
        Logger.status("Commit-sized patch failed");

        fileNormalFailed = new HashSet<>(rejectsNormal.fileDiffs().stream().map(fd -> fd.oldFile().toString()).collect(Collectors.toSet())).size();
        fileNormalFailed += skippedFilesNormal.size();
        Logger.status("" + fileNormalFailed + " of " + fileNormal + " normal file-sized patches failed.");
        lineNormalFailed = rejectsNormal.fileDiffs().stream().mapToInt(fd -> fd.hunks().size()).sum();
        lineNormalFailed += normalPatch.content().stream().filter(fd -> skippedFilesNormal.contains(fd.oldFile().toString())).mapToInt(fd -> fd.hunks().size()).sum();
        Logger.status("" + lineNormalFailed + " of " + lineNormal + " normal line-sized patches failed");

        final int fileFiltered = new HashSet<>(filteredPatch.content().stream().map(fd -> fd.oldFile().toString()).collect(Collectors.toList())).size();
        final int lineFiltered = filteredPatch.content().stream().mapToInt(fd -> fd.hunks().size()).sum();
        int fileFilteredFailed;
        int lineFilteredFailed;
        if (rejectsFiltered == null) {
            rejectsFiltered = new OriginalDiff(new LinkedList<>());
        }
        fileFilteredFailed = new HashSet<>(rejectsFiltered.fileDiffs().stream().map(fd -> fd.oldFile().toString()).collect(Collectors.toList())).size();
        fileFilteredFailed += skippedFilesFiltered.size();
        Logger.status("" + fileFilteredFailed + " of " + fileFiltered + " filtered file-sized patches failed.");
        lineFilteredFailed = rejectsFiltered.fileDiffs().stream().mapToInt(fd -> fd.hunks().size()).sum();
        lineFilteredFailed += filteredPatch.content().stream().filter(fd -> skippedFilesFiltered.contains(fd.oldFile().toString())).mapToInt(fd -> fd.hunks().size()).sum();
        Logger.status("" + lineFilteredFailed + " of " + lineFiltered + " filtered line-sized patches failed");

        final ConditionTable normalConditionTable = calculateConditionTable(normalPatch, normalPatch, resultDiffNormal, evolutionDiff);
        final long normalTP = normalConditionTable.tpCount();
        final long normalFP = normalConditionTable.fpCount();
        final long normalTN = normalConditionTable.tnCount();
        final long normalFN = normalConditionTable.fnCount();
        final long normalWrongLocation = normalTN + normalFN - lineNormalFailed;

        final ConditionTable filteredConditionTable = calculateConditionTable(filteredPatch, normalPatch, resultDiffFiltered, evolutionDiff);
        final long filteredTP = filteredConditionTable.tpCount();
        final long filteredFP = filteredConditionTable.fpCount();
        final long filteredTN = filteredConditionTable.tnCount();
        final long filteredFN = filteredConditionTable.fnCount();
        final long filteredWrongLocation = filteredTN + filteredFN - (/*filtered lines*/ lineNormal - lineFiltered) - lineFilteredFailed;

        assert normalTP + normalFP + normalFN + normalTN == filteredTP + filteredFP + filteredTN + filteredFN;
        assert normalTN + normalFN - normalWrongLocation <= lineNormalFailed;
        assert filteredTP + filteredFP + filteredFN + filteredTN == lineFiltered + (lineNormal - lineFiltered);

        return new PatchOutcome(dataset,
                runID,
                commitV0.id(),
                commitV1.id(),
                sourceVariant,
                targetVariant,
                resultDiffNormal.content().size(),
                resultDiffFiltered.content().size(),
                fileNormal,
                lineNormal,
                fileNormal - fileNormalFailed,
                lineNormal - lineNormalFailed,
                fileFiltered,
                lineFiltered,
                fileFiltered - fileFilteredFailed,
                lineFiltered - lineFilteredFailed,
                normalTP,
                normalFP,
                normalTN,
                normalFN,
                normalWrongLocation,
                filteredTP,
                filteredFP,
                filteredTN,
                filteredFN,
                filteredWrongLocation);
    }

    private static ConditionTable calculateConditionTable(FineDiff evaluatedPatch, FineDiff unfilteredPatch, FineDiff resultDiff, FineDiff evolutionDiff) {
        List<Change> changesInPatch = FineDiff.determineChangedLines(evaluatedPatch);
        List<Change> changesToClassify = FineDiff.determineChangedLines(unfilteredPatch);
        List<Change> changesInResult = FineDiff.determineChangedLines(resultDiff);
        List<Change> changesInEvolution = FineDiff.determineChangedLines(evolutionDiff);

        // Determine changes in the target variant's de.variantsync.studies.evolution that cannot be synchronized, because they are not part of the source variant and therefore not of the patch
        final List<Change> unpatchableChanges = new LinkedList<>();
        // Determine expected changes, i.e., changes in the target variant's de.variantsync.studies.evolution that can be synchronized
        final List<Change> requiredChanges = new LinkedList<>();
        {
            final List<Change> tempChanges = new LinkedList<>(changesToClassify);
            for (Change evolutionChange : changesInEvolution) {
                if (!tempChanges.contains(evolutionChange)) {
                    unpatchableChanges.add(evolutionChange);
                } else {
                    requiredChanges.add(evolutionChange);
                    tempChanges.remove(evolutionChange);
                }
            }
        }

        // Determine undesired changes, i.e., changes in the patch but not de.variantsync.studies.evolution
        final List<Change> undesiredChanges = new LinkedList<>();
        {
            final List<Change> tempChanges = new LinkedList<>(requiredChanges);
            for (Change patchChange : changesToClassify) {
                if (!tempChanges.contains(patchChange)) {
                    undesiredChanges.add(patchChange);
                } else {
                    tempChanges.remove(patchChange);
                }
            }
        }

        // Determine actual differences between result and expected result,
        // i.e., changes that should have been synchronized but were not, or changes that should not have been synchronized
        List<Change> actualDifferences = new LinkedList<>(changesInResult);
        unpatchableChanges.forEach(actualDifferences::remove);

        assert changesToClassify.size() >= changesInPatch.size();
        assert changesInEvolution.size() - changesToClassify.size() <= unpatchableChanges.size();
        assert changesInEvolution.size() - unpatchableChanges.size() <= changesToClassify.size();

        // We first want to account for the remaining differences between the actual and the expected result. They are either
        // false positives, i.e. changes in the patch that were applied but should not have been, or the mirror change
        // of a false negative that was applied to the wrong location.
        List<Change> remainingDifferences = new LinkedList<>();
        List<Change> fpChanges = new LinkedList<>();
        for (Change actualDifference : actualDifferences) {
            Change oppositeChange = getOppositeChange(actualDifference);
            if (undesiredChanges.contains(oppositeChange)) {
                // The patch contained a false positive, as the difference was not expected.
                changesInPatch.remove(oppositeChange);
                changesToClassify.remove(oppositeChange);
                undesiredChanges.remove(oppositeChange);
                fpChanges.add(oppositeChange);
            } else {
                remainingDifferences.add(actualDifference);
            }
        }

        // Now account for false negative
        actualDifferences = remainingDifferences;
        remainingDifferences = new LinkedList<>();
        List<Change> fnChanges = new LinkedList<>();
        for (Change actualDifference : actualDifferences) {
            // Is it a false negative?
            if (requiredChanges.contains(actualDifference)) {
                // Each line in the patch must only be considered once, all additional difference are false positives
                changesToClassify.remove(actualDifference);
                requiredChanges.remove(actualDifference);
                fnChanges.add(actualDifference);
            } else {
                remainingDifferences.add(actualDifference);
            }
        }

        // Now account for the remaining lines in the patch file and determine whether they are true positive or true negative
        List<Change> tpChanges = new LinkedList<>();
        List<Change> tnChanges = new LinkedList<>();
        for (var patchLine : changesToClassify) {
            if (requiredChanges.contains(patchLine)) {
                // In Patch & Expected: It is a true positive
                tpChanges.add(patchLine);
                // Remove the line from the expected changes to account for similar changes
                requiredChanges.remove(patchLine);
            } else if (undesiredChanges.contains(patchLine)){
                // In Patch & Unexpected: It is a true negative
                tnChanges.add(patchLine);
            }
        }
        long tp = tpChanges.size();
        long fp = fpChanges.size();
        long tn = tnChanges.size();
        long fn = fnChanges.size();
        assert tp + fp + tn + fn == FineDiff.determineChangedLines(unfilteredPatch).size();
        return new ConditionTable(tpChanges, fpChanges, tnChanges, fnChanges);
    }

    @NotNull
    private static Change getOppositeChange(Change actualDifference) {
        String changedText = actualDifference.line().line().substring(1);
        Change oppositeChange;
        if (actualDifference.line() instanceof AddedLine) {
            oppositeChange = new Change(actualDifference.file(), new RemovedLine("-" + changedText));
        } else {
            oppositeChange = new Change(actualDifference.file(), new AddedLine("+" + changedText));
        }
        return oppositeChange;
    }

    public static void main(final String... args) throws IOException {
        final AccumulatedOutcome allOutcomes = loadResultObjects(resultPath);
        System.out.println();
        System.out.println("++++++++++++++++++++++++++++++++++++++");
        System.out.println("Patch Success");
        System.out.println("++++++++++++++++++++++++++++++++++++++");
        printTechnicalSuccess(allOutcomes);

        System.out.println();
        System.out.println("++++++++++++++++++++++++++++++++++++++");
        System.out.println("Precision / Recall");
        System.out.println("++++++++++++++++++++++++++++++++++++++");

        long normalTP = allOutcomes.normalTP;
        long normalFP = allOutcomes.normalFP;
        long normalTN = allOutcomes.normalTN;
        long normalFN = allOutcomes.normalFN;

        System.out.println("Without Domain Knowledge");
        printPrecisionRecall(normalTP,
                normalFP,
                normalTN,
                normalFN,
                allOutcomes.normalWrongLocation);


        System.out.println();
        System.out.println("++++++++++++++++++++++++++++++++++++++");
        System.out.println("With Domain Knowledge");
        System.out.println();

        long filteredTP = allOutcomes.filteredTP;
        long filteredFP = allOutcomes.filteredFP;
        long filteredTN = allOutcomes.filteredTN;
        long filteredFN = allOutcomes.filteredFN;

        printPrecisionRecall(filteredTP,
                filteredFP,
                filteredTN,
                filteredFN,
                allOutcomes.filteredWrongLocation);

        System.out.println("++++++++++++++++++++++++++++++++++++++");
        System.out.println("Accuracy");
        System.out.println("++++++++++++++++++++++++++++++++++++++");

        printAccuracy(normalTP, normalFP, normalTN, normalFN, "Normal");
        printAccuracy(filteredTP, filteredFP, filteredTN, filteredFN, "Filtered");

        System.out.println("++++++++++++++++++++++++++++++++++++++");
    }

    private static void printAccuracy(long tp, long fp, long tn, long fn, String name) {
        long expectedCount = tp + tn;
        long allPositives = tp + fn;
        long allNegative = fp + tn;
        double truePositiveRate = (double) tp / (double) allPositives;
        double trueNegativeRate = (double) tn / (double) allNegative;
        long all = tp + fp + tn + fn;

        System.out.printf("%s patching achieved the expected result %d out of %d times%n", name, expectedCount, all);
        System.out.printf("Accuracy: %s%n", percentage(expectedCount, all));
        System.out.printf("Balanced Accuracy: %1.2f%n%n", ((truePositiveRate + trueNegativeRate) / 2.0));
    }

    private static void printTechnicalSuccess(final AccumulatedOutcome allOutcomes) {
        final long commitPatches = allOutcomes.commitPatches();
        final long commitSuccessNormal = allOutcomes.commitSuccessNormal();
        System.out.printf("%d of %d commit-sized patch applications succeeded (%s)%n", commitSuccessNormal, commitPatches, percentage(commitSuccessNormal, commitPatches));

        final long fileNormal = allOutcomes.fileNormal;
        final long fileSuccessNormal = allOutcomes.fileSuccessNormal;

        System.out.printf("%d of %d file-sized patch applications succeeded (%s)%n", fileSuccessNormal, fileNormal, percentage(fileSuccessNormal, fileNormal));

        final long lineNormal = allOutcomes.lineNormal;
        final long lineSuccessNormal = allOutcomes.lineSuccessNormal;
        System.out.printf("%d of %d line-sized patch applications succeeded (%s)%n", lineSuccessNormal, lineNormal, percentage(lineSuccessNormal, lineNormal));

        // -------------------
        final long lineFiltered = allOutcomes.lineFiltered;
        final long lineSuccessFiltered = allOutcomes.lineSuccessFiltered;
        System.out.printf("%d of %d line-sized patch applications succeeded after filtering (%s)%n", lineSuccessFiltered, lineFiltered, percentage(lineSuccessFiltered, lineFiltered));

    }

    private static void printPrecisionRecall(final long tp, final long fp, final long tn, final long fn, final long wrongLocation) {
        final double precision = (double) tp / ((double) tp + fp);
        final double recall = (double) tp / ((double) tp + fn);
        final double f_measure = (2 * precision * recall) / (precision + recall);

        System.out.println("TP: " + tp);
        System.out.println("FP: " + fp);
        System.out.println("TN: " + tn);
        System.out.println("FN: " + fn);
        System.out.printf("Precision: %1.2f%n", precision);
        System.out.printf("Recall: %1.2f%n", recall);
        System.out.printf("F-Measure: %1.2f%n", f_measure);
    }

    public static AccumulatedOutcome loadResultObjects(final Path path) throws IOException {
        long normalTP = 0;
        long normalFP = 0;
        long normalTN = 0;
        long normalFN = 0;

        long filteredTP = 0;
        long filteredFP = 0;
        long filteredTN = 0;
        long filteredFN = 0;

        long normalWrongLocation = 0;
        long filteredWrongLocation = 0;

        long commitPatches = 0;
        long commitSuccessNormal = 0;
        long commitSuccessFiltered = 0;

        long fileNormal = 0;
        long fileFiltered = 0;
        long fileSuccessNormal = 0;
        long fileSuccessFiltered = 0;

        long lineNormal = 0;
        long lineFiltered = 0;
        long lineSuccessNormal = 0;
        long lineSuccessFiltered = 0;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            List<String> outcomeLines = new LinkedList<>();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.isEmpty()) {
                    PatchOutcome outcome = parseResult(outcomeLines);
                    normalTP += outcome.normalTP();
                    normalFP += outcome.normalFP();
                    normalTN += outcome.normalTN();
                    normalFN += outcome.normalFN();

                    filteredTP += outcome.filteredTP();
                    filteredFP += outcome.filteredFP();
                    filteredTN += outcome.filteredTN();
                    filteredFN += outcome.filteredFN();

                    normalWrongLocation += outcome.normalWrongLocation();
                    filteredWrongLocation += outcome.filteredWrongLocation();

                    commitPatches++;
                    if (outcome.lineSuccessNormal() == outcome.lineNormal()) {
                        commitSuccessNormal++;
                    }
                    if (outcome.lineSuccessFiltered() == outcome.lineFiltered()) {
                        commitSuccessFiltered++;
                    }

                    fileNormal += outcome.fileNormal();
                    fileSuccessNormal += outcome.fileSuccessNormal();
                    fileFiltered += outcome.fileFiltered();
                    fileSuccessFiltered += outcome.fileSuccessFiltered();

                    lineNormal += outcome.lineNormal();
                    lineSuccessNormal += outcome.lineSuccessNormal();
                    lineFiltered += outcome.lineFiltered();
                    lineSuccessFiltered += outcome.lineSuccessFiltered();

                    outcomeLines.clear();
                } else {
                    outcomeLines.add(line);
                }
            }
        }

        System.out.printf("Read a total of %d results.", commitPatches);

        return new AccumulatedOutcome(
                normalTP, normalFP, normalTN, normalFN,
                filteredTP, filteredFP, filteredTN, filteredFN,
                normalWrongLocation, filteredWrongLocation,
                commitPatches, commitSuccessNormal, commitSuccessFiltered,
                fileNormal, fileFiltered, fileSuccessNormal, fileSuccessFiltered,
                lineNormal, lineFiltered, lineSuccessNormal, lineSuccessFiltered
        );
    }

    public static PatchOutcome parseResult(final List<String> lines) {
        final Gson gson = new Gson();
        final StringBuilder sb = new StringBuilder();
        lines.forEach(l -> sb.append(l).append("\n"));
        final JsonObject object = gson.fromJson(sb.toString(), JsonObject.class);
        return PatchOutcome.FromJSON(object);
    }

    public static String percentage(final long x, final long y) {
        final double percentage;
        if (y == 0) {
            percentage = 0;
        } else {
            percentage = 100 * ((double) x / (double) y);
        }
        return String.format("%3.1f%s", percentage, "%");
    }

    private static record ConditionTable(List<Change> tp, List<Change> fp, List<Change> tn, List<Change> fn) {
        public long tpCount() {
            return tp.size();
        }

        public long fpCount() {
            return fp.size();
        }

        public long tnCount() {
            return tn.size();
        }

        public long fnCount() {
            return fn.size();
        }
    }

    private static record AccumulatedOutcome(
            long normalTP,
            long normalFP,
            long normalTN,
            long normalFN,
            long filteredTP,
            long filteredFP,
            long filteredTN,
            long filteredFN,
            long normalWrongLocation,
            long filteredWrongLocation,
            long commitPatches,
            long commitSuccessNormal,
            long commitSuccessFiltered,
            long fileNormal,
            long fileFiltered,
            long fileSuccessNormal,
            long fileSuccessFiltered,
            long lineNormal,
            long lineFiltered,
            long lineSuccessNormal,
            long lineSuccessFiltered
    ) {
    }
}
