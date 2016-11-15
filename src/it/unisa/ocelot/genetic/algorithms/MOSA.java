package it.unisa.ocelot.genetic.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.dominators.Dominators;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import it.unisa.ocelot.c.edge_graph.EdgeGraph;
import it.unisa.ocelot.c.edge_graph.EdgeWrapper;
import it.unisa.ocelot.genetic.OcelotAlgorithm;
import it.unisa.ocelot.genetic.many_objective.MOSABranchCoverageProblem;
import it.unisa.ocelot.util.Front;
import jmetal.core.Operator;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;
import jmetal.util.MOSARanking;
import jmetal.util.comparators.CrowdingComparator;
import jmetal.util.comparators.ObjectiveComparator;

/**
 * Implementation of MOSA (Many-Objective Sorting Algorithm) This algorithm is
 * proposed in the paper:
 * 
 * A. Panichella, F.M. Kifetew, P. Tonella
 * "Reformulating Branch Coverage as a Many-Objective Optimization Problem" ICST
 * 2015
 *
 * @author giograno
 *
 */
public class MOSA extends OcelotAlgorithm {

	// the final Solution Set produced by the algorithm
	private SolutionSet archive;
	// we store here the complete set of target for given problem
	private List<LabeledEdge> allTargets;
	private List<LabeledEdge> allBranches;
	private CFG controlFlowGraph;
	@SuppressWarnings("unused")
	private Set<LabeledEdge> coveredBranches;
	private EdgeGraph<CFGNode, LabeledEdge> edgeGraph;
	
	private Dominators<EdgeWrapper<LabeledEdge>, DefaultEdge> dominators;

	private List<Integer> evaluations;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param problem
	 *            Problem to solve
	 */
	public MOSA(MOSABranchCoverageProblem problem, List<LabeledEdge> targets) {
		super(problem);
		this.controlFlowGraph = problem.getControlFlowGraph();
		allTargets = new ArrayList<>(targets);
		evaluations = new ArrayList<>();
		this.allBranches = this.controlFlowGraph.getBranchesFromCFG();
		this.coveredBranches = new HashSet<>();
		
		
		edgeGraph = new EdgeGraph<CFGNode, LabeledEdge>(
				this.controlFlowGraph, this.controlFlowGraph.getStart(), this.controlFlowGraph.getEnd());
		
		this.dominators = new Dominators<EdgeWrapper<LabeledEdge>, DefaultEdge>(edgeGraph, edgeGraph.getStart());
	}

	/**
	 * Returns the overall test suite
	 * 
	 * @return a SolutionSet
	 */
	public SolutionSet getArchive() {
		return archive;
	}

