package de.variantsync.studies.evolution;

import de.variantsync.studies.evolution.io.Resources;
import de.variantsync.studies.evolution.util.Logger;
import de.ovgu.featureide.fm.core.base.impl.*;
import de.ovgu.featureide.fm.core.configuration.*;
import de.ovgu.featureide.fm.core.io.sxfm.SXFMFormat;
import de.ovgu.featureide.fm.core.io.xml.XmlFeatureModelFormat;

import java.io.File;
import java.io.IOException;

public class Main {
    private static final File PROPERTIES_FILE = new File("src/main/resources/user.properties");
    private static final String VARIABILITY_DATASET = "variability_dataset";
    private static final String SPL_REPO = "spl_repo";
    private static final String VARIANTS_REPO = "variants_repo";
    private static boolean initialized = false;

    private static void InitFeatureIDE() {
        /*
         * Who needs an SPL if we can clone-and-own from FeatureIDE's FMCoreLibrary, lol.
         */

        FMFactoryManager.getInstance().addExtension(DefaultFeatureModelFactory.getInstance());
        FMFactoryManager.getInstance().addExtension(MultiFeatureModelFactory.getInstance());
        FMFactoryManager.getInstance().setWorkspaceLoader(new CoreFactoryWorkspaceLoader());

        FMFormatManager.getInstance().addExtension(new XmlFeatureModelFormat());
        FMFormatManager.getInstance().addExtension(new SXFMFormat());

        ConfigurationFactoryManager.getInstance().addExtension(DefaultConfigurationFactory.getInstance());
        ConfigurationFactoryManager.getInstance().setWorkspaceLoader(new CoreFactoryWorkspaceLoader());

        ConfigFormatManager.getInstance().addExtension(new XMLConfFormat());
        ConfigFormatManager.getInstance().addExtension(new DefaultFormat());
        ConfigFormatManager.getInstance().addExtension(new FeatureIDEFormat());
        ConfigFormatManager.getInstance().addExtension(new EquationFormat());
        ConfigFormatManager.getInstance().addExtension(new ExpressionFormat());
    }

    public static void Initialize() {
        if (!initialized) {
            Logger.initConsoleLogger();
            InitFeatureIDE();
            initialized = true;
            Logger.debug("Finished initialization");
        }
    }

    public static void main(final String[] args) throws IOException, Resources.ResourceIOException {
        Initialize();
    }
}