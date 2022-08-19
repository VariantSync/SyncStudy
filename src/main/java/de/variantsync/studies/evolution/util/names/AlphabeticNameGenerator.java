package de.variantsync.studies.evolution.util.names;

public class AlphabeticNameGenerator implements NameGenerator {
    private static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz".toUpperCase().toCharArray();

    @Override
    public String getNameAtIndex(int i) {
        final StringBuilder name = new StringBuilder();

        while (i >= ALPHABET.length) {
            name.insert(0, ALPHABET[i % ALPHABET.length]);
            i = (i / ALPHABET.length) - 1; // -1 because I don't know
        }

        return ALPHABET[i] + name.toString();
    }
}
