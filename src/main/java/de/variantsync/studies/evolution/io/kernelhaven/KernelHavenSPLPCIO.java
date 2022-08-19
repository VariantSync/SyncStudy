package de.variantsync.studies.evolution.io.kernelhaven;

import de.variantsync.studies.evolution.variability.pc.AnnotationStyle;
import de.variantsync.studies.evolution.variability.pc.LineBasedAnnotation;
import org.prop4j.Node;

/**
 * IO for presence condition of source code of software product lines.
 * The annotated code will be considered to have inline annotations (in CPP style):
 * #if
 * foo();
 * #endif
 * This importer specifically expects the imported files to be produced by KernelHaven to adjust for its quirks.
 */
public class KernelHavenSPLPCIO extends KernelHavenPCIO {
    public KernelHavenSPLPCIO() {
        super(".spl.csv");
    }

    @Override
    protected LineBasedAnnotation createAnnotation(final Node blockCondition, final int startLine, int endLine) {
        /// If a block starts at 1 in KernelHaven files, it does not denote an #if but the entire file.
        /// Thus, there is no #if at line 1 but LineBasedAnnotation expects a macro at startLine.
        final boolean isVirtualSurroundingTrue = startLine == 1;

        /*
        Line numbers for the all surrounding true are given in [1, last line of file + 1] format.
        Line numbers for macros are given in [#if, #endif) format.
         */
        if (isVirtualSurroundingTrue) {
            /*
            If we find the all-surrounding "true" macro:
            We know that it always points behind one line behind the last line.
            Thus, we would need to do
                endLine -= 1;
            here. However, this would be inconsistent with the export of presence condition.
            When exporting presence conditions, we cannot recover the information if an annotation
            was the all surrounding virtual true macro. So to be consistent, we just leave the line
            as is although it now points to the (non-existent) line after the file's last line.
            This might cause warnings on exporting PCs.
             */
        } else {
            endLine += 1 /* to include #endif */;
        }

        return new LineBasedAnnotation(
                blockCondition,
                startLine,
                endLine,
                isVirtualSurroundingTrue ? AnnotationStyle.External : AnnotationStyle.Internal);
    }
}
