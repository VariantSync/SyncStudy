package de.variantsync.studies.evolution.variability.pc.visitor;

import de.variantsync.studies.evolution.variability.pc.Artefact;

/**
 * A focus on a particular element in the visited data structure.
 * Apart from giving access to the visited element (@see getValue()), the focus can and  should be instructed
 * on how to proceed traversing the visited data by the visitor.
 * @param <A> Type of artefact that is in focus.
 */
public abstract class ArtefactVisitorFocus<A extends Artefact> {
    protected final A value;

    public ArtefactVisitorFocus(final A artefact) {
        this.value = artefact;
    }

    /**
     * @return The value in the visited data structure that is in focus.
     */
    public A getValue() {
        return value;
    }

    /**
     * Accepts a visitor and thus starts the visiting from this focus onwards.
     * This will invoke the corresponding visit-method in the visitor depending on the type of data <A> in focus.
     * @param visitor The visitor that is supposed to visit this focus.
     */
    public abstract void accept(ArtefactVisitor visitor);
}
