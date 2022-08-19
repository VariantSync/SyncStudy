package de.variantsync.studies.evolution.variability.pc.visitor;

import de.variantsync.studies.evolution.variability.pc.ArtefactTree;
import de.variantsync.studies.evolution.variability.pc.SyntheticArtefactTreeNode;

public class SyntheticArtefactTreeNodeVisitorFocus<C extends ArtefactTree<?>> extends ArtefactTreeVisitorFocus<SyntheticArtefactTreeNode<C>> {
    public SyntheticArtefactTreeNodeVisitorFocus(final SyntheticArtefactTreeNode<C> artefact) {
        super(artefact);
    }

    @Override
    public void accept(final ArtefactVisitor visitor) {
        visitor.visitGenericArtefactTreeNode(this);
    }
}
