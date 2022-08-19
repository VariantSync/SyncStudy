package de.variantsync.studies.evolution.io;

import de.variantsync.studies.evolution.util.functional.Result;
import de.variantsync.studies.evolution.util.functional.Unit;

import java.nio.file.Path;

/**
 * Interface for writing resources or assets to disk or upload to cloud storage.
 * @param <T> The type of resource this writer can write.
 */
public interface ResourceWriter<T> {
    /**
     * @return True iff this load can load the resource at the given path p.
     */
    boolean canWrite(Path p);

    /**
     * Loads the resource at the given path p.
     * Will only be invoked by Resources when canLoad(p) returned true so no duplicate check is necessary.
     * @return Either the loaded resource at path p or an exception describing the failure.
     */
    Result<Unit, ? extends Exception> write(T object, Path p);
}
