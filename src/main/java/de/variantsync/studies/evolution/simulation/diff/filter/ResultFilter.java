package de.variantsync.studies.evolution.simulation.diff.filter;

import de.variantsync.studies.evolution.feature.Variant;
import de.variantsync.studies.evolution.variability.pc.Artefact;
import de.variantsync.studies.evolution.simulation.diff.components.FineDiff;

import java.nio.file.Path;

public class ResultFilter extends EditFilter {

    public ResultFilter(FineDiff expectedChangesInTarget, Artefact oldTraces, Artefact newTraces, Variant targetVariant, Path oldVersionRoot, Path newVersionRoot, int strip) {
        super(oldTraces, newTraces, targetVariant, oldVersionRoot, newVersionRoot, strip);
    }
}