	/**
	 * Runs the MOSA algorithm
	 * 
	 * @return a <code>SolutionSet</code> that is a set of non dominated
	 *         solutions as a result of the algorithm execution
	 */
	@Override
	public SolutionSet execute() throws JMException, ClassNotFoundException {
		int populationSize;
		int maxEvaluations;
		int evaluations;
		double maxCoverage;

		// Read the parameters
		populationSize = ((Integer) getInputParameter("populationSize")).intValue();
		maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
		if (getInputParameter("maxCoverage") != null)
			maxCoverage = ((Double) getInputParameter("maxCoverage")).doubleValue();
		else
			maxCoverage = 1.0;

		SolutionSet population;
		SolutionSet offspringPopulation;
		SolutionSet union;
		this.archive = new SolutionSet(this.allTargets.size());

		Operator mutationOperator;
		Operator crossoverOperator;
		Operator selectionOperator;

		// Initialize the variables
		population = new SolutionSet(populationSize);
		evaluations = 0;

		// Read the operators
		mutationOperator = operators_.get("mutation");
		crossoverOperator = operators_.get("crossover");
		selectionOperator = operators_.get("selection");

		// Create the initial solutionSet
		Solution newSolution;
		for (int i = 0; i < populationSize; i++) {
			newSolution = new Solution(problem_);
			problem_.evaluate(newSolution);
			evaluations++;
			population.add(newSolution);
		}

		// store every T.C. that covers previously uncovered branches in the
		// archive
		this.updateArchive(population, evaluations);

		while (evaluations < maxEvaluations && calculateCoverage() < maxCoverage) {
			offspringPopulation = new SolutionSet(populationSize);
			Solution[] parents = new Solution[2];

			for (int i = 0; i < (populationSize / 2); i++) {
				if (evaluations < maxEvaluations) {
					// obtain parents
					parents[0] = (Solution) selectionOperator.execute(population);
					parents[1] = (Solution) selectionOperator.execute(population);
					Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
					mutationOperator.execute(offSpring[0]);
					mutationOperator.execute(offSpring[1]);
					problem_.evaluate(offSpring[0]);
					problem_.evaluate(offSpring[1]);
					offspringPopulation.add(offSpring[0]);
					offspringPopulation.add(offSpring[1]);
					evaluations += 2;
				} // if

			} // for

			// Create the solutionSet union of solutionSet and offSpring
			union = ((SolutionSet) population).union(offspringPopulation);

			Front fronts = this.preferenceSorting(union);

			int remain = populationSize;
			int frontIndex = 0;
			SolutionSet front = null;
			population.clear(); // population t+1

			// Obtain the next front
			front = fronts.getFront(frontIndex);

			while ((remain > 0) && (remain >= front.size())) {

				this.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());

				// Add the individuals of this front
				for (int i = 0; i < front.size(); i++) {
					population.add(front.get(i));
				}

				// Decrement remain
				remain -= front.size();

				// Obtain next front
				frontIndex++;
				if (remain > 0)
					front = fronts.getFront(frontIndex);
			} // while

			// if remain is less than current front size, insert only the best
			if (remain > 0) {
				// current front contains the individuals to insert
				this.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
				front.sort(new CrowdingComparator());

				for (int i = 0; i < remain; i++)
					population.add(front.get(i));
			}

			this.updateArchive(population, evaluations);
			evaluations++;

		}// while
		
		this.algorithmStats.setEvaluations(evaluations);

