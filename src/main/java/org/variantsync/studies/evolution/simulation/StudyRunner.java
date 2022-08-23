package org.variantsync.studies.evolution.simulation;

import org.variantsync.studies.evolution.simulation.experiment.ExperimentConfiguration;
import org.variantsync.studies.evolution.simulation.experiment.SynchronizationStudy;

import java.io.File;

public class StudyRunner {
    public static void main(final String... args) {
        if (args.length < 1) {
            System.err.println("The first argument should provide the path to the configuration file that is to be used");
        }
        final ExperimentConfiguration config = new ExperimentConfiguration(new File(args[0]));

        final SynchronizationStudy synchronizationStudy = new SynchronizationStudy(config);

        try {
            synchronizationStudy.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            System.exit(0);
        }
    }
}