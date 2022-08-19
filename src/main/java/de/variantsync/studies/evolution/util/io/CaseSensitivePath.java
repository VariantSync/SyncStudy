package de.variantsync.studies.evolution.util.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Wrapper for java.nio.file.Path that is case sensitive.
 * We experienced that Path.of("FOO.c") is equal to Path.of("foo.c").
 * The sole purpose of CaseSensitivePath is to make these unequal.
 */
public record CaseSensitivePath(Path path) implements Comparable<CaseSensitivePath> {
    /**
     * @see Path::of
     */
    public static CaseSensitivePath of(final String first, final String... levels) {
        return new CaseSensitivePath(Path.of(first, levels));
    }

    /**
     * @see Path::resolve
     */
    public CaseSensitivePath resolve(final CaseSensitivePath other) {
        return new CaseSensitivePath(this.path.resolve(other.path));
    }

    /**
     * @see Path::resolve
     */
    public CaseSensitivePath resolve(final String other) {
        return new CaseSensitivePath(this.path.resolve(other));
    }

    /**
     * @see Path::resolve but with extra arguments as a shortcut with Path::of
     */
    public CaseSensitivePath resolve(final String first, final String... levels) {
        return new CaseSensitivePath(this.path.resolve(Path.of(first, levels)));
    }

    public CaseSensitivePath resolve(final Path fileName) {
        return new CaseSensitivePath(this.path.resolve(fileName));
    }

    /**
     * @return True iff the file or directory this path points to exists.
     */
    public boolean exists() {
        return Files.exists(path);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CaseSensitivePath that = (CaseSensitivePath) o;
        return path.equals(that.path) && path.toString().equals(that.path.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public int compareTo(final CaseSensitivePath o) {
        return path.compareTo(o.path);
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
