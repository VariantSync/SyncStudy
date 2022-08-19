package de.variantsync.studies.evolution.variability.pc;

import de.variantsync.studies.evolution.feature.Variant;
import de.variantsync.studies.evolution.util.Logger;
import de.variantsync.studies.evolution.util.fide.bugfix.FixTrueFalse;
import de.variantsync.studies.evolution.util.functional.Functional;
import de.variantsync.studies.evolution.util.functional.Result;
import de.variantsync.studies.evolution.util.io.CaseSensitivePath;
import de.variantsync.studies.evolution.variability.pc.groundtruth.GroundTruth;
import de.variantsync.studies.evolution.variability.pc.options.VariantGenerationOptions;
import de.variantsync.studies.evolution.variability.pc.visitor.ArtefactVisitorFocus;
import de.variantsync.studies.evolution.variability.pc.visitor.SyntheticArtefactTreeNodeVisitorFocus;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * A synthetic node in the artefact tree that does not need to have a physical counterpart (e.g., file or annotation).
 * @param <Child> The type of children this node can have.
 */
public class SyntheticArtefactTreeNode<Child extends ArtefactTree<?>> extends ArtefactTree<Child> {
    /**
     * Creates a new empty tree (node) with feature mapping True.
     */
    public SyntheticArtefactTreeNode() {
        this(new ArrayList<>());
    }

    /**
     * Creates a new tree (node) with feature mapping True and the given subtrees.
     */
    public SyntheticArtefactTreeNode(final List<Child> subtrees) {
        super(FixTrueFalse.True, subtrees, null);
    }

    /**
     * Plain copy constructor.
     * @param other Object to create a plain copy of (without copying children).
     */
    public SyntheticArtefactTreeNode(final ArtefactTree<Child> other) {
        super(other.getFeatureMapping(), new ArrayList<>(), other.getFile());
    }

    @Override
    public ArtefactVisitorFocus<? extends Artefact> createVisitorFocus() {
        return new SyntheticArtefactTreeNodeVisitorFocus<>(this);
    }

    @Override
    public Result<GroundTruth, Exception> generateVariant(final Variant variant, final CaseSensitivePath sourceDir, final CaseSensitivePath targetDir, final VariantGenerationOptions strategy) {
        final CaseSensitivePath f = getFile();
        final SyntheticArtefactTreeNode<Child> copy = plainCopy();
        final GroundTruth groundTruth = GroundTruth.withoutAnnotations(copy);

        if (f != null && !sourceDir.resolve(f).exists()) {
            Logger.error("Skipping file " + f + " as it does not exist!");
            if (strategy.exitOnError() && !strategy.ignoreNonExistentSPLFiles()) {
                return Result.Failure(new FileNotFoundException(f + " does not exist!"));
            }
        } else {
            for (final Child subtree : subtrees) {
                if (subtree instanceof SourceCodeFile sourceCodeFile) {
                    if (!strategy.filter().shouldKeep(sourceCodeFile)) {
                        continue;
                    }
                }

                if (variant.isImplementing(subtree.getPresenceCondition())) {
                    final Result<GroundTruth, Exception> result = subtree
                            .generateVariant(variant, sourceDir, targetDir, strategy);

                    result.ifSuccess(childGroundTruth -> {
                        copy.addTrace(Functional.uncheckedCast(childGroundTruth.artefact()));
                        groundTruth.add(childGroundTruth);
                    });

                    if (result.isFailure()) {
                        if (
                                strategy.exitOnError()
                                && !(strategy.ignoreNonExistentSPLFiles() && result.getFailure() instanceof FileNotFoundException)
                        ) {
                            return result;
                        } else {
                            Logger.error(result.getFailure().getMessage());
                        }
                    }
                }
            }
        }

        return Result.Success(groundTruth);
    }

    @Override
    public String toString() {
        return "SyntheticArtefactTreeNode{" +
                "featureMapping=" + getFeatureMapping() +
                '}';
    }

    public SyntheticArtefactTreeNode<Child> plainCopy() {
        return new SyntheticArtefactTreeNode<>(this);
    }
}
