package de.variantsync.studies.evolution.repository;

import java.nio.file.Path;

/**
 * Repository containing a preprocessor based software product line (e.g., Linux kernel or Maven).
 */

public class SPLRepository extends AbstractSPLRepository {
    public SPLRepository(final Path path){
        super(path);
    }
}
