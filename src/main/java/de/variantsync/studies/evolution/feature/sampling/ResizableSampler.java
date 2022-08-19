package de.variantsync.studies.evolution.feature.sampling;

public abstract class ResizableSampler implements Sampler {
    private int size;

    public ResizableSampler(final int size) {
        this.size = size;
    }

    public void setSize(final int size) {
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }
}
