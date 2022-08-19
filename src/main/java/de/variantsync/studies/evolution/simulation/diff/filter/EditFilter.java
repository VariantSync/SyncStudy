package de.variantsync.studies.evolution.simulation.diff.filter;

import de.variantsync.studies.evolution.feature.Variant;
import de.variantsync.studies.evolution.variability.pc.Artefact;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditFilter extends PCBasedFilter {
    private final Map<EditLocation, Boolean> keepDecisions = new HashMap<>();
    
    public EditFilter(final Artefact oldTraces, final Artefact newTraces, final Variant targetVariant, final Path oldVersionRoot, final Path newVersionRoot) {
        super(oldTraces, newTraces, targetVariant, oldVersionRoot, newVersionRoot);
    }

    public EditFilter(final Artefact oldTraces, final Artefact newTraces, final Variant targetVariant, final Path oldVersionRoot, final Path newVersionRoot, final int strip) {
        super(oldTraces, newTraces, targetVariant, oldVersionRoot, newVersionRoot, strip);
    }

    @Override
    public boolean keepEdit(final Path filePath, final int index) {
        final EditLocation editLocation = new EditLocation(filePath, index);
        final boolean keep = super.keepEdit(filePath, index);
        keepDecisions.put(editLocation, keep);
        return keep;
    }
    
    @Override
    public boolean keepContext(final Path filePath, final int index) {
        final EditLocation editLocation = new EditLocation(filePath, index);
        return keepDecisions.getOrDefault(editLocation, true);
    }
    
    protected record EditLocation(Path filePath, int index) {
        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final EditLocation that = (EditLocation) o;
            return index == that.index && Objects.equals(filePath, that.filePath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(filePath, index);
        }
    }
}
