package de.variantsync.studies.evolution.feature.sampling;

import de.variantsync.studies.evolution.feature.Variant;
import de.variantsync.studies.evolution.feature.config.SimpleConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class LinuxKernel {
    private static final String[] DISTRO_CONFIGS = new String[] {
            "distro-configs/debian.config",
            "distro-configs/fedora.config",
            "distro-configs/mint.config",
            "distro-configs/mx-linux.config",
            "distro-configs/ubuntu.config"
    };

    public static Sample GetSample() throws IOException {
        final List<Variant> variants = new ArrayList<>(DISTRO_CONFIGS.length);

        for (final String res : DISTRO_CONFIGS) {
            try (final InputStream is = Objects.requireNonNull(Sample.class.getClassLoader().getResourceAsStream(res));
                 final Scanner scanner = new Scanner(is)) {
                final List<String> lines = new LinkedList<>();
                while (scanner.hasNext()) {
                    lines.add(scanner.nextLine());
                }

                final List<String> activeFeatures = lines.stream()
                        .filter(l -> !l.startsWith("#")) // filter comments
                        .map(l -> l.split("=")[0].trim()) // get names of active features
                        .filter(l -> !l.isEmpty()) // remove empty lines?
                        .collect(Collectors.toList());
                final String name = res.split("/")[1].split("\\.")[0];
                variants.add(new Variant(name, new SimpleConfiguration(activeFeatures)));
            }
        }

        return Sample.of(variants);
    }
}
