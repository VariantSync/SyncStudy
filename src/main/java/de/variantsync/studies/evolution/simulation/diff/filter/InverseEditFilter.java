package de.variantsync.studies.evolution.simulation.diff.filter;

import de.variantsync.studies.evolution.simulation.diff.components.FileDiff;
import org.variantsync.vevos.simulation.feature.Variant;
import org.variantsync.vevos.simulation.variability.pc.Artefact;

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
