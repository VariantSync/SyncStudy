package de.variantsync.studies.evolution.variability.pc.groundtruth;

import de.variantsync.studies.evolution.variability.pc.LineBasedAnnotation;

public record AnnotationGroundTruth(
        LineBasedAnnotation splArtefact,
        LineBasedAnnotation variantArtefact,
        BlockMatching matching
)
{
}
