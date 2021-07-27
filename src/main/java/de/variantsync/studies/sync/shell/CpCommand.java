package de.variantsync.studies.sync.shell;

import java.nio.file.Path;
import java.util.LinkedList;

public class CpCommand implements IShellCommand {
    private static final String COMMAND = "cp";
    private final String from;
    private final String to;
    private final LinkedList<String> args = new LinkedList<>();


    public CpCommand(Path from, Path to) {
        this.from = from.toString();
        this.to = to.toString();
    }

    /**
     * copy directories recursively
     * @return this command
     */
    public CpCommand recursive() {
        this.args.add("-r");
        return this;
    }

    @Override
    public String[] parts() {
        String[] parts = new String[args.size() + 3];

        parts[0] = COMMAND;
        int index = 0;
        for (; index < args.size(); index++) {
            parts[index + 1] = args.get(index);
        }
        parts[index+1] = from;
        parts[index+2] = to;
        return parts;
    }
}