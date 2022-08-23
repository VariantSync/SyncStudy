package de.variantsync.studies.evolution.simulation.error;

/**
 * Custom Error class to represent critical errors in the study's setup.
 */
public class SetupError extends Error {
    public SetupError(final String s) {
        super(s);
    }
}