package de.variantsync.studies.sync.diff;

import de.variantsync.studies.sync.diff.components.FileDiff;
import de.variantsync.studies.sync.diff.components.Hunk;
import de.variantsync.studies.sync.diff.components.HunkLocation;
import de.variantsync.studies.sync.diff.components.OriginalDiff;
import de.variantsync.studies.sync.diff.lines.*;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class DiffParser {

    public static OriginalDiff toOriginalDiff(final List<String> lines) {
        // The diff is empty, but this is also a valid scenario
        if (lines.isEmpty()) {
            return new OriginalDiff(new ArrayList<>());
        }
        final List<FileDiff> fileDiffs = new LinkedList<>();
        // Determine the substring which a FileDiff starts with
        String fileDiffStart = "";
        String fileDiffFollow = "";
        if (lines.get(0).startsWith("diff")) {
            // Several files were processed, the diff of each file starts with the 'diff' command that was used
            fileDiffStart = "diff";
            fileDiffFollow = "---";
        } else if (lines.get(0).startsWith("---")) {
            // Only one file was processed, the diff of the file starts with the hunk header
            fileDiffStart = "---";
            fileDiffFollow = "+++";
        }

        List<String> fileDiffContent = null;
        int indexNext = 0;
        for (final String line : lines) {
            indexNext++;
            if (line.startsWith(fileDiffStart)) {
                // Create a FileDiff from the collected lines
                if (fileDiffContent != null) {
                    fileDiffs.add(parseFileDiff(fileDiffContent));
                }
                // Reset the lines that should go into the next FileDiff
                fileDiffContent = new LinkedList<>();
            } else if (line.contains(fileDiffStart)) {
                if (indexNext < lines.size() ) {
                    final String nextLine = lines.get(indexNext);
                    if (nextLine.startsWith(fileDiffFollow)) {
                        final String additionalContent = line.substring(0, line.indexOf(fileDiffStart));
                        // Create a FileDiff from the collected lines
                        if (fileDiffContent != null) {
                            fileDiffContent.add(additionalContent);
                            fileDiffs.add(parseFileDiff(fileDiffContent));
                        }
                        // Reset the lines that should go into the next FileDiff
                        fileDiffContent = new LinkedList<>();
                        fileDiffContent.add(line.substring(line.indexOf(fileDiffStart)));
                        continue;
                    }
                }
            }
            if (fileDiffContent == null) {
                throw new IllegalArgumentException("The provided lines do not contain one of the expected fileDiffStart values");
            }
            fileDiffContent.add(line);
        }
        // Parse the content of the last file diff
        fileDiffs.add(parseFileDiff(fileDiffContent));

        return new OriginalDiff(fileDiffs);
    }

    private static FileDiff parseFileDiff(final List<String> fileDiffContent) {
        int index = 0;
        final String HUNK_START = "@@ -";
        String nextLine = fileDiffContent.get(index);

        // Parse the header
        final List<String> header = new LinkedList<>();
        String oldFile = null;
        String newFile = null;
        {
            boolean atHeader = true;
            while (atHeader) {
                if (nextLine.startsWith("---")) {
                    oldFile = nextLine.split("\\s+")[1];
                } else if (nextLine.startsWith("+++")) {
                    newFile = nextLine.split("\\s")[1];
                }
                header.add(nextLine);
                index++;
                nextLine = fileDiffContent.get(index);
                if (nextLine.startsWith(HUNK_START)) {
                    atHeader = false;
                }
            }
        }

        // Parse the hunks
        final List<Hunk> hunks = new LinkedList<>();
        {
            List<String> hunkLines = new LinkedList<>();
            hunkLines.add(nextLine);
            for (index += 1; index < fileDiffContent.size(); index++) {
                nextLine = fileDiffContent.get(index);
                if (nextLine.startsWith(HUNK_START)) {
                    hunks.add(parseHunk(hunkLines));
                    hunkLines = new LinkedList<>();
                }
                hunkLines.add(nextLine);
            }
            // Parse the content of the last hunk
            hunks.add(parseHunk(hunkLines));
        }

        return new FileDiff(header, hunks, Paths.get(Objects.requireNonNull(oldFile)), Paths.get(Objects.requireNonNull(newFile)));
    }

    private static Hunk parseHunk(final List<String> lines) {
        // Parse the header
        final HunkLocation location = parseHunkHeader(lines.get(0));
        final List<Line> content = new LinkedList<>();
        for (int i = 1; i < lines.size(); i++) {
            final String line = lines.get(i);
            if (line.startsWith("+")) {
                content.add(new AddedLine(line));
            } else if (line.startsWith("-")) {
                content.add(new RemovedLine(line));
            } else if (line.startsWith("\\")) {
                content.add(new MetaLine(line));
            } else {
                content.add(new ContextLine(line));
            }
        }
        return new Hunk(location, content);
    }

    private static HunkLocation parseHunkHeader(final String line) {
        final String[] parts = line.split("\\s+");
        final String sourceLocationString = parts[1].substring(1);
        final String targetLocationString = parts[2].substring(1);

        return new HunkLocation(Integer.parseInt(sourceLocationString.split(",")[0]), Integer.parseInt(targetLocationString.split(",")[0]));
    }
}