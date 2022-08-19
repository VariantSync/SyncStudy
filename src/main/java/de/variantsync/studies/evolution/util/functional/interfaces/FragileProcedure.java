package de.variantsync.studies.evolution.util.functional.interfaces;

@FunctionalInterface
public interface FragileProcedure<E extends Exception> {
    void run() throws E;
}
