package de.variantsync.studies.evolution.variability;

import de.variantsync.studies.evolution.util.Logger;
import de.variantsync.studies.evolution.util.list.NonEmptyList;
import de.variantsync.studies.evolution.variability.sequenceextraction.CleaningEvolutionStepsStream;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VariabilityDataset {
    private final Set<SPLCommit> allCommits;
    private final List<SPLCommit> successCommits;
    private final List<SPLCommit> errorCommits;
    private final List<SPLCommit> partialSuccessCommits;

    public VariabilityDataset(@NotNull final List<SPLCommit> successCommits, @NotNull final List<SPLCommit> errorCommits, @NotNull final List<SPLCommit> partialSuccessCommits) {
        this.successCommits = successCommits;
        this.errorCommits = errorCommits;
        this.partialSuccessCommits = partialSuccessCommits;
        this.allCommits = new HashSet<>();
        this.allCommits.addAll(successCommits);
        this.allCommits.addAll(errorCommits);
        this.allCommits.addAll(partialSuccessCommits);
        if (allCommits.size() != successCommits.size() + errorCommits.size() + partialSuccessCommits.size()) {
            final String errorMessage = "Some of the dataset's commits belong to more than one category (SUCCESS | ERROR | INCOMPLETE_PC)";
            Logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * @return A Set of all commits contained in this dataset.
     */
    public Set<SPLCommit> getAllCommits() {
        return allCommits;
    }

    /**
     * @return A List of all commits for which variability data was extracted successfully.
     */
    public List<SPLCommit> getSuccessCommits() {
        return successCommits;
    }

    /**
     * @return A List of all commits for which variability data could not be extracted.
     */
    public List<SPLCommit> getErrorCommits() {
        return errorCommits;
    }

    /**
     * @return A List of all commits for which variability data was extracted partially.
     */
    public List<SPLCommit> getPartialSuccessCommits() {
        return partialSuccessCommits;
    }

    /**
     * Retrieve all sequences of usable commits in form of a VariabilityHistory instance.
     *
     * <p>
     * The retrieved VariabilityHistory is a list that contains commit sequences that can be used for our de.variantsync.studies.evolution study. Each
     * sequence consists of at least two commits. A sequence is a list of commits, where a commit at index
     * i is the logical parent of the commit at index i+1.
     * </p>
     *
     * @return All sequences of commits that are usable in our de.variantsync.studies.evolution study.
     */
    public VariabilityHistory getVariabilityHistory(final SequenceExtractor sequenceExtractor) {
        // Build a VariabilityHistory instance by applying the provided function to the set of success commits
        final List<NonEmptyList<SPLCommit>> history = sequenceExtractor.extract(this.successCommits);

        if (history.isEmpty()) {
            Logger.error("There is no valid sequence of commits from which a VariabilityHistory can be built!");
            throw new IllegalStateException();
        } else {
            return new VariabilityHistory(new NonEmptyList<>(history));
        }
    }

    /**
     * Collects all de.variantsync.studies.evolution steps into a set.
     * @see #streamEvolutionSteps()
     */
    public Set<EvolutionStep<SPLCommit>> getEvolutionSteps() {
        return streamEvolutionSteps().collect(Collectors.toSet());
    }


    /**
     * Streams all valid pairs of success commits of this dataset.
     * @see #streamEvolutionSteps(Collection)
     */
    public Stream<EvolutionStep<SPLCommit>> streamEvolutionSteps() {
        return streamEvolutionSteps(successCommits);
    }

    /**
     * Returns a sorted stream of {@link EvolutionStep<SPLCommit>}. Pairs of commits that
     * have one commits in common will be returned sequentially (similar to playing domino).
     * Visited SPLCommit's will be cleaned via {@link SPLCommit#forget()}.
     * @see #streamEvolutionSteps(Collection)
     */
    public CleaningEvolutionStepsStream<SPLCommit> tidyStreamEvolutionSteps() {
        return new CleaningEvolutionStepsStream<>(streamEvolutionSteps()::iterator);
    }

    /**
     * Return the set of commit pairs (childCommit, parentCommit) for which the following holds:
     * <ul>
     *     <li>
     *         childCommit processed its corresponding SPL commit successfully, i.e., feature model and code variability was extracted
     *     </li>
     *     <li>
     *         The SPL commit, which was processed by childCommit, has exactly one parent commit in the SPL history (it is no merge commit)
     *     </li>
     *     <li>
     *         The parent of the SPL commit was also processed successfully and is represented by parentCommit
     *     </li>
     * </ul>
     *
     * @return Set of commit pairs that can be used in a variability de.variantsync.studies.evolution study
     */
    public static Stream<EvolutionStep<SPLCommit>> streamEvolutionSteps(final Collection<SPLCommit> successCommits) {
        return successCommits.stream()
                .map(c -> {
                    if (c.parents().isPresent()) {
                        final SPLCommit[] parents = c.parents().get();
                        // We only consider commits that did not process a merge
                        final boolean notAMerge = parents.length == 1;
                        final SPLCommit p = parents[0];
                        // We only consider commits that processed an SPL commit whose parent was also processed
                        final boolean parentSuccess = successCommits.contains(p);
                        if (notAMerge && parentSuccess) {
                            return new EvolutionStep<>(p, c);
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull);
    }
}