package de.variantsync.studies.evolution.simulation.diff.filter;

import de.variantsync.studies.evolution.feature.Variant;
import de.variantsync.studies.evolution.util.io.CaseSensitivePath;
import de.variantsync.studies.evolution.util.Logger;
import de.variantsync.studies.evolution.util.functional.Result;
import de.variantsync.studies.evolution.variability.pc.Artefact;
import de.variantsync.studies.evolution.simulation.diff.components.FileDiff;
import de.variantsync.studies.evolution.simulation.error.Panic;
import org.prop4j.Node;

import java.nio.file.Path;

public class PCBasedFilter implements IFileDiffFilter, ILineFilter {
    protected final Artefact oldTraces;
    protected final Artefact newTraces;
    protected final Variant targetVariant;
    protected final Path oldVersion;
    protected final Path newVersion;
    protected final int strip;

    public PCBasedFilter(final Artefact oldTraces, final Artefact newTraces, final Variant targetVariant, final Path oldVersionRoot, final Path newVersionRoot) {
        this.oldTraces = oldTraces;
        this.newTraces = newTraces;
        this.targetVariant = targetVariant;
        this.oldVersion = oldVersionRoot;
        this.newVersion = newVersionRoot;
        this.strip = 0;
    }

    public PCBasedFilter(final Artefact oldTraces, final Artefact newTraces, final Variant targetVariant, final Path oldVersionRoot, final Path newVersionRoot, final int strip) {
        this.oldTraces = oldTraces;
        this.newTraces = newTraces;
        this.targetVariant = targetVariant;
        this.oldVersion = oldVersionRoot;
        this.newVersion = newVersionRoot;
        this.strip = strip;
    }

    protected boolean shouldKeep(final Variant targetVariant, final Artefact traces, Path filePath, final int index) {
        filePath = filePath.subpath(strip, filePath.getNameCount());
        final Result<Node, Exception> pc = traces
                .getPresenceConditionOf(new CaseSensitivePath(filePath), index);
        if (pc.isSuccess()) {
            return targetVariant.isImplementing(pc.getSuccess());
        } else {
            Logger.error("Was not able to load PC for line " + index + " of " + filePath);
            return false;
        }
    }

    protected boolean shouldKeep(final Variant targetVariant, final Artefact traces, Path filePath) {
        filePath = filePath.subpath(strip, filePath.getNameCount());
        final Result<Node, Exception> result = traces.getPresenceConditionOf(new CaseSensitivePath(filePath));
        if (result.isFailure()) {
            Logger.warning("No PC found for " + filePath);
            return false;
        } else {
            final Node pc = result.getSuccess();
            return targetVariant.isImplementing(pc);
        }
    }

    @Override
    public boolean shouldKeep(final FileDiff fileDiff) {
        return shouldKeep(targetVariant, oldTraces, fileDiff.oldFile()) || shouldKeep(targetVariant, newTraces, fileDiff.newFile());
    }

    @Override
    public boolean keepEdit(final Path filePath, final int index) {
        if (oldVersion.endsWith(filePath.getName(0))) {
            return shouldKeep(targetVariant, oldTraces, filePath, index);
        } else if (newVersion.endsWith(filePath.getName(0))) {
            return shouldKeep(targetVariant, newTraces, filePath, index);
        } else {
            final String message = "The given path '" + filePath + "' does not match any of the versions' paths";
            Logger.error(message);
            throw new Panic(message);
        }
    }
}
