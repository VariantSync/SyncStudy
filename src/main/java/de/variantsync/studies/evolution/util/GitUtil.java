package de.variantsync.studies.evolution.util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/*
package load;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;


 * Class for loading Gits from several sources.
 *
 * @author Soeren Viegener

public class GitLoader {

    public static final String DEFAULT_REPOSITORIES_DIRECTORY = "repositories";

     * Loads a Git from a directory
     * @param dirname the name of the directory where the git repository is located
     * @return A Git object of the repository
    public static Git fromDirectory(String dirname){
        try {
            return Git.open(new File(dirname));
        } catch (IOException e) {
            Logger.warn("Failed to load git repo from {}", dirname);
            return null;
        }
    }

     * Loads a Git from a directory located in the default repositories directory
     * @param dirname The name of the directory in the default repositories directory
     * @return A Git object of the repository

    public static Git fromDefaultDirectory(String dirname){
        return fromDirectory(DEFAULT_REPOSITORIES_DIRECTORY + "/" + dirname);
    }

     * Loads a Git from a remote repository
     * @param remoteUri URI of the remote git repository
     * @param repositoryName Name of the repository. Sets the directory name in the default repositories directory where this repository is cloned to
     * @return A Git object of the repository

    public static Git fromRemote(String remoteUri, String repositoryName){
        try {
            Git git = Git.cloneRepository()
                    .setURI( remoteUri )
                    .setDirectory(Paths.get(DEFAULT_REPOSITORIES_DIRECTORY, repositoryName).toFile())
                    .call();
            return git;
        } catch (GitAPIException e) {
            Logger.warn("Failed to load git repo from {}", remoteUri);
            return null;
        }
    }

     * Loads a Git from a zipped repository
     * @param zipFileName Name of the zip file located in the default repositories directory
     * @return A Git object of the repository

    public static Git fromZip(String zipFileName){
        String pathname = DEFAULT_REPOSITORIES_DIRECTORY + "/" + zipFileName;
        try {
            ZipFile zipFile = new ZipFile(pathname);
            zipFile.extractAll(DEFAULT_REPOSITORIES_DIRECTORY);
        } catch (ZipException e) {
            Logger.warn("Failed to extract git repo from {}", pathname);
            return null;
        }
        return fromDefaultDirectory(zipFileName.substring(0, zipFileName.length()-4));
    }
}

 */
public class GitUtil {

    /**
     * Loads a Git from a remote repository
     *
     * @param remoteUri      URI of the remote git repository
     * @param repositoryName Name of the repository. Sets the directory name in the default repositories directory where this repository is cloned to
     * @return A Git object of the repository
     */
    public static Git fromRemote(final String remoteUri, final String repositoryName, final String repoParentDir) throws GitAPIException {
        try {
            return Git.cloneRepository()
                    .setURI(remoteUri)
                    .setDirectory(Paths.get(repoParentDir, repositoryName).toFile())
                    .call();
        } catch (final GitAPIException e) {
            Logger.error("Failed to load git repo from " + remoteUri, e);
            throw e;
        }
    }

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
