package de.variantsync.studies.evolution.io;

import de.variantsync.studies.evolution.util.Logger;
import de.variantsync.studies.evolution.util.functional.Result;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO: Implement readLinesAs(Path p, Function<> f) with which one can load a file into a desired format

public class TextIO {
    public static String[] readLinesAsArray(final File file) throws IOException {
        final LinkedList<String> lines = new LinkedList<>();
        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.lines().forEach(lines::add);
        } catch (final IOException e) {
            Logger.error("Failed to read lines from file: ", e);
            throw e;
        }
        return lines.toArray(new String[0]);
    }

    public static String readLastLine(final File file) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = "";
            while (reader.ready()) {
                line = reader.readLine();
            }
            return line;
        } catch (final IOException e) {
            Logger.error("Failed to read lines from file: ", e);
            throw e;
        }
    }

    /**
     * Read the lines in the file under the given path, trim whitespace at the start and end of each line, and remove empty lines
     *
     * @param p Path to the file that should be read
     * @return The lines that were read
     */
    public static Result<List<String>, IOException> readLinesTrimmed(final Path p) {
        return Result.Try(() -> Files.readAllLines(p).stream().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
    }

    /**
     * Extract all lines in [lineFrom, lineTo] = i from sourceFile and put them into targetFile for each interval i in linesToTake.
     *
     * @param sourceFile  File from which to read lines. Won't be altered.
     * @param targetFile  File to write the desired lines to.
     * @param linesToTake Intervals of lines to copy.
     * @throws IOException May occur upon writing or creating files.
     */
    public static void copyTextLines(final Path sourceFile, final Path targetFile, final List<Integer> linesToTake) throws IOException {
        /// Do not use Files.readAllLines(sourceFile) as it assumes the files to be in UTF-8 and crashes otherwise.
        //  Do also not use try (final Stream<String> linesStream = new BufferedReader(new FileReader(sourceFile.toFile())).lines()) {
        // Apparently, Java is stupid and the BufferedReader is not closed by the try-with-resources if it is anonymous.
        try (BufferedReader br = new BufferedReader(new FileReader(sourceFile.toFile()));
             Stream<String> linesStream = br.lines()) {
            final List<String> read_lines = linesStream.collect(Collectors.toList());
            final StringBuilder linesToWrite = new StringBuilder();

            for (final Integer lineNo : linesToTake) {
                int lineIndex = lineNo - 1;
                // skip lines that exceed the content
                if (lineIndex >= read_lines.size()) {
                    String logMessage = "Skipped copying line "
                            + lineNo
                            + " from \""
                            + sourceFile
                            + "\" to \""
                            + targetFile
                            + "\" as it is out of bounds [1, "
                            + read_lines.size()
                            + "]!";

                    if (lineIndex > read_lines.size()) {
                        // This was logged frequently and is caused by https://bugs.openjdk.java.net/browse/JDK-8199413
                        // Skipping the line really is the best solution, as the empty line is created by appending a line separator
                        // to the previous line. I added the additional if-statement, to only catch cases in which more than one line 
                        // is out of bounds, which indicates a serious problem. 
                        Logger.error(logMessage);
                    }
                } else {
                    // The list read_lines is 0-based.
                    // Given lines are 1-based because line numbers are typically given 1-based.
                    // Thus, we have to -1 here because line numbers are 1-indexed
                    // but we want to look them up in read_lines, which is 0-based.
                    linesToWrite.append(read_lines.get(lineIndex)).append(System.lineSeparator());
                }
            }

            Files.write(
                    targetFile,
                    linesToWrite.toString().getBytes(),
                    StandardOpenOption.APPEND);
        }
    }

    /**
     * Writes the given text to the given file.
     * Creates a new file and assumes there exists no file yet at the given path.
     *
     * @param p    File to create and fill with text.
     * @param text Text to write to file.
     * @throws IOException if an I/O error occurs writing to or creating the file, or the text cannot be encoded using the specified charset.
     *                     Also throws if the given file already exists.
     */
    public static void write(final Path p, final String text) throws IOException {
        Files.writeString(p, text, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
    }

    public static String readAsString(final Path p) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new FileReader(p.toFile()))) {
            return reader.lines().collect(Collectors.joining());
        } catch (final IOException e) {
            Logger.error("Failed to read lines from file: ", e);
            throw e;
        }
    }
}
