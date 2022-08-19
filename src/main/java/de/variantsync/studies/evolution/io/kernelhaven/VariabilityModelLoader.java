package de.variantsync.studies.evolution.io.kernelhaven;

import de.variantsync.studies.evolution.io.ResourceLoader;
import de.variantsync.studies.evolution.util.fide.FeatureModelUtils;
import de.variantsync.studies.evolution.util.functional.Result;
import de.variantsync.studies.evolution.util.io.PathUtils;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import net.ssehub.kernel_haven.variability_model.JsonVariabilityModelCache;

import java.nio.file.Path;

public class VariabilityModelLoader implements ResourceLoader<IFeatureModel> {
    
    @Override
    public boolean canLoad(Path p) {
        return PathUtils.hasExtension(p,".json");
    }

    @Override
    public Result<IFeatureModel, ? extends Exception> load(Path p) {
        return Result.Try(() -> {
            JsonVariabilityModelCache cache = new JsonVariabilityModelCache(p.getParent().toFile());
            return FeatureModelUtils.FromVariabilityModel(cache.readFixed(p.toFile()));
        });
    }


}
