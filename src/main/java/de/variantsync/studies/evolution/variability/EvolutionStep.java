package de.variantsync.studies.evolution.variability;

import de.variantsync.studies.evolution.repository.Commit;

public record EvolutionStep<T extends Commit>(T parent, T child) {
    @Override
    public String toString() {
        return "(" + parent  + ", " + child + ")";
    }
}
