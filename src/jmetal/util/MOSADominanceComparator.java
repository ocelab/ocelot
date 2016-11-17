package jmetal.util;

import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.genetic.many_objective.MOSABranchCoverageProblem;
import it.unisa.ocelot.util.Debugger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jmetal.core.Solution;

/**
 * This class implements a <code>Comparator</code> (a method for comparing
 * <code>Solution</code> based on the concept of Pareto Dominance, as proposed
 * by Panichella et al. in "Reformulating Branch Coverage as a Many-Objective
 * Optimization Problem", considering only the non-dominance relation for the
 * uncovered branches in an <code>MOSABranchCoverageProblem</code>
 * 
 * @author giograno
 *
 */
@SuppressWarnings("rawtypes")
public class MOSADominanceComparator implements Comparator {

	private final List<LabeledEdge> target;

	/**
	 * Constructor of MOSADominanceComparator class
	 * 
	 * @param uncoveredBranches
	 */
	public MOSADominanceComparator(List<LabeledEdge> uncoveredBranches) {
		this.target = new ArrayList<>(uncoveredBranches);
	}

	/**
	 * Compares two solutions.
	 * 
	 * @param object1
	 *            Object representing the first <code>Solution</code>.
	 * @param object2
	 *            Object representing the second <code>Solution</code>.
	 * @return -1, or 0, or 1 if solution1 dominates solution2, both are
	 *         non-dominated, or solution1 is dominated by solution22,
	 *         respectively.
	 */
	public int compare(Object o1, Object o2) {
		if (o1 == null)
			return 1;
		else if (o2 == null)
			return -1;

		Solution solution1 = (Solution) o1;
		Solution solution2 = (Solution) o2;

		int dominate1; // dominate1 indicates if some objective of solution1
						// dominates the same objective in solution2. dominate2
		int dominate2; // is the complementary of dominate1.

		dominate1 = 0;
		dominate2 = 0;

		int flag; // stores the result of the comparison

		// Equal number of violated constraints. Applying a dominance Test then
		double value1, value2;

		for (LabeledEdge uncoveredBranch : target) {
			if (uncoveredBranch.isCovered())
				continue; //branch covered
			
			value1 = solution1.getObjective(uncoveredBranch.getObjectiveID());
			value2 = solution2.getObjective(uncoveredBranch.getObjectiveID());

			if (value1 < value2) {
				flag = -1;
			} else if (value1 > value2) {
				flag = 1;
			} else {
				flag = 0;
			}

			if (flag == -1) {
				dominate1 = 1;
			}

			if (flag == 1) {
				dominate2 = 1;
			}
		}

		if (dominate1 == dominate2) {
			return 0; // No one dominate the other
		}
		if (dominate1 == 1) {
			return -1; // solution1 dominate
		}
		return 1; // solution2 dominate
	}

}
