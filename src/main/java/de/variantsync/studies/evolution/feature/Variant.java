package de.variantsync.studies.evolution.feature;

import de.variantsync.studies.evolution.feature.config.IConfiguration;
import org.prop4j.Node;

import java.util.Arrays;

public class Variant {
    private final String name;
    private final IConfiguration configuration;

    public Variant(final String name, final IConfiguration configuration) {
        this.name = name;
        this.configuration = configuration;
    }

    public boolean isImplementing(final Node presenceCondition) {
        return configuration.satisfies(presenceCondition);
    }

    public String getName() {
        return name;
    }

    public IConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public String toString() {
        return "Variant " + name + " with configuration " +
                Arrays.stream(configuration.toString().split("\\n")).reduce((a, b) -> a + ", " + b);
    }
}
