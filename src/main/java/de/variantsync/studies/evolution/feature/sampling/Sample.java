package de.variantsync.studies.evolution.feature.sampling;

import de.variantsync.studies.evolution.feature.Variant;

import java.util.List;

/**
 * A Sample is a list of variants that were created for a feature model based on a preconfigured sampling strategy.
 *
 * @param variants
 */
public record Sample(List<Variant> variants) {
    public static Sample of(final List<Variant> variants) {
        return new Sample(variants);
    }

    public int size() {
        return variants.size();
    }
}
