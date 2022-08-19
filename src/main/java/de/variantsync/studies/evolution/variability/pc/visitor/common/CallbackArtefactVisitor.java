package de.variantsync.studies.evolution.variability.pc.visitor.common;

import de.variantsync.studies.evolution.variability.pc.Artefact;
import de.variantsync.studies.evolution.variability.pc.ArtefactTree;
import de.variantsync.studies.evolution.variability.pc.visitor.*;

import java.util.function.Consumer;

/**
 * Visitor template that handles each element in the same way, neglecting their types.
 * For each visited element, this visitor will
 * 1.) invoke onEnter
 * 2.) visit all subtrees
 * 3.) invoke onEnd
 */
public record CallbackArtefactVisitor(Consumer<Artefact> onEnter, Consumer<Artefact> onLeave) implements ArtefactVisitor {
    private void callback(final ArtefactTreeVisitorFocus<? extends Artefact> focus) {
        onEnter.accept(focus.getValue());
        focus.visitAllSubtrees(this);
        onLeave.accept(focus.getValue());
    }

    @Override
    public <T extends ArtefactTree<?>> void visitGenericArtefactTreeNode(final SyntheticArtefactTreeNodeVisitorFocus<T> focus) {
        callback(focus);
    }

    @Override
    public void visitSourceCodeFile(final SourceCodeFileVisitorFocus focus) {
        callback(focus);
    }

    @Override
    public void visitLineBasedAnnotation(final LineBasedAnnotationVisitorFocus focus) {
        callback(focus);
    }
}
