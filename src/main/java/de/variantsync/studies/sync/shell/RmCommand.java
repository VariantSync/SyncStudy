package de.variantsync.studies.sync.shell;

import java.nio.file.Path;
import java.util.LinkedList;

public class RmCommand extends ShellCommand {
    private static final String COMMAND = "rm";
    private final String file;
    private final LinkedList<String> args = new LinkedList<>();
    
    public RmCommand(Path file) {
        this.file = file.toString();
    }

    public RmCommand(String file) {
        this.file = file;
    }

    @Override
    public String[] parts() {
        String[] parts = new String[args.size() + 2];

        parts[0] = COMMAND;
        int index = 0;
        for (; index < args.size(); index++) {
            parts[index + 1] = args.get(index);
        }
        
        parts[index+1] = file;
        return parts;
    }

    public RmCommand recursive() {
        this.args.add("-r");
        return this;
    }
}
