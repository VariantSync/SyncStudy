package de.variantsync.studies.evolution.util.io;

import java.nio.file.Path;

public interface TypedPath {
    Path path();

    static Path unwrapNullable(final TypedPath p) {
        return p == null ? null : p.path();
    }
}