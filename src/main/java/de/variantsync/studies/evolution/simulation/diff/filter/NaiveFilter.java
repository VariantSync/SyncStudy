package de.variantsync.studies.evolution.simulation.diff.filter;

import org.variantsync.vevos.simulation.feature.Variant;
import org.variantsync.vevos.simulation.variability.pc.Artefact;

import java.nio.file.Path;

public class NaiveFilter extends PCBasedFilter {

    public NaiveFilter(final Artefact oldTraces, final Artefact newTraces, final Variant targetVariant, final Path oldVersionRoot, final Path newVersionRoot) {
        super(oldTraces, newTraces, targetVariant, oldVersionRoot, newVersionRoot);
    }

    public NaiveFilter(final Artefact oldTraces, final Artefact newTraces, final Variant targetVariant, final Path oldVersionRoot, final Path newVersionRoot, final int strip) {
        super(oldTraces, newTraces, targetVariant, oldVersionRoot, newVersionRoot, strip);
    }

    @Override
    public boolean keepContext(final Path filePath, final int index) {
        return true;
    }

}
