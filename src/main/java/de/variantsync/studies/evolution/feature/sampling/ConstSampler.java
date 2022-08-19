package de.variantsync.studies.evolution.feature.sampling;

import de.ovgu.featureide.fm.core.base.IFeatureModel;

public final record ConstSampler(Sample sample) implements Sampler {
    @Override
    public int size() {
        return sample.size();
    }

    @Override
    public Sample sample(final IFeatureModel model) {
        return sample;
    }
}
