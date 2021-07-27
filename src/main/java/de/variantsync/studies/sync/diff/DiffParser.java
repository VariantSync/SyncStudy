package de.variantsync.studies.sync.diff;

import de.variantsync.evolution.util.NotImplementedException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DiffParser {

    public static FineDiff toFineDiff(OriginalDiff originalDiff) {
        throw new NotImplementedException();
    }

    public static OriginalDiff toOriginalDiff(List<String> lines) {
        // The diff is empty, but this is also a valid scenario
        if (lines.isEmpty()) {
            return new OriginalDiff(new ArrayList<>());
        }
        List<FileDiff> fileDiffs = new LinkedList<>();
        // Determine the substring which a FileDiff starts with
        String fileDiffStart = "";
        if (lines.get(0).startsWith("diff")) {
            // Several files were processed, the diff of each file starts with the 'diff' command that was used
            fileDiffStart = "diff";
        } else if (lines.get(0).startsWith("---")) {
            // Only one file was processed, the diff of the file starts with the hunk header
            fileDiffStart = "---";
        }

        List<String> fileDiffContent = null;
        for (String line : lines) {
            if (line.startsWith(fileDiffStart)) {
                // Create a FileDiff from the collected lines
                if (fileDiffContent != null) {
                    fileDiffs.add(parseFileDiff(fileDiffContent));
                }
                // Reset the lines that should go into the next FileDiff
                fileDiffContent = new LinkedList<>();
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

    private static FileDiff parseFileDiff(List<String> fileDiffContent) {
        int index = 0;
        final String HUNK_START = "@@";
        String nextLine = fileDiffContent.get(index);

        // Parse the header
        List<String> header = new LinkedList<>();
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
                nextLine = fileDiffContent.get(index);
                if (nextLine.startsWith(HUNK_START)) {
                    atHeader = false;
                }
                index++;
            }
        }

        // Parse the hunks
        List<Hunk> hunks = new LinkedList<>();
        {
            List<String> hunkLines = new LinkedList<>();
            for (; index < fileDiffContent.size(); index++) {
                hunkLines.add(nextLine);
                nextLine = fileDiffContent.get(index);
                if (nextLine.startsWith(HUNK_START)) {
                    hunks.add(parseHunk(hunkLines));
                    hunkLines = new LinkedList<>();
                }
            }
            // Parse the content of the last hunk
            hunks.add(parseHunk(hunkLines));
        }

        return new FileDiff(header, hunks, oldFile, newFile);
    }

    private static Hunk parseHunk(List<String> lines) {
        // Parse the header
        LocationPair locationPair = parseHunkHeader(lines.get(0));
        List<Line> content = new LinkedList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("+")) {
                content.add(new AddedLine(line));
            } else if (line.startsWith("-")) {
                content.add(new RemovedLine(line));
            } else {
                content.add(new ContextLine(line));
            }
        }
        return new Hunk(locationPair.sourceLocation, locationPair.targetLocation, content);
    }

    private static LocationPair parseHunkHeader(String line) {
        String[] parts = line.split("\\s+");
        String sourceLocationString = parts[1].substring(1);
        String targetLocationString = parts[2].substring(1);

        parts = sourceLocationString.split(",");
        HunkLocation source = new HunkLocation(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));

        parts = targetLocationString.split(",");
        HunkLocation target = new HunkLocation(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        return new LocationPair(source, target);
    }

    private record LocationPair(HunkLocation sourceLocation, HunkLocation targetLocation) {
    }
}