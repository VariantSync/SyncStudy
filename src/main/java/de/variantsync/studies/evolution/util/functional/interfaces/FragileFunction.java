package de.variantsync.studies.evolution.util.functional.interfaces;

@FunctionalInterface
public interface FragileFunction<A, B, E extends Exception> {
    B run(A a) throws E;
    
    default <Output, ExceptionAfter extends Exception> FragileFunction<A, Output, Exception> andThen(final FragileFunction<B, Output, ExceptionAfter> after) {
        return (A input) -> after.run(this.run(input));
    }
}
