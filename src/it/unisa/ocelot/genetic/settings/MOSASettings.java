package it.unisa.ocelot.genetic.settings;

import java.util.List;

import it.unisa.ocelot.genetic.StandardProblem;
import it.unisa.ocelot.genetic.encoding.graph.Graph;
import jmetal.core.Algorithm;
import jmetal.core.Problem;
import jmetal.util.JMException;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.algorithms.MOSA;
import it.unisa.ocelot.genetic.many_objective.MOSABranchCoverageProblem;

public class MOSASettings extends GASettings {
	private List<LabeledEdge> targetEdges;
	private ConfigManager config;
	
	public MOSASettings(Problem pProblem) {
		super(pProblem);
	}
	
	public MOSASettings(Problem pProblem, ConfigManager pConfig, List<LabeledEdge> pTargetEdges) {
		super(pProblem, pConfig);
		
		this.config = pConfig;
		
		this.targetEdges = pTargetEdges;
	}
	
	public Algorithm configure() throws JMException {
		StandardProblem sp = (StandardProblem)problem_;
		List<Graph> graphList = sp.getGraphList();

		Algorithm algorithm = new MOSA((MOSABranchCoverageProblem)problem_, targetEdges, graphList);
		algorithm.setInputParameter("maxCoverage", this.config.getRequiredCoverage());
		return super.configure(algorithm);
    }
}