		return this.archive;
	}

	/**
	 * Stores every test case that covers previous uncovered branches in the
	 * archive variable as a candidate test case to form the final test suite
	 * 
	 * @param candidates
	 *            a set of candidate test cases
	 */
	private void updateArchive(SolutionSet candidates, int evaluation) {

		for (LabeledEdge edge : allTargets) {

			if (edge.isCovered())
				continue;

			Iterator<Solution> iteratorCandidates = candidates.iterator();
			while (iteratorCandidates.hasNext()) {

				Solution currentCandidate = iteratorCandidates.next();
				double objectiveScore = currentCandidate.getObjective(edge.getObjectiveID());

				if (objectiveScore == 0.0) {
					// target covered
					this.setBranchCovered(edge);
					this.algorithmStats.log("Covered branch " + edge.toString() + " from "
							+ this.controlFlowGraph.getEdgeSource(edge).toString());

					// look for all target collateraly covered
					for (LabeledEdge collateralEdge : allTargets) {
						if (currentCandidate.getObjective(collateralEdge.getObjectiveID()) == 0.0
								&& !collateralEdge.isCovered()) {
							this.setBranchCovered(collateralEdge);
							this.algorithmStats.log("Collaterally covered "
									+ collateralEdge.toString()
									+ " from "
									+ this.controlFlowGraph.getEdgeSource(collateralEdge)
											.toString());
						}
					}
						

					archive.add(currentCandidate);
					evaluations.add(evaluation);
					break;
				}
			} // while candidates
		} // while targets
	}
	
	private void setBranchCovered(LabeledEdge pEdge) {
		pEdge.setCovered();
		Set<EdgeWrapper<LabeledEdge>> edgeDominators = null;
		for (LabeledEdge branch : this.allBranches) {
			if (!allTargets.contains(branch)) {
				if (edgeDominators == null)
					edgeDominators = dominators.getStrictDominators(edgeGraph.getWrapper(pEdge));
				
				if (edgeDominators.contains(edgeGraph.getWrapper(branch))) {
					
					branch.setCovered();
				}
			}
		}
	}

	private Front preferenceSorting(SolutionSet candidates) {

		Front front = new Front();
		SolutionSet population = candidates;
		SolutionSet front_0 = new SolutionSet(allTargets.size());

		/*** preference criterion ***/
		double minimum_fitness = Double.MAX_VALUE;
		Solution t_best = null; // best test case
		Set<Solution> solutionsToDelete = new HashSet<>();

		for (LabeledEdge target : allTargets) {

			Iterator<Solution> populationIterator = population.iterator();
			while (populationIterator.hasNext()) {

				Solution currentSolution = populationIterator.next();
				int idObjective = target.getObjectiveID();
				double currentObjective = currentSolution.getObjective(idObjective);

				if (currentObjective < minimum_fitness) {
					minimum_fitness = currentObjective;
					t_best = currentSolution;
				}// end-if

			} // end-while
			t_best.setRank(0); // set rank 0 for preference criterion
			front_0.add(t_best); // adding to front 0
			solutionsToDelete.add(t_best);

		} // end for

		front.addFront(front_0);

		// Remotion of solution in first front from overall population
		Iterator<Solution> populationIterator = population.iterator();
		while (populationIterator.hasNext()) {
			Solution currentSolution = populationIterator.next();
			if (solutionsToDelete.contains(currentSolution))
				populationIterator.remove();
		}

		/*** fast-non dominated-sort ***/

		MOSARanking ranking = new MOSARanking(population, allTargets);

		int remain = population.size();
		int front_number = 0;
		SolutionSet currentFront = new SolutionSet(candidates.size());

		while (remain > 0) {
			currentFront = ranking.getSubfront(front_number);
			remain -= currentFront.size();
			front.addFront(currentFront);
			front_number++;
		}

		return front;
	}

	public void crowdingDistanceAssignment(SolutionSet solutionSet, int numberOfObjects) {
		int size = solutionSet.size();

		if (size == 0) {
			return;
		}

		if (size == 1) {
			solutionSet.get(0).setCrowdingDistance(Double.POSITIVE_INFINITY);
			return;
		}

		// initialize to 0.0 all crowding distances
		for (int i = 0; i < size; i++)
			solutionSet.get(i).setCrowdingDistance(0.0);

		double minObjective = 0.0;
		double maxObjective = 0.0;
		double distance = 0.0;

		for (int i = 0; i < numberOfObjects; i++) {
			// sort the population by current object
			//try {
			solutionSet.sort(new ObjectiveComparator(i));
			//} catch (IllegalArgumentException e) {
			//	System.out.println("ERROR");
			//}
			minObjective = solutionSet.get(0).getObjective(i);
			maxObjective = solutionSet.get(size - 1).getObjective(i);

			// set infinity crowding distance for first and last element
			Solution current0 = new Solution(solutionSet.get(0));
			current0.setCrowdingDistance(Double.POSITIVE_INFINITY);
			solutionSet.replace(0, current0); // replace needed to avoid
												// override
			Solution currentLast = new Solution(solutionSet.get(size - 1));
			currentLast.setCrowdingDistance(Double.POSITIVE_INFINITY);
			solutionSet.replace(size - 1, currentLast);

			for (int j = 1; j < size - 1; j++) {
				distance = solutionSet.get(j + 1).getObjective(i)
						- solutionSet.get(j - 1).getObjective(i);

				// avoid division by 0 that leads to NaN values
				if (maxObjective - minObjective == 0)
					distance = 0.0;
				else
					distance = distance / (maxObjective - minObjective);

				distance += solutionSet.get(j).getCrowdingDistance();
				Solution current = new Solution(solutionSet.get(j));
				current.setCrowdingDistance(distance);
				solutionSet.replace(j, current);
			}
		} // for
	}

	@SuppressWarnings("unused")
	private boolean allTargetsCovered() {
		for (LabeledEdge edge : allTargets)
			if (!(edge.isCovered()))
				return false;

		return true;
	}
	
	private double calculateCoverage() {
		double covered = 0;
		int total = 0;
		for (LabeledEdge edge : allBranches) {
			if (edge.isCovered())
				covered++;
			else {
//				System.out.println("Branch " + edge + " from " + this.controlFlowGraph.getEdgeSource(edge));
			}
			total++;
		}
		
		double coverage = covered / total;
//		System.out.println(coverage);
		return coverage;
	}

	public List<Integer> getEvaluations() {
		return evaluations;
	}
}
