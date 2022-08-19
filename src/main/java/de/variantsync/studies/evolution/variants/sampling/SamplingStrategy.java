package de.variantsync.studies.evolution.variants.sampling;

import de.variantsync.studies.evolution.feature.sampling.Sample;
import de.variantsync.studies.evolution.variants.blueprints.VariantsRevisionBlueprint;
import de.ovgu.featureide.fm.core.base.IFeatureModel;

import java.util.Optional;

public interface SamplingStrategy {
    Sample sampleForRevision(Optional<IFeatureModel> model, VariantsRevisionBlueprint blueprint);
}
