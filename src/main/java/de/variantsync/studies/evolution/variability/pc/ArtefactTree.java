package de.variantsync.studies.evolution.variability.pc;

import de.variantsync.studies.evolution.util.fide.FormulaUtils;
import de.variantsync.studies.evolution.util.io.CaseSensitivePath;
import org.prop4j.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents feature traces as a tree structure.
 * That is, nodes in subtrees inherit feature mappings from their ancestors in their presence condition.
 * @param <Child> The type of children that can be added to this tree. Only affects direct children, meaning
 *               that it is not transitive and grandchildren could be of another type.
 */
public abstract class ArtefactTree<Child extends ArtefactTree<?>> implements Artefact {
    private Node featureMapping;
    private final CaseSensitivePath file;
    private ArtefactTree<?> parent;
    protected List<Child> subtrees;

    /**
     * Creates a new empty tree (node) with the given feature mapping.
     */
    public ArtefactTree(final Node featureMapping) {
        this(featureMapping, new ArrayList<>(), null);
    }

    /**
     * Creates a new tree (node) with the given feature mapping and subtrees representing (content of) the given file.
     */
    public ArtefactTree(final Node featureMapping, final List<Child> subtrees, final CaseSensitivePath file) {
        Objects.requireNonNull(featureMapping);
        Objects.requireNonNull(subtrees);

        this.featureMapping = featureMapping;
        this.file = file;

        setSubtrees(subtrees);
    }

    protected void setFeatureMapping(final Node featureMapping) {
        this.featureMapping = featureMapping;
    }

    @Override
    public Node getFeatureMapping() {
        return featureMapping;
    }

    @Override
    public Node getPresenceCondition() {
        final Artefact parent = getParent();
        return parent == null ?
                featureMapping :
                FormulaUtils.AndSimplified(parent.getPresenceCondition(), featureMapping);
    }

    @Override
    public CaseSensitivePath getFile() {
        if (file != null) {
            return file;
        }
        if (parent != null) {
            return parent.getFile();
        }
        return null;
    }

    /**
     * Sets the parent of this (sub-)tree.
     * Does not perform relocations and is only used for internal use after other tree operations.
     * @param parent The new parent of this tree.
     */
    void setParent(final ArtefactTree<?> parent) {
        this.parent = parent;
    }

    /**
     * @return The parent of this (sub-)tree.
     */
    public ArtefactTree<?> getParent() {
        return parent;
    }

    /**
     * Replaces this trees subtrees with the given subtrees.
     * Previous subtrees will have their parent set to null.
     */
    protected void setSubtrees(final List<Child> subtrees) {
        // let all previous children forget their parent
        if (this.subtrees != null) {
            for (final Child c : this.subtrees) {
                c.setParent(null);
            }
        }

        this.subtrees = subtrees;

        // update the parent of the new children
        if (this.subtrees != null) {
            for (final Child c : this.subtrees) {
                c.setParent(this);
            }
        }
    }

    public List<Child> getSubtrees() {
        return subtrees;
    }

    public int getNumberOfSubtrees() {
        return subtrees.size();
    }

    public void addTraces(final Collection<Child> annotations) {
        for (final Child b : annotations) {
            addTrace(b);
        }
    }

    /**
     * Adds the given subtree to this tree.
     * Behaviour might change based on subclasses (e.g., for LineBasedAnnotation).
     * @param child The subtree to add.
     */
    public void addTrace(final Child child) {
        subtrees.add(child);
        child.setParent(this);
    }

    /**
     * Removes all subtrees.
     */
    public void clear() {
        for (final Child c : subtrees) {
            c.setParent(null);
        }
        subtrees.clear();
    }

    public boolean isLeaf() {
        return subtrees.isEmpty();
    }

    @Override
    public void simplify() {
        for (final Child c : subtrees) {
            c.simplify();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ArtefactTree<?> that = (ArtefactTree<?>) o;
        // don't compare parents so we only compare subtrees
        return featureMapping.equals(that.featureMapping) && subtrees.equals(that.subtrees);
    }

    @Override
    public int hashCode() {
        return Objects.hash(featureMapping, file /*, parent , subtrees*/);
    }
}
