package de.variantsync.studies.evolution.util;

public class StringUtils {
    /**
     * Creates a string consisting of i*2 spaces.
     */
    public static String genIndent(int i) {
        final StringBuilder s = new StringBuilder();
        for (; i > 0; --i) {
            s.append("  ");
        }
        return s.toString();
    }
}
