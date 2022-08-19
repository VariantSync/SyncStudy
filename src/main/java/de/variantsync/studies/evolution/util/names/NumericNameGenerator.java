package de.variantsync.studies.evolution.util.names;

public class NumericNameGenerator implements NameGenerator {
    private final String prefix, suffix;

    public NumericNameGenerator(final String prefix, final String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public NumericNameGenerator(final String prefix) {
        this(prefix, "");
    }

    @Override
    public String getNameAtIndex(final int i) {
        return prefix + i + suffix;
    }
}