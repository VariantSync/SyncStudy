package de.variantsync.studies.evolution.feature.config;

import de.ovgu.featureide.fm.core.analysis.cnf.IVariables;
import de.ovgu.featureide.fm.core.analysis.cnf.LiteralSet;
import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import de.ovgu.featureide.fm.core.configuration.ConfigurationAnalyzer;
import de.ovgu.featureide.fm.core.configuration.ConfigurationPropagator;
import de.ovgu.featureide.fm.core.configuration.Selection;
import de.variantsync.studies.evolution.util.fide.bugfix.FixTrueFalse;
import de.variantsync.studies.evolution.util.functional.Lazy;
import org.prop4j.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for configurations from FeatureIDE that implements IConfiguration.
 */
public class FeatureIDEConfiguration implements IConfiguration {
    private /* final */ Configuration featureIDEConfig;
    // Lazy assignment of features to selected/unselected depending on selected features in FeatureIDE model
    private final Lazy<Map<Object, Boolean>> asAssignment = Lazy.of(() -> {
        final Map<Object, Boolean> assignment = new HashMap<>();
        assignment.put(FixTrueFalse.True.var, true);
        assignment.put(FixTrueFalse.False.var, false);

        for (IFeature f : featureIDEConfig.getFeatureModel().getFeatures()) {
            // Put name in map although formula.getValue expects Map<OBJECT, Boolean>.
            assignment.put(f.getName(), false);
        }

        for (IFeature f : featureIDEConfig.getSelectedFeatures()) {
            // Put name in map although formula.getValue expects Map<OBJECT, Boolean>.
            assignment.put(f.getName(), true);
        }

        return assignment;
    });

    /**
     * Wrap the given FeatureIDE configuration.
     *
     * @param featureIDEConfig Configuration in FeatureIDE format.
     */
    public FeatureIDEConfiguration(final Configuration featureIDEConfig) {
        this.featureIDEConfig = featureIDEConfig;
    }

    /**
     * Create a new FeatureIDEConfiguration from the provided literal set and feature model formula.
     *
     * @param literalSet   The set of literals that defines which features are selected / unselected
     * @param featureModel The formula that describes the feature model (i.e., its features and how they are related)
     */
    public FeatureIDEConfiguration(final LiteralSet literalSet, final FeatureModelFormula featureModel) {
        featureIDEConfig = new Configuration(featureModel);

        final IVariables vars = featureModel.getVariables();

        final int[] trueVariables = literalSet.getPositive().getLiterals();
        final int[] falseVariables = literalSet.getNegative().getLiterals();
        for (final int trueVar : trueVariables) {
            featureIDEConfig.setManual(vars.getName(trueVar), Selection.SELECTED);
        }
        for (final int falseVar : falseVariables) {
            featureIDEConfig.setManual(vars.getName(falseVar), Selection.UNSELECTED);
        }

        // Selection should be complete as the given literalSet should be total.
        // So we do not have to analyze and complete configurations.
    }

    /**
     * Create a minimal viable configuration such that all features in the given selection are selected.
     *
     * @param fm               Feature model to satisfy.
     * @param selectedFeatures Features to select.
     */
    public FeatureIDEConfiguration(final FeatureModelFormula fm, final List<String> selectedFeatures) {
        this(new Configuration(fm));

        for (final String activeFeature : selectedFeatures) {
            featureIDEConfig.setManual(activeFeature, Selection.SELECTED);
        }

        // Selection might be incomplete (e.g., parent feature not selected)
        final ConfigurationAnalyzer analyzer = new ConfigurationAnalyzer(new ConfigurationPropagator(fm, featureIDEConfig));
        analyzer.completeMin();
    }

    /**
     * Converts this configuration to an assignment from variables to values.
     *
     * @return An assignment from variables to values.
     */
    public Map<Object, Boolean> toAssignment() {
        return asAssignment.run();
    }

    @Override
    public boolean satisfies(final Node formula) {
        return formula.getValue(toAssignment());
    }

    public Configuration getConfiguration() {
        return featureIDEConfig;
    }
}
