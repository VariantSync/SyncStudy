package org.variantsync.studies.evolution.simulation.diff.lines;

import java.nio.file.Path;

/**
 * Represents a change to a file
 *
 * @param file the changed file
 * @param line the changed line in the file
 */
public record Change(Path file, Line line) {
}
