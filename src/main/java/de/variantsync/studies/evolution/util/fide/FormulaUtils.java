package de.variantsync.studies.evolution.util.fide;

import de.variantsync.studies.evolution.util.fide.bugfix.FixTrueFalse;
import org.prop4j.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FormulaUtils {
    public static Node negate(final Node node) {
        if (node instanceof Literal l) {
            return negate(l);
        }

        return new Not(node);
    }

    public static Literal negate(final Literal lit) {
        if (lit == null || lit.var == null) {
            throw new NullPointerException();
        }
        return new Literal(lit.var, !lit.positive);
    }

    public static Node AndSimplified(final Node a, final Node b) {
        if (FixTrueFalse.isTrue(a)) {
            return b;
        } else if (FixTrueFalse.isTrue(b)) {
            return a;
        } else if (FixTrueFalse.isFalse(a) || FixTrueFalse.isFalse(b)) {
            return FixTrueFalse.False;
        }

        final And result = new And(a, b);
        flatten(result);
        return result;
    }

    /**
     * This method inlines all recursive Ands into the top level And, passed as argument.
     */
    public static void flatten(final And and) {
        final List<And> redundantChildren = new ArrayList<>();

        do {
            final List<Node> andsChildren = new ArrayList<>(Arrays.asList(and.getChildren()));
            for (final And redundantChild : redundantChildren) {
                andsChildren.remove(redundantChild);
                andsChildren.addAll(Arrays.asList(redundantChild.getChildren()));
            }
            redundantChildren.clear();
            and.setChildren(andsChildren.toArray());

            for (final Node child : and.getChildren()) {
                if (child instanceof And) {
                    redundantChildren.add((And)child);
                }
            }
        } while (!redundantChildren.isEmpty());
    }

    public static String toString(final Node formula, final String[] symbols) {
        final NodeWriter writer = new NodeWriter(formula);
        writer.setNotation(NodeWriter.Notation.INFIX);
        writer.setEnquoteWhitespace(false);
        writer.setEnforceBrackets(true);
        writer.setSymbols(symbols);
        return writer.nodeToString();
    }
}
