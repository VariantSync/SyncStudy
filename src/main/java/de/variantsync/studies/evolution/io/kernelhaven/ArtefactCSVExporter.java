package de.variantsync.studies.evolution.io.kernelhaven;

import de.variantsync.studies.evolution.io.data.CSV;
import de.variantsync.studies.evolution.util.fide.FormulaUtils;
import de.variantsync.studies.evolution.variability.pc.ArtefactTree;
import de.variantsync.studies.evolution.variability.pc.LineBasedAnnotation;
import de.variantsync.studies.evolution.variability.pc.SourceCodeFile;
import de.variantsync.studies.evolution.variability.pc.visitor.ArtefactVisitor;
import de.variantsync.studies.evolution.variability.pc.visitor.LineBasedAnnotationVisitorFocus;
import de.variantsync.studies.evolution.variability.pc.visitor.SourceCodeFileVisitorFocus;
import de.variantsync.studies.evolution.variability.pc.visitor.SyntheticArtefactTreeNodeVisitorFocus;
import org.prop4j.NodeWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Exporter for artefact trees to CSV files in the format from KernelHaven.
 *
 * CSV Header:
 * Path;File Condition;Block Condition;Presence Condition;start;end
 */
public class ArtefactCSVExporter implements ArtefactVisitor {
    private final static int COLUMN_COUNT = 6;
    private final List<String[]> csv = new ArrayList<>();
    private SourceCodeFile currentFile = null;

    /**
     * @return A new csv row.
     */
    private static String[] makeRow() {
        return new String[COLUMN_COUNT];
    }

    public ArtefactCSVExporter() {
        // create header
        final String[] header = makeRow();
        header[0] = "Path";
        header[1] = "File Condition";
        header[2] = "Block Condition";
        header[3] = "Presence Condition";
        header[4] = "start";
        header[5] = "end";
        csv.add(header);
    }

    /**
     * Finalizes the export.
     * @return CSV object that can be used for writing to disk.
     */
    public CSV export() {
        return new CSV(csv);
    }

    /**
     * Creates a CSV row for the given annotation but uses the given start and end lines.
     * @return The CSV row.
     */
    private String[] toRow(final LineBasedAnnotation annotation) {
        final String[] row = makeRow();
        row[0] = currentFile.getFile().toString();
        row[1] = FormulaUtils.toString(currentFile.getPresenceCondition(), NodeWriter.javaSymbols);
        row[2] = FormulaUtils.toString(annotation.getFeatureMapping(), NodeWriter.javaSymbols);
        row[3] = FormulaUtils.toString(annotation.getPresenceCondition(), NodeWriter.javaSymbols);
        row[4] = "" + annotation.getLineFrom();
        // -1 because Kernelhaven stores annotations as [#if, #endif) intervals, so we have to point one line before the annotation end (#endif).
        row[5] = "" + (annotation.getLineTo() - (annotation.isMacro() ? 1 : 0));
        return row;
    }

    @Override
    public <T extends ArtefactTree<?>> void visitGenericArtefactTreeNode(final SyntheticArtefactTreeNodeVisitorFocus<T> focus) {
        focus.visitAllSubtrees(this);
    }

    @Override
    public void visitSourceCodeFile(final SourceCodeFileVisitorFocus focus) {
        currentFile = focus.getValue();
        focus.skipRootAnnotationButVisitItsSubtrees(this);
        currentFile = null;
    }

    @Override
    public void visitLineBasedAnnotation(final LineBasedAnnotationVisitorFocus focus) {
        final LineBasedAnnotation annotation = focus.getValue();
        csv.add(toRow(annotation));
        focus.visitAllSubtrees(this);
    }
}