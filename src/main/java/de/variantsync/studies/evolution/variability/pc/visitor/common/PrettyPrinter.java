package de.variantsync.studies.evolution.variability.pc.visitor.common;

import de.variantsync.studies.evolution.variability.pc.Artefact;
import de.variantsync.studies.evolution.variability.pc.ArtefactTree;
import de.variantsync.studies.evolution.variability.pc.LineBasedAnnotation;
import de.variantsync.studies.evolution.variability.pc.SourceCodeFile;
import de.variantsync.studies.evolution.variability.pc.visitor.*;

/**
 * Pretty printer for ArtefactTrees.
 */
public class PrettyPrinter implements ArtefactVisitor {
    private StringBuilder builder;
    private String indent = "";

    public String prettyPrint(final Artefact artefact) {
        builder = new StringBuilder();
        artefact.accept(this);
        return builder.toString();
    }

    private void printSubtrees(final ArtefactTreeVisitorFocus<? extends Artefact> focus) {
        final String oldIndent = indent;
        indent += "  ";
        focus.visitAllSubtrees(this);
        indent = oldIndent;
    }

    @Override
    public <T extends ArtefactTree<?>> void visitGenericArtefactTreeNode(final SyntheticArtefactTreeNodeVisitorFocus<T> focus) {
        builder.append(indent).append("[").append(System.lineSeparator());
        printSubtrees(focus);
        builder.append(indent).append("]").append(System.lineSeparator());
    }

    @Override
    public void visitSourceCodeFile(final SourceCodeFileVisitorFocus focus) {
        final SourceCodeFile file = focus.getValue();
        builder
                .append(indent)
                .append(file.getFile())
                .append("<")
                .append(file.getFeatureMapping())
                .append(">[")
                .append(System.lineSeparator());

        printSubtrees(focus);

        builder.append(indent).append("]").append(System.lineSeparator());
    }

    @Override
    public void visitLineBasedAnnotation(final LineBasedAnnotationVisitorFocus focus) {
        final LineBasedAnnotation annotation = focus.getValue();
        builder
                .append(indent)
                .append("#if ")
                .append(annotation.getFeatureMapping())
                .append(" @")
                .append(annotation.getLineFrom())
                .append(System.lineSeparator());

        printSubtrees(focus);

        builder.append(indent).append("#endif @").append(annotation.getLineTo()).append(System.lineSeparator());
    }
}
