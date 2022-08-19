package de.variantsync.studies.evolution.feature.config;

import org.prop4j.Node;

/**
 * Impossible configuration intended to be used for debugging only.
 * This configuration is a satisfying assignment for all propositional formulas.
 */
public class SayYesToAllConfiguration implements IConfiguration {
    @Override
    public boolean satisfies(final Node formula) {
        return true;
    }
}
