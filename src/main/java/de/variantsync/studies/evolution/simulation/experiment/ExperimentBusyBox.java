package de.variantsync.studies.evolution.simulation.experiment;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureModelElement;
import de.variantsync.studies.evolution.feature.sampling.FeatureIDESampler;
import de.variantsync.studies.evolution.feature.sampling.Sample;
import de.variantsync.studies.evolution.feature.sampling.Sampler;
import de.variantsync.studies.evolution.repository.SPLRepository;
import de.variantsync.studies.evolution.util.Logger;
import de.variantsync.studies.evolution.util.fide.FeatureModelUtils;
import de.variantsync.studies.evolution.variability.SPLCommit;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.stream.Collectors;

public class ExperimentBusyBox extends Experiment{
    private IFeatureModel currentModel;
    private SPLCommit commitV0Current;
    private SPLCommit commitV1Current;
    private final Sampler sampler;


    public ExperimentBusyBox(final ExperimentConfiguration config) {
        super(config);
        sampler = FeatureIDESampler.CreateRandomSampler(numVariants);
    }

    @Override
    protected Sample sample(final SPLCommit commitV0, final SPLCommit commitV1) {
        if (currentModel == null || commitV0Current != commitV0 || commitV1Current != commitV1) {
            Logger.status("Loading feature models.");
            commitV0Current = commitV0;
            commitV1Current = commitV1;
            final IFeatureModel modelV0 = commitV0.featureModel().run().orElseThrow();
            final IFeatureModel modelV1 = commitV1.featureModel().run().orElseThrow();
            // We use the union of both models to sample configurations, so that all features are included
            Logger.status("Creating model union.");
            currentModel = FeatureModelUtils.UnionModel(modelV0, modelV1);

            featureModelDebug(modelV0, modelV1);
        }
        return sampler.sample(currentModel);
    }

    @Override
    protected void postprocessSPLRepositories(final SPLRepository splRepositoryV0, final SPLRepository splRepositoryV1) {
        Logger.status("Normalizing BusyBox files...");
        try {
            BusyboxPreparation.normalizeDir(splCopyA.toFile());
            BusyboxPreparation.normalizeDir(splCopyB.toFile());
        } catch (final IOException e) {
            Logger.error("", e);
            panic("Was not able to normalize BusyBox.", e);
        }
    }

    @Override
    protected void preprocessSPLRepositories(final SPLRepository splRepositoryV0, final SPLRepository splRepositoryV1) {
        // Stash all changes and drop the stash. This is a workaround as the JGit API does not support restore.
        Logger.status("Cleaning state of V0 repo.");
        try {
            splRepositoryV0.stashCreate(true);

        splRepositoryV0.dropStash();
        Logger.status("Cleaning state of V1 repo.");
        splRepositoryV1.stashCreate(true);
        splRepositoryV1.dropStash();
        } catch (final IOException | GitAPIException e) {
            panic("Was not able to preprocess SPL repository.", e);
        }
    }

    private void featureModelDebug(final IFeatureModel modelV0, final IFeatureModel modelV1) {
        if (inDebug) {
            final Collection<String> featuresInDifference = FeatureModelUtils.getSymmetricFeatureDifference(modelV0, modelV1);
            try {
                Files.write(debugDir.resolve("features-V0.txt"), modelV0.getFeatures().stream().map(IFeatureModelElement::getName).collect(Collectors.toSet()));
                Files.write(debugDir.resolve("features-V1.txt"), modelV1.getFeatures().stream().map(IFeatureModelElement::getName).collect(Collectors.toSet()));
                Files.write(debugDir.resolve("variables-in-difference.txt"), featuresInDifference);
            } catch (final IOException e) {
                Logger.error("Was not able to write commit data.", e);
            }
        }
    }
}
