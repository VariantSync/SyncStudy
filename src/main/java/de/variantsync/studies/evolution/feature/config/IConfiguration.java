package de.variantsync.studies.evolution.feature.config;

import org.prop4j.Node;

/**
 * A configuration is an assignment of features to values.
 * This is a total configuration.
 */
@FunctionalInterface
public interface IConfiguration {
    /**
     * Evaluate the given propositional formula.
     * @param formula Formula to evaluate.
     * @return True iff this IConfiguration is a satisfying assignment for the given formula.
     */
    boolean satisfies(Node formula);
}
