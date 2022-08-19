package de.variantsync.studies.evolution.sat;

import de.variantsync.studies.evolution.util.fide.FormulaUtils;
import org.prop4j.And;
import org.prop4j.Equals;
import org.prop4j.Implies;
import org.prop4j.Node;
import org.prop4j.explain.solvers.SatSolver;
import org.prop4j.explain.solvers.SatSolverFactory;

public class SAT {
    public static boolean isSatisfiable(final Node formula) {
        final SatSolver solver = SatSolverFactory.getDefault().getSatSolver();
        solver.addFormulas(formula);
        return solver.isSatisfiable();
    }

    public static boolean isTautology(final Node formula) {
        return !isSatisfiable(FormulaUtils.negate(formula));
    }

    public static boolean implies(final Node left, final Node right) {
        return isTautology(new Implies(left, right));
    }

    public static boolean equivalent(final Node pc, final And and) {
        return isTautology(new Equals(pc, and));
    }
}
