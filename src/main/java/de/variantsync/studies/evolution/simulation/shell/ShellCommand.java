package de.variantsync.studies.evolution.simulation.shell;

import de.variantsync.studies.evolution.util.functional.Result;
import de.variantsync.studies.evolution.simulation.error.ShellException;

import java.util.Arrays;
import java.util.List;

public abstract class ShellCommand {
    /***
     * Return the String parts that define and configure the command execution (e.g., ["echo", "Hello World"])
     *
     * @return the parts of the shell command.
     */
    public abstract String[] parts();

    /**
     * Interpret the result code returned from a shell command
     *
     * @param resultCode the code that is to be parsed
     * @return the result
     */
    public Result<List<String>, ShellException> interpretResult(final int resultCode, final List<String> output) {
        return resultCode == 0 ? Result.Success(output) : Result.Failure(new ShellException(output));
    }

    @Override
    public String toString() {
        return Arrays.toString(this.parts());
    }
}