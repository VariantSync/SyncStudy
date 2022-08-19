package de.variantsync.studies.evolution.variability.sequenceextraction;

import de.variantsync.studies.evolution.repository.Commit;
import de.variantsync.studies.evolution.util.list.NonEmptyList;
import de.variantsync.studies.evolution.util.list.StackUtil;
import de.variantsync.studies.evolution.variability.EvolutionStep;
import de.variantsync.studies.evolution.variability.SPLCommit;
import de.variantsync.studies.evolution.variability.SequenceExtractor;
import de.variantsync.studies.evolution.variability.VariabilityDataset;

import java.util.*;
import java.util.stream.Collectors;

public class Domino implements SequenceExtractor {
    /**
     * Returns a sorted lists of {@link SPLCommit}.
     * Each sublist corresponds to a successive chains of commits that directly follow each other.
     */
    @Override
    public List<NonEmptyList<SPLCommit>> extract(final Collection<SPLCommit> commits) {
        final Stack<Stack<SPLCommit>> dominos =
                Sort(VariabilityDataset.streamEvolutionSteps(commits)::iterator);
        return dominos.stream()
                .map(s -> new NonEmptyList<>(new ArrayList<>(s)))
                .collect(Collectors.toList());
    }

    public static <C extends Commit> Stack<Stack<C>> Sort(final Iterable<EvolutionStep<C>> dominos) {
        /*
         * First, sort each domino into a set chains.
         * For each domino, we sort it into a chain of other dominos if there is a suitable chain.
         * If there is none, this domino forms a new chain.
         */
        final Stack<Stack<C>> chains = new Stack<>();

//        System.out.println("Step 1: Build Chains");
        buildChains : for (final EvolutionStep<C> domino : dominos) {
//            System.out.println("  Found " + domino);
            // push the domino to an existing stack if possible
            for (final Stack<C> row : chains) {
                if (row.peek().equals(domino.parent())) {
                    row.push(domino.child());
//                    System.out.println("  Pushed it to chain " + row);
                    continue buildChains;
                }
            }

            // if there was no suitable stack, create one
            final Stack<C> newRow = new Stack<>();
            newRow.add(domino.parent());
            newRow.add(domino.child());
            chains.push(newRow);
//            System.out.println("  Created new chain " + newRow);
        }


//        System.out.println("Result of step 1: " + chains);

        /*
         * Second, merge all chains by stacking them ontop of each other.
         * Repeat this until we cannot merge any chains.
         */
//        System.out.println("Step 2: Merge Chains");
        final Stack<Stack<C>> finalRows = new Stack<>();
        while (!chains.isEmpty()) {
            // Pick one of the chains ...
            Stack<C> current = chains.pop();
//            System.out.println("  Inspecting " + current);
            // ... and merge it with the other chains as long as possible.
            final Set<Stack<C>> mergedStacks = new HashSet<>();
            do {
                mergedStacks.clear();
                for (final Stack<C> other : chains) {
                    if (current.firstElement().equals(other.lastElement())) {
//                        System.out.println("    pushing it to " + other);
                        other.pop();
                        StackUtil.pushAToB(current, other);
                        current = other;
//                        System.out.println("    got " + current);
                        mergedStacks.add(other);
                    } else if (other.firstElement().equals(current.lastElement())) {
//                        System.out.println("    pushed " + other + " onto it");
                        current.pop();
                        StackUtil.pushAToB(other, current);
//                        System.out.println("    got " + current);
                        mergedStacks.add(other);
                    }
                }
                chains.removeAll(mergedStacks);
            } while (!mergedStacks.isEmpty());

            finalRows.push(current);
        }

        return finalRows;
    }
}
