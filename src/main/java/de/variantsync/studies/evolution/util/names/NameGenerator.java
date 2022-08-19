package de.variantsync.studies.evolution.util.names;

@FunctionalInterface
public interface NameGenerator {
    String getNameAtIndex(int i);
}