package it.unisa.ocelot.c.cfg.paths;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.LabeledEdge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import jmetal.core.Solution;
import net.sf.javailp.Linear;
import net.sf.javailp.OptType;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.SolverFactory;
import net.sf.javailp.SolverFactoryGLPK;
import net.sf.javailp.SolverFactoryLpSolve;
import net.sf.javailp.SolverFactorySAT4J;
import net.sf.javailp.VarType;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class PathSearchSimplex {
	private CFG cfg;
	private List<RealVector> basisPaths;
	private int basisPathSize;
	private int[] coefficients;
	private int timesCovered;
	private SolverFactory factory;
	private Solver solver;
	
	public PathSearchSimplex(CFG pCFG, List<RealVector> pBasisPaths, int pTimesCovered) {
		this.cfg = pCFG;
		this.basisPaths = pBasisPaths;
		this.basisPathSize = this.cfg.edgeSet().size();
		this.timesCovered = pTimesCovered;
		
		this.factory = new SolverFactoryLpSolve(); // use lp_solve
		this.factory.setParameter(Solver.VERBOSE, 0);
		this.factory.setParameter(Solver.TIMEOUT, 100); // set timeout to 100 seconds
		this.solver = factory.get();
	}
	
	public void solve() {
		this.solve(new ArrayList<LabeledEdge>(this.cfg.edgeSet()));
	}
	
	public void solve(Collection<LabeledEdge> pToCover) {
		List<LabeledEdge> sortedEdges = new ArrayList<LabeledEdge>(this.cfg.edgeSet());
		Collections.sort(sortedEdges);
		
		LabeledEdge startingEdge = new ArrayList<LabeledEdge>(this.cfg.outgoingEdgesOf(this.cfg.getStart())).get(0);
		
		int[] toCover = new int[sortedEdges.size()];
		for (int i = 0 ; i < sortedEdges.size(); i++)
			if (pToCover.contains(sortedEdges.get(i)) || sortedEdges.get(i).equals(startingEdge))
				toCover[i] = 1;
		
		
		Problem problem = new Problem();
		Linear objective = new Linear();
		for (int i = 0; i < this.basisPaths.size(); i++)
			objective.add(1, "C"+i);
		
		problem.setObjective(objective, OptType.MIN);
		
		for (int i = 0; i < this.basisPathSize; i++) {
			Linear constraint = new Linear();
			for (int j = 0; j < this.basisPaths.size(); j++) {
				constraint.add(this.basisPaths.get(j).getEntry(i), "C"+j);
			}
			problem.add(constraint, ">=", this.timesCovered * toCover[i]);
			
		}
		
		for (int i = 0; i < this.basisPaths.size(); i++) {
			problem.setVarType("C"+i, VarType.INT);
			problem.setVarLowerBound("C"+i, Integer.MIN_VALUE);
			problem.setVarUpperBound("C"+i, Integer.MAX_VALUE);
		}
		
		Result result = solver.solve(problem);
		
		this.coefficients = new int[this.basisPaths.size()];
		for (int i = 0; i < this.basisPaths.size(); i++)
			this.coefficients[i] = (Integer)result.get("C"+i);
	}
	
	public List<List<LabeledEdge>> getChosenPaths() {
		int[] data = this.coefficients; 
		
		RealVector currentSolution = new ArrayRealVector(new double[this.cfg.edgeSet().size()]);
		for (int i = 0; i < data.length; i++) {
			double currentCoefficient = data[i];
			RealVector currentBasisPath = this.basisPaths.get(i);
			RealVector currentBasisPathTotal = currentBasisPath.mapMultiply(currentCoefficient);
			
			currentSolution = currentSolution.add(currentBasisPathTotal);
		}
		
		RealVector copyCoefficients = currentSolution.copy();
		List<List<LabeledEdge>> result = new ArrayList<List<LabeledEdge>>();
		while (copyCoefficients.getMaxValue() > 0 || copyCoefficients.getMinValue() < 0) {
			System.out.println("Trying...");
			copyCoefficients = currentSolution.copy();
			result = this.getPaths(copyCoefficients);
		}
		
		return result;
	}
	
	private List<List<LabeledEdge>> getPaths(RealVector coefficients) {
		List<LabeledEdge> cliche = new ArrayList<LabeledEdge>(this.cfg.edgeSet());
		Collections.sort(cliche);
		
		Random random = new Random();
				
		List<List<LabeledEdge>> paths = new ArrayList<List<LabeledEdge>>();
		List<LabeledEdge> currentPath = new ArrayList<LabeledEdge>();
		CFGNode currentNode = this.cfg.getStart();
		
		while (true) {
			List<LabeledEdge> edges = new ArrayList<LabeledEdge>();
			
			//Gets all the outgoing edges from the current node that are available
			for (LabeledEdge outgoingEdge : this.cfg.outgoingEdgesOf(currentNode)) {
				if (coefficients.getEntry(cliche.indexOf(outgoingEdge)) != 0.0)
					edges.add(outgoingEdge);
			}
			
			//Exit point of the loop: if no edges are available, exit.
			//WARNING: it doesn't check if the paths are correct!
			if (edges.size() == 0)
				return paths;
			
			//Picks a random edge from the available ones
			LabeledEdge chosenEdge = edges.get(random.nextInt(edges.size()));
			int chosenIndex = cliche.indexOf(chosenEdge);
			
			//Updates the coefficients
			coefficients.setEntry(chosenIndex, coefficients.getEntry(chosenIndex)-1);
			
			//Updates current path and current node
			currentPath.add(chosenEdge);
			currentNode = this.cfg.getEdgeTarget(chosenEdge);
			
			//If we reach the end of the CFG, we go back to the start
			if (currentNode.equals(this.cfg.getEnd())) {
				currentNode = this.cfg.getStart();
				paths.add(currentPath);
				currentPath = new ArrayList<LabeledEdge>();
			}
		}
	}
}
