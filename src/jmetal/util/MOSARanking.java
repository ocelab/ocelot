package jmetal.util;

import it.unisa.ocelot.c.cfg.LabeledEdge;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;

import jmetal.core.SolutionSet;

/**
 * This class implements some facilities for ranking solutions. Given a
 * <code>SolutionSet</code> object, their solution are ranked according to
 * scheme proposed in MOSA algorithm, proposed by Panichella et al in
 * "Reformulating Branch Coverage as a Many-Objective Optimization Problem". The
 * first non dominated front, having rank 0, is obtained with their proposed
 * preference criterion. Remaining solution are ranked similarly as proposed in
 * NGSA-II, with the difference that the we consider non-dominance relation only
 * for uncovered branches, using <code>MOSADominanceComparator</code>
 * 
 * @author giograno
 *
 */
public class MOSARanking {

	/**
	 * The <code>SolutionSet</code> to rank
	 */
	private SolutionSet solutionSet_;

	/**
	 * An array containing all the fronts found during the search
	 */
	private SolutionSet[] ranking_;
	
	private List<LabeledEdge> uncoveredBranches;

	/**
	 * stores a <code>MOSADominanceComparator</code> for dominance checking
	 */
	@SuppressWarnings("rawtypes")
	private static Comparator dominance_;

	/**
	 * Constructor
	 * 
	 * @param solutionSet
	 *            The <code>SolutionSet</code> to be ranked.
	 * @param allTargets
	 */
	@SuppressWarnings("unchecked")
	public MOSARanking(SolutionSet solutionSet, List<LabeledEdge> allTargets) {
		solutionSet_ = solutionSet;
		this.uncoveredBranches = new ArrayList<>(allTargets);
		MOSARanking.dominance_ = new MOSADominanceComparator(this.uncoveredBranches);

		// dominateMe[i] contains the number of solutions dominating i
		int[] dominateMe = new int[solutionSet_.size()];

		// iDominate[k] contains the list of solutions dominated by k
		List<Integer>[] iDominate = new List[solutionSet_.size()];

		// front[i] contains the list of individuals belonging to the front i
		List<Integer>[] front = new List[solutionSet_.size() + 1];

		// flagDominate is an auxiliary encodings.variable
		int flagDominate;

		// Initialize the fronts
		for (int i = 0; i < front.length; i++)
			front[i] = new LinkedList<Integer>();

		/*** Fast non dominated sorting algorithm ***/

		for (int p = 0; p < solutionSet_.size(); p++) {
			// Initialize the list of individuals that i dominate and the number
			// of individuals that dominate me
			iDominate[p] = new LinkedList<Integer>();
			dominateMe[p] = 0;
		}
		for (int p = 0; p < (solutionSet_.size() - 1); p++) {
			// For all q individuals , calculate if p dominates q or vice versa
			for (int q = p + 1; q < solutionSet_.size(); q++) {

				// check dominance
				flagDominate = dominance_.compare(solutionSet.get(p),
						solutionSet.get(q));

				if (flagDominate == -1) {
					iDominate[p].add(q);
					dominateMe[q]++;
				} else if (flagDominate == 1) {
					iDominate[q].add(p);
					dominateMe[p]++;
				}
			}
		}

		// n.b: first front normally is on rank 0, so in MOSA rank 0 is reserved
		// for MOSA preference criterion

		// if nobody nominates p, p belongs to the first front
		for (int p = 0; p < solutionSet_.size(); p++) {
			if (dominateMe[p] == 0) {
				front[0].add(p);
				solutionSet.get(p).setRank(1); //rank 0 for best case
			}
		}

		// Obtain the rest of fronts
		int i = 0;
		Iterator<Integer> it1, it2; // Iterators
		while (front[i].size() != 0) {
			i++;
			it1 = front[i - 1].iterator();
			while (it1.hasNext()) {
				it2 = iDominate[it1.next()].iterator();
				while (it2.hasNext()) {
					int index = it2.next();
					dominateMe[index]--;
					if (dominateMe[index] == 0) {
						front[i].add(index);
						solutionSet_.get(index).setRank(i + 1);
					}
				}
			}
		}
		// <-

		ranking_ = new SolutionSet[i];
		//0,1,2,....,i-1 are front, then i fronts
		for (int j = 0; j < i; j++) {
			ranking_[j] = new SolutionSet(front[j].size());
			it1 = front[j].iterator();
			while (it1.hasNext()) {
				ranking_[j].add(solutionSet.get(it1.next()));
			}
		}

	} // Ranking

	/**
	 * Returns a <code>SolutionSet</code> containing the solutions of a given
	 * rank.
	 * 
	 * @param rank
	 *            The rank
	 * @return Object representing the <code>SolutionSet</code>.
	 */
	public SolutionSet getSubfront(int rank) {
		return ranking_[rank];
	} // getSubFront

	/**
	 * Returns the total number of subFronts founds.
	 */
	public int getNumberOfSubfronts() {
		return ranking_.length;
	} // getNumberOfSubfronts
} // Ranking
