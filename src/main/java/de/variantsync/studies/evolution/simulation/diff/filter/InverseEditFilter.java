package de.variantsync.studies.evolution.simulation.diff.filter;

import de.variantsync.studies.evolution.feature.Variant;
import de.variantsync.studies.evolution.variability.pc.Artefact;
import de.variantsync.studies.evolution.simulation.diff.components.FileDiff;

import java.nio.file.Path;

public class InverseEditFilter implements ILineFilter, IFileDiffFilter {
    final private EditFilter editFilter;

    public InverseEditFilter(Artefact oldTraces, Artefact newTraces, Variant targetVariant, Path oldVersionRoot, Path newVersionRoot) {
        editFilter = new EditFilter(oldTraces, newTraces, targetVariant, oldVersionRoot, newVersionRoot);
    }

    public InverseEditFilter(Artefact oldTraces, Artefact newTraces, Variant targetVariant, Path oldVersionRoot, Path newVersionRoot, int strip) {
        editFilter = new EditFilter(oldTraces, newTraces, targetVariant, oldVersionRoot, newVersionRoot, strip);
    }

    @Override
    public boolean shouldKeep(FileDiff fileDiff) {
        return !editFilter.shouldKeep(fileDiff);
    }

    @Override
    public boolean keepEdit(Path filePath, int index) {
        return !editFilter.keepEdit(filePath, index);
    }
}
