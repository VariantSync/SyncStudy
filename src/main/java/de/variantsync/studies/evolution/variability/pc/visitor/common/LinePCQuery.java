package de.variantsync.studies.evolution.variability.pc.visitor.common;

import de.variantsync.studies.evolution.util.functional.Result;
import de.variantsync.studies.evolution.util.io.CaseSensitivePath;
import de.variantsync.studies.evolution.variability.pc.ArtefactTree;
import de.variantsync.studies.evolution.variability.pc.LineBasedAnnotation;
import de.variantsync.studies.evolution.variability.pc.SourceCodeFile;
import de.variantsync.studies.evolution.variability.pc.visitor.ArtefactVisitor;
import de.variantsync.studies.evolution.variability.pc.visitor.LineBasedAnnotationVisitorFocus;
import de.variantsync.studies.evolution.variability.pc.visitor.SourceCodeFileVisitorFocus;
import de.variantsync.studies.evolution.variability.pc.visitor.SyntheticArtefactTreeNodeVisitorFocus;
import org.prop4j.Node;

import java.io.FileNotFoundException;

public class LinePCQuery implements ArtefactVisitor {
    private final CaseSensitivePath relativePath;
    private final int lineNumber;

    private SourceCodeFile foundFile = null;
    private boolean lineFound = false;
    private Node result = null;

    public LinePCQuery(final CaseSensitivePath relativePath, final int lineNumber) {
        this.relativePath = relativePath;
        this.lineNumber = lineNumber;
    }

    public Result<Node, Exception> getResult() {
        if (foundFile != null) {
            if (lineFound) {
                return Result.Success(result);
            }

            return Result.Failure(new IndexOutOfBoundsException(
                    "Given line number "
                            + lineNumber
                            + " is not within bounds of file "
                            + relativePath
                            + " that ranges from "
                            + foundFile.getRootAnnotation().getLineFrom()
                            + " to "
                            + foundFile.getRootAnnotation().getLineTo()
                            + "."
            ));
        }

        return Result.Failure(new FileNotFoundException("Could not find file " + relativePath.toString() + "!"));
    }

    @Override
    public <C extends ArtefactTree<?>> void visitGenericArtefactTreeNode(final SyntheticArtefactTreeNodeVisitorFocus<C> focus) {
//        Logger.info("visitGenericArtefactTreeNode(" + focus.getValue() + ")");
        for (int i = 0; foundFile == null && i < focus.getValue().getNumberOfSubtrees(); ++i) {
            focus.visitSubtree(i, this);
        }
    }

    @Override
    public void visitSourceCodeFile(final SourceCodeFileVisitorFocus focus) {
//        Logger.info("visitSourceCodeFile(" + focus.getValue() + ")");
        if (foundFile == null && focus.getValue().getFile().equals(relativePath)) {
            foundFile = focus.getValue();
            focus.skipRootAnnotationButVisitItsSubtrees(this);
        }
    }

    @Override
    public void visitLineBasedAnnotation(final LineBasedAnnotationVisitorFocus focus) {
//        Logger.info("visitLineBasedAnnotation(" + focus.getValue() + ")");
        final LineBasedAnnotation val = focus.getValue();
        if (!lineFound && val.annotates(lineNumber)) {
            focus.visitAllSubtrees(this);
            if (!lineFound) {
                result = val.getPresenceCondition();
                lineFound = true;
            }
        }
    }
}
