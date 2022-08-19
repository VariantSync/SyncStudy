package de.variantsync.studies.evolution.simulation.experiment;

import de.variantsync.studies.evolution.util.LogLevel;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;

public class ExperimentConfiguration {
    private final Configuration config;
    private static final String EXPERIMENT_SUBJECT = "experiment.subject";
    private static final String EXPERIMENT_REPEATS = "experiment.repeats";
    private static final String EXPERIMENT_VARIANT_COUNT = "experiment.variant.count";
    private static final String EXPERIMENT_DIR_MAIN = "experiment.dir.main";
    private static final String EXPERIMENT_DIR_DATASET = "experiment.dir.dataset";
    private static final String EXPERIMENT_DIR_SPL = "experiment.dir.spl";
    private static final String EXPERIMENT_DEBUG = "experiment.debug";
    private static final String EXPERIMENT_LOGGER_LEVEL = "experiment.logger.level";
    private static final String EXPERIMENT_STARTID = "experiment.startid";

    public ExperimentConfiguration(final File propertiesFile) {
        final Parameters params = new Parameters();
        try {
            final var builder =
                    new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                            .configure(params.properties()
                                    .setFile(propertiesFile)
                                    .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
            this.config = builder.getConfiguration();
        } catch (ConfigurationException e) {
            System.err.println("Was not able to load properties file " + propertiesFile);
            throw new RuntimeException(e);
        }
    }

    public EExperimentalSubject EXPERIMENT_SUBJECT() {
        return EExperimentalSubject.valueOf(config.getString(EXPERIMENT_SUBJECT));
    }

    public int EXPERIMENT_REPEATS() {
        return config.getInt(EXPERIMENT_REPEATS);
    }

    public int EXPERIMENT_VARIANT_COUNT() {
        return config.getInt(EXPERIMENT_VARIANT_COUNT);
    }

    public String EXPERIMENT_DIR_MAIN() {
        return config.getString(EXPERIMENT_DIR_MAIN);
    }

    public String EXPERIMENT_DIR_DATASET() {
        return config.getString(EXPERIMENT_DIR_DATASET);
    }

    public String EXPERIMENT_DIR_SPL() {
        return config.getString(EXPERIMENT_DIR_SPL);
    }

    public Boolean EXPERIMENT_DEBUG() {
        return config.getBoolean(EXPERIMENT_DEBUG);
    }

    public LogLevel EXPERIMENT_LOGGER_LEVEL() {
        return LogLevel.valueOf(config.getString(EXPERIMENT_LOGGER_LEVEL));
    }

    public int EXPERIMENT_START_ID() {return config.getInt(EXPERIMENT_STARTID, 0);}
}
