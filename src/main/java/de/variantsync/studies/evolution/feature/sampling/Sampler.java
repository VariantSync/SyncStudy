package de.variantsync.studies.evolution.feature.sampling;

import de.ovgu.featureide.fm.core.base.IFeatureModel;

/**
 * A Sampler is responsible for providing a sample of variants for a given feature model. The size of each sample is to
 * be configured in the sampler.
 */
public interface Sampler {
    /**
     * @return The size of each sample that is sampled by this Sampler.
     */
    int size();

    /**
     * Create and return a sample of variants for the given feature model.
     *
     * @param model The model for which a sample is created
     * @return The sample of variants
     */
    Sample sample(IFeatureModel model);
}
