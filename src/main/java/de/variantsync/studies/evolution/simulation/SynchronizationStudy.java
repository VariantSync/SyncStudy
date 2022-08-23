package de.variantsync.studies.evolution.simulation;

import de.variantsync.studies.evolution.simulation.experiment.ExperimentConfiguration;
import de.variantsync.studies.evolution.simulation.experiment.Experiment;

import java.io.File;

public class SynchronizationStudy {
    public static void main(final String... args) {
        if (args.length < 1) {
            System.err.println("The first argument should provide the path to the configuration file that is to be used");
        }
        final ExperimentConfiguration config = new ExperimentConfiguration(new File(args[0]));

        final Experiment experiment = new Experiment(config);

        try {
            experiment.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            System.exit(0);
        }
    }
}