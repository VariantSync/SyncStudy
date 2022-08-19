package de.variantsync.studies.evolution.io.featureide;

import de.variantsync.studies.evolution.feature.config.FeatureIDEConfiguration;
import de.variantsync.studies.evolution.feature.config.IConfiguration;
import de.variantsync.studies.evolution.io.ResourceLoader;
import de.variantsync.studies.evolution.io.ResourceWriter;
import de.variantsync.studies.evolution.util.fide.ProblemListUtils;
import de.variantsync.studies.evolution.util.functional.Result;
import de.variantsync.studies.evolution.util.functional.Unit;
import de.variantsync.studies.evolution.util.io.PathUtils;
import de.ovgu.featureide.fm.core.base.impl.ConfigurationFactoryManager;
import de.ovgu.featureide.fm.core.io.IConfigurationFormat;
import de.ovgu.featureide.fm.core.io.ProblemList;
import de.ovgu.featureide.fm.core.io.manager.FileHandler;

import java.nio.file.Path;

public class FeatureIDEConfigurationIO implements ResourceLoader<IConfiguration>, ResourceWriter<IConfiguration> {
    private final IConfigurationFormat format;

    public FeatureIDEConfigurationIO(final IConfigurationFormat format) {
        this.format = format;
    }

    @Override
    public boolean canLoad(final Path p) {
        return format.supportsRead() && PathUtils.hasExtension(p, format.getSuffix());
    }

    @Override
    public boolean canWrite(final Path p) {
        return format.supportsWrite() && PathUtils.hasExtension(p, format.getSuffix());
    }

    @Override
    public Result<IConfiguration, ? extends Exception> load(final Path p) {
        return Result.Try(() -> new FeatureIDEConfiguration(ConfigurationFactoryManager.getInstance().getFactory(p, format).create()));
    }

    @Override
    public Result<Unit, ? extends Exception> write(final IConfiguration configuration, final Path p) {
        if (configuration instanceof FeatureIDEConfiguration featureIDEConfig) {
            final ProblemList problemList = FileHandler.save(p, featureIDEConfig.getConfiguration(), format);
            return ProblemListUtils.toResult(
                    problemList,
                    Unit::Instance,
                    () -> "Could not write configuration " + configuration + " to file " + p + ".");
        } else {
            return Result.Failure(new IllegalArgumentException("Given configuration " + configuration + " is not a FeatureIDEConfiguration but a " + configuration.getClass()));
        }
    }
}
