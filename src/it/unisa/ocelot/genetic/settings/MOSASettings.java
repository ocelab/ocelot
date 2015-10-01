package it.unisa.ocelot.genetic.settings;

import java.util.List;

import jmetal.core.Algorithm;
import jmetal.core.Problem;
import jmetal.util.JMException;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.algorithms.MOSA;
import it.unisa.ocelot.genetic.many_objective.MOSABranchCoverageProblem;

public class MOSASettings extends GASettings {
	private List<LabeledEdge> targetEdges;
	public MOSASettings(Problem pProblem) {
		super(pProblem);
	}
	
	public MOSASettings(Problem pProblem, ConfigManager pConfig, List<LabeledEdge> pTargetEdges) {
		super(pProblem, pConfig);
		
		this.targetEdges = pTargetEdges;
	}
	
	public Algorithm configure() throws JMException {
		Algorithm algorithm = new MOSA((MOSABranchCoverageProblem)problem_, targetEdges);
		
		return super.configure(algorithm);
    }
}
