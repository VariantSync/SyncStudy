package de.variantsync.studies.evolution.simulation.diff.filter;

import de.variantsync.studies.evolution.simulation.diff.components.FineDiff;
import org.variantsync.vevos.simulation.feature.Variant;
import org.variantsync.vevos.simulation.variability.pc.Artefact;

import java.nio.file.Path;

public class ResultFilter extends EditFilter {

    public ResultFilter(FineDiff expectedChangesInTarget, Artefact oldTraces, Artefact newTraces, Variant targetVariant, Path oldVersionRoot, Path newVersionRoot, int strip) {
        super(oldTraces, newTraces, targetVariant, oldVersionRoot, newVersionRoot, strip);
    }
}
