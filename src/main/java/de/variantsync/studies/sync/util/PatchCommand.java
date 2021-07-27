package de.variantsync.studies.sync.util;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;

public class PatchCommand implements IShellCommand {
    private static final String COMMAND = "patch";
    private final LinkedList<String> args = new LinkedList<>();
    private final String patchFile;

    public PatchCommand(Path patchFile) {
        this.patchFile = "< " + patchFile;
    }

    public static PatchCommand Recommended(Path patchFile) {
        return new PatchCommand(patchFile).forward().strip(1);
    }

    /**
     * When a patch does not apply, patch usually checks if the patch looks like it has been applied
     * already by trying to reverse- apply the first hunk. The --forward option prevents that. See
     * also -R.
     *
     * @return this command
     */
    public PatchCommand forward() {
        this.args.add("--forward");
        return this;
    }

    /**
     * Strip the smallest prefix containing num leading slashes from each file name found in the patch
     * file. A sequence of one or more adjacent slashes is counted as a single slash. This controls
     * how file names found in the patch file are treated, in case you keep your files in a different
     * directory than the person who sent out the patch. For example, supposing the file name in the
     * patch file was
     *
     * @param number how many leading slashes to slash from the file name
     * @return this command
     */
    public PatchCommand strip(int number) {
        this.args.add("--strip=" + number);
        return this;
    }

    /**
     * Send output to outfile instead of patching files in place. Do not use this option if outfile is
     * one of the files to be patched. When outfile is -, send output to standard output, and send any
     * messages that would usually go to standard output to standard error.
     *
     * @param outputPath The outfile path
     * @return this command
     */
    public PatchCommand outfile(Path outputPath) {
        this.args.add("-output=" + outputPath);
        return this;
    }

    @Override
    public String[] parts() {
        String[] parts = new String[args.size() + 2];

        parts[0] = COMMAND;
        int index = 0;
        for (; index < args.size(); index++) {
            parts[index + 1] = args.get(index);
        }
        parts[index + 1] = patchFile;
        return parts;
    }

    @Override
    public String toString() {
        return "patch: " + Arrays.toString(parts());
    }
}