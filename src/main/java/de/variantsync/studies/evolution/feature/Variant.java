package de.variantsync.studies.evolution.feature;

import de.variantsync.studies.evolution.feature.config.IConfiguration;
import org.prop4j.Node;

import java.util.Arrays;

/**
 * A Variant is represented by its name at its configuration (i.e., the features that it implements).
 */
public class Variant {
    private final String name;
    private final IConfiguration configuration;

    public Variant(final String name, final IConfiguration configuration) {
        this.name = name;
        this.configuration = configuration;
    }

    /**
     * Check whether the variant fulfills the given presence condition. In other words, this method returns true,
     * if the presence condition (i.e., a propositional formula of features) is fulfilled if the features in the variant's
     * configuration are selected.
     *
     * @param presenceCondition The presence condition that is to be checked
     * @return true iff the variant implements the given presence condition, false otherwise.
     */
    public boolean isImplementing(final Node presenceCondition) {
        return configuration.satisfies(presenceCondition);
    }

    /**
     * @return The name of this variant.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The configuration of this variant
     */
    public IConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public String toString() {
        return "Variant " + name + " with configuration " +
                Arrays.stream(configuration.toString().split("\\n")).reduce((a, b) -> a + ", " + b);
    }
}
