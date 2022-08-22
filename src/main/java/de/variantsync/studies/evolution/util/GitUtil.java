package de.variantsync.studies.evolution.util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

public class GitUtil {

    public static Git loadGitRepo(final File repoDir) throws IOException {
        try {
            final Repository repository = new FileRepositoryBuilder()
                    .setGitDir(new File(repoDir, ".git"))
                    .build();

            return new Git(repository);
        } catch (final IOException e) {
            Logger.error("Was not able to load git repo: ", e);
            throw e;
        }
    }

}
