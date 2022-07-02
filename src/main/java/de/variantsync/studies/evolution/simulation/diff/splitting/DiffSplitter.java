package de.variantsync.studies.evolution.simulation.diff.splitting;

import de.variantsync.studies.evolution.simulation.diff.components.*;
import de.variantsync.studies.evolution.simulation.diff.filter.DefaultLineFilter;
import de.variantsync.studies.evolution.simulation.diff.filter.IFileDiffFilter;
import de.variantsync.studies.evolution.simulation.diff.filter.ILineFilter;
import de.variantsync.studies.evolution.simulation.diff.lines.AddedLine;
import de.variantsync.studies.evolution.simulation.diff.lines.ContextLine;
import de.variantsync.studies.evolution.simulation.diff.lines.Line;
import de.variantsync.studies.evolution.simulation.diff.lines.RemovedLine;
import de.variantsync.studies.evolution.simulation.diff.filter.DefaultFileDiffFilter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DiffSplitter {

    public static FineDiff split(final OriginalDiff diff, final IContextProvider contextProvider) {
        return split(diff, null, null, contextProvider);
    }

    public static FineDiff split(final OriginalDiff originalDiff, IFileDiffFilter fileFilter, ILineFilter lineFilter, final IContextProvider contextProvider) {
        fileFilter = fileFilter == null ? new DefaultFileDiffFilter() : fileFilter;
        lineFilter = lineFilter == null ? new DefaultLineFilter() : lineFilter;

        // The list in which we will collect the
        final List<FileDiff> splitFileDiffs = new LinkedList<>();

        // Go over all FileDiff in diff
        for (final FileDiff fileDiff : originalDiff.fileDiffs()) {
            // Only process file diffs that should be taken into account according to the filter
            if (fileFilter.shouldKeep(fileDiff)) {
                // Split FileDiff into edit-sized FileDiffs
                splitFileDiffs.addAll(split(fileDiff, contextProvider, lineFilter));
            }
        }

        return new FineDiff(splitFileDiffs);
    }

    private static List<FileDiff> split(final FileDiff fileDiff, final IContextProvider contextProvider, final ILineFilter lineFilter) {
        final List<FileDiff> fileDiffs = new LinkedList<>();

        int hunkLocationOffset = 0;
        for (final Hunk hunk : fileDiff.hunks()) {
            // Index that points to the location of the current line in the current hunk
            int oldIndex = 0;
            int newIndex = 0;
            for (final Line line : hunk.content()) {
                if (line instanceof RemovedLine) {
                    if (lineFilter.keepEdit(fileDiff.oldFile(), hunk.location().startLineSource() + oldIndex)) {
                        final int leadContextStart = hunk.location().startLineTarget() + newIndex - 1;
                        final int trailContextStart = hunk.location().startLineSource() + oldIndex + 1;
                        fileDiffs.add(calculateMiniDiff(contextProvider, lineFilter, fileDiff, hunk, line, trailContextStart, leadContextStart, hunkLocationOffset));
                    }
                    oldIndex++;
                } else if (line instanceof AddedLine) {
                    if (lineFilter.keepEdit(fileDiff.newFile(), hunk.location().startLineTarget() + newIndex)) {
                        final int leadContextStart = hunk.location().startLineTarget() + newIndex - 1;
                        final int trailContextStart = hunk.location().startLineSource() + oldIndex;
                        fileDiffs.add(calculateMiniDiff(contextProvider, lineFilter, fileDiff, hunk, line, trailContextStart, leadContextStart, hunkLocationOffset));
                        // Handle creation of new files. An offset of 1 has to be added after the file has been created with the first line
                        if (hunk.location().startLineSource() == 0) {
                            hunkLocationOffset = 1;
                        }
                    }
                    newIndex++;
                } else if (line instanceof ContextLine) {
                    // Increase the index
                    oldIndex++;
                    newIndex++;
                }
            }
        }
        return fileDiffs;
    }

    private static FileDiff calculateMiniDiff(final IContextProvider contextProvider, final ILineFilter lineFilter,
                                              final FileDiff fileDiff, final Hunk hunk, final Line line, final int trailContextStart,
                                              final int leadContextStart,
                                              final int hunkLocationOffset) {
        final List<Line> leadingContext = contextProvider.leadingContext(lineFilter, fileDiff, leadContextStart);
        final List<Line> trailingContext = contextProvider.trailingContext(lineFilter, fileDiff, trailContextStart);
        final List<Line> content = new LinkedList<>(leadingContext);
        content.add(line);
        content.addAll(trailingContext);

        final HunkLocation location = new HunkLocation(hunk.location().startLineSource() + hunkLocationOffset, hunk.location().startLineTarget() + hunkLocationOffset);

        final Hunk miniHunk = new Hunk(location, content);
        return new FileDiff(fileDiff.header(), Collections.singletonList(miniHunk), fileDiff.oldFile(), fileDiff.newFile());

    }
}