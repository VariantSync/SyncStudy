package de.variantsync.studies.evolution.io.data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Record to represent CSV files row-wise.
 */
public record CSV(List<String[]> rows) {
    public String toString(final String delimiter) {
        return rows.stream()
                .map(row -> String.join(delimiter, row))
                .collect(Collectors.joining("\r\n"));
    }

    @Override
    public String toString() {
        return toString("; ");
    }
}
