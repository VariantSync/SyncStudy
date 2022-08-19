package de.variantsync.studies.evolution.variability.pc.groundtruth;

import de.variantsync.studies.evolution.util.io.CaseSensitivePath;
import de.variantsync.studies.evolution.variability.pc.Artefact;
import de.variantsync.studies.evolution.variability.pc.SourceCodeFile;

import java.util.HashMap;
import java.util.Map;

/**
 * GroundTruth of a variant generation process.
 * A GroundTruth consists of
 * (1) an artefact (tree) representing the variant and its presence conditions,
 * (2) a matching of source code lines for each generated file.
 */
public record GroundTruth(Artefact artefact, Map<CaseSensitivePath, AnnotationGroundTruth> groundTruth) {
    public void add(final GroundTruth other) {
        groundTruth.putAll(other.groundTruth);
    }

    public static GroundTruth withoutAnnotations(final Artefact artefact) {
        return new GroundTruth(artefact, new HashMap<>());
    }

    public static GroundTruth forSourceCodeFile(final SourceCodeFile codeFile, final AnnotationGroundTruth annotationGroundTruth) {
        final Map<CaseSensitivePath, AnnotationGroundTruth> map = new HashMap<>();
        map.put(codeFile.getFile(), annotationGroundTruth);
        return new GroundTruth(codeFile, map);
    }
}
