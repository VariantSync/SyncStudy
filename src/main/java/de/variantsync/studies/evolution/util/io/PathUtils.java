package de.variantsync.studies.evolution.util.io;

import de.variantsync.studies.evolution.util.functional.CompositeException;
import de.variantsync.studies.evolution.util.functional.Monoid;
import de.variantsync.studies.evolution.util.functional.Result;
import de.variantsync.studies.evolution.util.functional.Unit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Stream;

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

    /**
     * Maybe bug?
     * I often have the problem with Java that it only requests the deletion of the file, but does not
     * guarantee that it is deleted. In the VariabilityExtraction project this was a serious issue and in the end
     * I had to call rm -f  ... as shell command.
     * @param path Path to a directory that should be deleted.
     * @return Unit iff deletion was successful, an exception explaining the failure otherwise.
     */
    public static Result<Unit, CompositeException> deleteDirectory(final Path path) {
        // read java doc, Files.walk need close the resources.
        // try-with-resources to ensure that the stream's open directories are closed
        try (final Stream<Path> walk = Files.walk(path)) {
            final Monoid<Result<Unit, CompositeException>> resultReducer = Result.MONOID(Unit.MONOID, CompositeException.MONOID);
            return walk
                    .sorted(Comparator.reverseOrder())
                    .map(f -> Result.Try(() -> Files.delete(f)).mapFail(CompositeException::new))
                    .reduce(resultReducer::mAppend)
                    .orElseGet(() -> Result.Success(Unit.Instance()));
        } catch (final NoSuchFileException e) {
            // If the given path does not exist, then there was nothing to delete.
            // So it is already "non existent" which is what we want.
            return Result.Success(Unit.Instance());
        } catch (final IOException e) {
            return Result.Failure(new CompositeException(e));
        }
    }
}
