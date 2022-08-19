package de.variantsync.studies.evolution.variability.pc;

import de.variantsync.studies.evolution.sat.SAT;
import org.prop4j.Node;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Collections of methods for simplifying LineBasedAnnotations.
 */
public class LineBasedAnnotationSimplifier {
    /**
     * @return A simplified formula of the given annotation's feature mapping.
     */
    public static Node simplifiedMapping(final LineBasedAnnotation root) {
        // Actually we could employ presence condition simplification by Rhein et al. here.
        return root.getFeatureMapping().simplifyTree();
    }

    /**
     * Inlines all subtrees with redundant annotation. Example:
     *
     * #if A
     *   #if True
     *   #endif
     *   #if A || B
     *   #endif
     *   #if A
     *     #if B
     *     #endif
     *   #endif
     * #endif
     *
     * is simplified to
     *
     * #if A
     *   #if B
     *   #endif
     * #endif
     *
     * while keeping referenced line numbers intact.
     *
     * @return All non-redundant subtrees that should become the subtrees of root after simplification.
     */
    public static List<LineBasedAnnotation> flattenedSubtrees(final LineBasedAnnotation root) {
        final Queue<LineBasedAnnotation> subtreesToCheck = new LinkedList<>(root.getSubtrees());
        final List<LineBasedAnnotation> nonRedundantSubtrees = new ArrayList<>(subtreesToCheck.size());

        LineBasedAnnotation subtreeToCheck;
        while (!subtreesToCheck.isEmpty()) {
            subtreeToCheck = subtreesToCheck.poll();
            if (SAT.implies(root.getFeatureMapping(), subtreeToCheck.getFeatureMapping())) {
                subtreesToCheck.addAll(subtreeToCheck.getSubtrees());
            } else {
                nonRedundantSubtrees.add(subtreeToCheck);
            }
        }

        return nonRedundantSubtrees;
    }

    /**
     * Merges all adjacent annotations in the given list that are the same. Example:
     *
     * #if A
     * #endif
     * #if B
     * #endif
     * #if B
     * #endif
     *
     * is simplified to
     *
     * #if A
     * #endif
     * #if B
     * #endif
     *
     * while keeping referenced line numbers intact.
     * Does not check for formula equivalence (e.g., True <=> A or not A) but equality (True == True).
     *
     * @return All non-redundant subtrees that should become the subtrees of root after simplification.
     */
    public static List<LineBasedAnnotation> mergedEqualNeighbours(final List<LineBasedAnnotation> mappings) {
        if (mappings.isEmpty()) return mappings;

        // Merge similar neighbouring children
        final List<LineBasedAnnotation> simplifiedSubtrees = new ArrayList<>(mappings.size());
        LineBasedAnnotation currentSubtree = mappings.get(0);
        simplifiedSubtrees.add(currentSubtree);

        for (int subtreeIndex = 1; subtreeIndex < mappings.size(); ++subtreeIndex) {
            final LineBasedAnnotation nextSubtree = mappings.get(subtreeIndex);
            // If two neighbouring nodes are equal ...
            if (currentSubtree.getLineTo() + 1 == nextSubtree.getLineFrom() && currentSubtree.getFeatureMapping().equals(nextSubtree.getFeatureMapping())) {
                // ... merge them
                currentSubtree.addTraces(nextSubtree.getSubtrees());
                currentSubtree.setLineTo(nextSubtree.getLineTo());
            } else {
                // ... otherwise keep them as is.
                currentSubtree = nextSubtree;
                simplifiedSubtrees.add(currentSubtree);
            }
        }

        return simplifiedSubtrees;
    }

    /**
     * Simplifies the given annotations using all other simplification methods in this class.
     */
    public static void simplify(final LineBasedAnnotation root) {
        root.setFeatureMapping(simplifiedMapping(root));
        final List<LineBasedAnnotation> flattenedSubtrees = mergedEqualNeighbours(flattenedSubtrees(root));
        for (final LineBasedAnnotation child : flattenedSubtrees) {
            simplify(child);
        }
        root.setSubtrees(flattenedSubtrees);
    }
}
