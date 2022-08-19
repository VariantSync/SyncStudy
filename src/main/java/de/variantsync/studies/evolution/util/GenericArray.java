package de.variantsync.studies.evolution.util;

import java.util.function.IntFunction;

public class GenericArray {
    @SafeVarargs
    public static <T, U extends T> T[] create(final U... ts) {
        return ts;
    }

    @SuppressWarnings("unchecked")
    public static <T> IntFunction<T[]> create() {
        return len -> (T[]) new Object[len];
    }
}
