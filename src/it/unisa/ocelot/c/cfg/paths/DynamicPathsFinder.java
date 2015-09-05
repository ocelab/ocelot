package it.unisa.ocelot.c.cfg.paths;

import java.util.List;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.KShortestPaths;

import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.c.cfg.TooManyInfeasiblePathsException;
import it.unisa.ocelot.c.cfg.WeightedCFG;

public class DynamicPathsFinder {
	private static final int MAX_FAILED_PATHS = 20;
	
	private WeightedCFG cfg;
	private int k;
	private int maxK;
	private KShortestPaths<CFGNode, LabeledEdge> algorithm;

	public DynamicPathsFinder(WeightedCFG pCFG) {
		this.cfg = pCFG;
		
		this.k = 0;
		this.maxK = 5;
		this.algorithm = new KShortestPaths<CFGNode, LabeledEdge>(this.cfg, pCFG.getStart(), this.maxK);
	}
	
	public void setExecutedPath(List<LabeledEdge> path) {
		for (LabeledEdge edge : path)
			this.cfg.setEdgeWeight(edge, 1);
		
		this.clearInfeasiblePaths();
	}
	
	public List<LabeledEdge> getMaxCoveragePath() {
		List<GraphPath<CFGNode, LabeledEdge>> paths = this.algorithm.getPaths(this.cfg.getEnd());
		
		return paths.get(this.k).getEdgeList();
	}

	public void notifyInfeasiblePath() throws TooManyInfeasiblePathsException {
		this.k++;
		
		if (this.k >= this.maxK) {
			this.maxK *= 2;
			this.algorithm = new KShortestPaths<CFGNode, LabeledEdge>(this.cfg, this.cfg.getStart(), this.maxK);
		}
		
		if (this.k > MAX_FAILED_PATHS)
			throw new TooManyInfeasiblePathsException("Exceded " + MAX_FAILED_PATHS + " infeasible paths");
	}
	
	public void clearInfeasiblePaths() {
		this.algorithm = new KShortestPaths<CFGNode, LabeledEdge>(this.cfg, this.cfg.getStart(), 5);
		this.k = 0;
	}
}
