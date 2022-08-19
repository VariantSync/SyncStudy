package de.variantsync.studies.evolution.io;

import de.variantsync.studies.evolution.util.functional.Result;

import java.nio.file.Path;

/**
 * Interface for loading resources or assets from disk or network.
 * @param <T> The type of resource this loader can load.
 */
public interface ResourceLoader<T> {
    /**
     * @return True iff this load can load the resource at the given path p.
     */
    boolean canLoad(Path p);

    /**
     * Loads the resource at the given path p.
     * Will only be invoked by Resources when canLoad(p) returned true so no duplicate check is necessary.
     * @return Either the loaded resource at path p or an exception describing the failure.
     */
    Result<T, ? extends Exception> load(Path p);
}
