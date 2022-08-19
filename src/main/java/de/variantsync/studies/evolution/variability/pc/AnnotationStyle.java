package de.variantsync.studies.evolution.variability.pc;

/**
 * Describes whether annotations to source code are internal (within source code such as c preprocessor annotations)
 * or external (stored outside of the file as happens for generated variants).
 */
public enum AnnotationStyle {
    /**
     * for SPLs -> with macros
     */
    Internal(1),
    /**
     * for variants -> without macros
     */
    External(0);

    /**
     * Offset to annotated code. For Internal, this is 1 as the actual annotated source code is 1 line after/before
     * the annotated line start/end. Used in LineBasedAnnotation.
     */
    final int offset;
    AnnotationStyle(final int offset) {
        this.offset = offset;
    }
}
