package de.variantsync.studies.evolution.variability.pc;

import de.variantsync.studies.evolution.feature.Variant;
import de.variantsync.studies.evolution.io.TextIO;
import de.variantsync.studies.evolution.util.Logger;
import de.variantsync.studies.evolution.util.fide.bugfix.FixTrueFalse;
import de.variantsync.studies.evolution.util.functional.Functional;
import de.variantsync.studies.evolution.util.functional.Result;
import de.variantsync.studies.evolution.util.functional.Traversable;
import de.variantsync.studies.evolution.util.io.CaseSensitivePath;
import de.variantsync.studies.evolution.util.io.PathUtils;
import de.variantsync.studies.evolution.variability.pc.groundtruth.AnnotationGroundTruth;
import de.variantsync.studies.evolution.variability.pc.groundtruth.BlockMatching;
import de.variantsync.studies.evolution.variability.pc.groundtruth.GroundTruth;
import de.variantsync.studies.evolution.variability.pc.options.VariantGenerationOptions;
import de.variantsync.studies.evolution.variability.pc.visitor.SourceCodeFileVisitorFocus;
import org.prop4j.Node;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents a variable source code file (e.g., because part of a plugin or only conditionally included).
 */
public class SourceCodeFile extends ArtefactTree<LineBasedAnnotation> {
    private final LineBasedAnnotation rootAnnotation;

    public SourceCodeFile(final Node featureMapping, final CaseSensitivePath relativePath) {
        this(featureMapping, relativePath, new LineBasedAnnotation(FixTrueFalse.True, 1, 1, AnnotationStyle.External));
    }

    private SourceCodeFile(final Node featureMapping, final CaseSensitivePath relativePath, final LineBasedAnnotation root) {
        super(featureMapping, Collections.singletonList(root), relativePath);
        rootAnnotation = root;
    }

    @Override
    public SourceCodeFileVisitorFocus createVisitorFocus() {
        return new SourceCodeFileVisitorFocus(this);
    }

    @Override
    public Result<GroundTruth, Exception> generateVariant(
            final Variant variant,
            final CaseSensitivePath sourceDir,
            final CaseSensitivePath targetDir,
            final VariantGenerationOptions strategy) {
        final CaseSensitivePath sourceFile = sourceDir.resolve(getFile());
        final CaseSensitivePath targetFile = targetDir.resolve(getFile());

        // Check if the source file exists.
        if (!Files.exists(sourceFile.path())) {
            return Result.Failure(new FileNotFoundException("Source file " + sourceFile + " does not exist!"));
        }

        final Result<Optional<AnnotationGroundTruth>, IOException> groundTruth =
                // Create the target file.
                PathUtils.createEmptyAsResult(targetFile.path())
                // Write to target file.
                .bind(unit -> Traversable.sequence(rootAnnotation.deriveForVariant(variant).map(splAnnotationGroundTruth -> {
                    final BlockMatching lineMatching = splAnnotationGroundTruth.matching();
                    // only write lines of blocks that are part of our variant
                    final List<Integer> lines = rootAnnotation.getAllLinesFor(lineMatching::isPresentInVariant);
                    return Result.Try(
                            () -> {
                                TextIO.copyTextLines(sourceFile.path(), targetFile.path(), lines);
                                return splAnnotationGroundTruth;
                            });
                })));

        return groundTruth.bimap(
                // In case of success, return ground truth.
                Functional.match(
                        splAnnotationGroundTruth -> GroundTruth.forSourceCodeFile(
                                new SourceCodeFile(getFeatureMapping(), getFile(), splAnnotationGroundTruth.variantArtefact()),
                                splAnnotationGroundTruth
                        ),
                        () -> GroundTruth.withoutAnnotations(new SourceCodeFile(getFeatureMapping(), getFile()))
                ),
                // In case of failure, log it (and implicitly transform IOException to Exception).
                ioexception -> {
                    Logger.error("Could not create variant file " + targetFile + " because ", ioexception);
                    return ioexception;
                }
        );
    }

    public LineBasedAnnotation getRootAnnotation() {
        return rootAnnotation;
    }

    public void simplify() {
        rootAnnotation.simplify();
    }

    @Override
    public void addTrace(final LineBasedAnnotation lineBasedAnnotation) {
        rootAnnotation.addTrace(lineBasedAnnotation);
        rootAnnotation.setLineTo(Math.max(rootAnnotation.getLineTo(), lineBasedAnnotation.getLineTo()));
    }

    @Override
    public String toString() {
        return "SourceCodeFile{" +
                "featureMapping=" + getFeatureMapping() +
                ", path=" + getFile() +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final SourceCodeFile that = (SourceCodeFile) o;
        return getFile().equals(that.getFile());
    }
}
