package de.variantsync.studies.evolution.util.functional.interfaces;

@FunctionalInterface
public interface FragileSupplier<T, E extends Exception> {
    T get() throws E;
}
