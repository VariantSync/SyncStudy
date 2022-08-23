package org.variantsync.studies.evolution.simulation.diff.components;

import java.util.LinkedList;
import java.util.List;

/**
 * A OriginalDiff holds the difference between two versions of a software project. The difference is represented by the
 * patches of all changed files. The patches are grouped by file and represented by FileDiff objects.
 * Each FileDiff contains the patches made to a specific file as specified by UNIX diff.
 *
 * @param fileDiffs The differences of the changed files.
 */
public record OriginalDiff(List<FileDiff> fileDiffs) implements IDiffComponent {
    @Override
    public List<String> toLines() {
        final List<String> lines = new LinkedList<>();
        fileDiffs.stream().map(IDiffComponent::toLines).forEach(lines::addAll);
        return lines;
    }

    public boolean isEmpty() {
        return this.fileDiffs.isEmpty();
    }
}