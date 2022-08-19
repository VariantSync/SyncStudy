package de.variantsync.studies.evolution.util;

import de.variantsync.studies.evolution.util.functional.Result;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static de.variantsync.studies.evolution.util.LogLevel.*;


public class Logger {
    protected static LogLevel logLevel = INFO;
    private static Logger INSTANCE;
    private final Map<LogLevel, OutputStream> streamMap;

    private Logger(final Map<LogLevel, OutputStream> streamMap) {
        this.streamMap = streamMap;
    }

    public static void init(final Map<LogLevel, OutputStream> streamMap) {
        if (Logger.INSTANCE != null) {
            Logger.warning("Logger already initialized");
            return;
        }
        for (final LogLevel level : LogLevel.values()) {
            if (!streamMap.containsKey(level)) {
                throw new IllegalArgumentException("No Output stream for " + logLevel + " defined.");
            }
        }
        Logger.INSTANCE = new Logger(streamMap);
    }

    public static void initConsoleLogger() {
        final Map<LogLevel, OutputStream> streamMap = new HashMap<>();
        streamMap.put(LogLevel.INFO, System.out);
        streamMap.put(LogLevel.DEBUG, System.out);
        streamMap.put(LogLevel.STATUS, System.out);
        streamMap.put(LogLevel.WARNING, System.out);
        streamMap.put(LogLevel.ERROR, System.err);
        init(streamMap);
    }

    public static void debug(final String message) {
        INSTANCE.log(message, DEBUG);
    }

    public static void info(final String message) {
        INSTANCE.log(message, INFO);
    }

    public static <T> void info(final Collection<T> collection) {
        info(collectionToString(collection));
    }

    public static void status(final String message) {
        INSTANCE.log(message, STATUS);
    }

    public static void warning(final String message) {
        INSTANCE.log(message, WARNING);
    }

    public static void error(final String message) {
        INSTANCE.log(message, ERROR);
    }

    public static <T> void error(final Collection<T> collection) {
        error(collectionToString(collection));
    }

    public static void error(final String message, final Exception e) {
        INSTANCE.log(String.format("%s%n%s%n%s%n", message, e.getMessage(), Arrays.toString(e.getStackTrace())), ERROR);
    }

    public static <S, F> void log(final Result<S, F> result) {
        if (result.isSuccess()) {
            info(result.getSuccess().toString()); // printing in green would be cool! :D
        } else {
            error(result.getFailure().toString());
        }
    }

    public static void setLogLevel(final LogLevel level) {
        Logger.logLevel = level;
    }

    public static LogLevel logLevel() {
        return Logger.logLevel;
    }

    private static <T> String collectionToString(final Collection<T> collection) {
        final StringBuilder sb = new StringBuilder("[");
        int count = 0;
        for (final var item : collection) {
            sb.append(item);
            if (count < collection.size() - 1) {
                sb.append(" , ");
            }
            count++;
        }
        sb.append("]");
        return sb.toString();
    }

    protected void log(final String message, final LogLevel targetLevel) {
        if (logLevel.ordinal() <= targetLevel.ordinal()) {
            try {
                final var stream = streamMap.get(targetLevel);
                stream.write(format(message, targetLevel).getBytes(StandardCharsets.UTF_8));
                stream.write('\n');
                stream.flush();
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    // https://stackoverflow.com/questions/11306811/how-to-get-the-caller-class-in-java
    public static String getCallerClassName() {
        final StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        String callerClassName = null;
        for (int i = 1; i < stElements.length; i++) {
            final StackTraceElement ste = stElements[i];
            if (!ste.getClassName().startsWith("java.lang.Thread")) {
                if (callerClassName == null) {
                    callerClassName = ste.getClassName();
                } else if (!callerClassName.equals(ste.getClassName())) {
                    final String[] name = ste.getClassName().split("\\.");
                    return name[name.length - 1];
                }
            }
        }
        return null;
    }

    private static String format(final String message, final LogLevel level) {
        return String.format("[%s] [%s] [%s] [%s] %s", LocalDateTime.now().format(DateTimeFormatter.ISO_TIME), level, Thread.currentThread().getName(), getCallerClassName(), message);
    }

}
