package de.variantsync.studies.evolution.util.io;

import de.variantsync.studies.evolution.util.functional.Result;
import de.variantsync.studies.evolution.util.functional.Unit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

public class PathUtils {
    public static boolean hasExtension(final Path path, final String... extensions) {
        final String p = path.toString().toLowerCase(Locale.ROOT);
        for (final String extension : extensions) {
            if (p.endsWith(extension.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    public static Result<Unit, IOException> createEmptyAsResult(final Path p) {
        return Result.FromFlag(
                () -> PathUtils.createEmpty(p),
                () -> new IOException("File already exists!")
        );
    }

    /**
     * Creates a new empty file at the given path.
     * @param p pointing to a non-existent file to create.
     * @return True if the file was created. False if the file already exists.
     * @throws IOException When file could not be created.
     */
    public static boolean createEmpty(final Path p) throws IOException {
        return createEmpty(p.toFile());
    }

    /**
     * @see PathUtils::createEmpty(Path)
     */
    public static boolean createEmpty(final File f) throws IOException {
        if (!f.getParentFile().exists() && !f.getParentFile().mkdirs()) {
            throw new IOException("Creating directory " + f.getParentFile() + " failed. Thus, the file " + f.getAbsolutePath() + " could not be created!");
        }
        return f.createNewFile();
    }

}
