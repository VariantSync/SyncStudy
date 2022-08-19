package de.variantsync.studies.evolution.variability.pc.visitor;

import de.variantsync.studies.evolution.variability.pc.ArtefactTree;

/**
 * Interface for visitors on artefacts (see visitor pattern).
 * A visitor traverses a data structure (in this case the ArtefactTree).
 * For each possible element in the data structure, the visitor has a dedicated method that is invoked when this element
 * is detected.
 * Visitors can dictate how the visited data structure is traversed by instructing the given focus.
 * With the focus, further sub-structures (e.g., subtrees) can be traversed subsequently or entire parts of the data
 * can even by ignored.
 */
public interface ArtefactVisitor {
    /**
     * Invoked upon traversing a GenericArtefactTreeNode.
     */
    <C extends ArtefactTree<?>> void visitGenericArtefactTreeNode(SyntheticArtefactTreeNodeVisitorFocus<C> focus);

    /**
     * Invoked upon traversing a SourceCodeFile.
     */
    void visitSourceCodeFile(SourceCodeFileVisitorFocus focus);

    /**
     * Invoked upon traversing a LineBasedAnnotation.
     */
    void visitLineBasedAnnotation(LineBasedAnnotationVisitorFocus focus);
}
