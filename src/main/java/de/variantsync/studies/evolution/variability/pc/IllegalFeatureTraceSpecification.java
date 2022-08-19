package de.variantsync.studies.evolution.variability.pc;

/**
 * Exception that is thrown upon parsing illegal specifications of feature traces.
 */
public class IllegalFeatureTraceSpecification extends RuntimeException {
    public IllegalFeatureTraceSpecification(final String msg) {
        super(msg);
    }
}
