package it.unisa.ocelot.c.genetic;

import java.util.List;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.CFGVisitor;
import it.unisa.ocelot.c.compiler.GCC;
import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.EventsHandler;
import it.unisa.ocelot.simulator.Simulator;
import it.unisa.ocelot.simulator.listeners.BDALListener;
import it.unisa.ocelot.simulator.listeners.TestSimulatorListener;
import it.unisa.ocelot.util.Utils;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.encodings.solutionType.ArrayRealAndBinarySolutionType;
import jmetal.encodings.solutionType.ArrayRealSolutionType;
import jmetal.encodings.variable.ArrayReal;
import jmetal.encodings.variable.Real;
import jmetal.util.JMException;

public class TargetCoverageProblem extends Problem {
	private static final long serialVersionUID = 1930014794768729268L;
	
	private CFG cfg;
	private Class<Object>[] parameters;
	
	public TargetCoverageProblem(String pSource, String pFunctionName, Class<Object>[] pParameters) throws Exception {
		this.cfg = buildCFG(pSource, pFunctionName);
		
		numberOfVariables_ = pParameters.length;
        numberOfObjectives_ = 1;
        numberOfConstraints_ = 0;
        problemName_ = "TestingPrioritizationProblem";

        solutionType_ = new ArrayRealSolutionType(this);

        length_ = new int[numberOfVariables_];
        lowerLimit_ = new double[numberOfVariables_];
        upperLimit_ = new double[numberOfVariables_];
        for (int i = 0; i < numberOfVariables_; i++) {
        	lowerLimit_[i] = 0;
        	upperLimit_[i] = 255;
        }
        
        this.parameters = pParameters;
	}
	
	public CFG getCFG() {
		return cfg;
	}
	
	public void setTarget(CFGNode pNode) {
		this.cfg.setTarget(pNode);
	}
	
	public void evaluate(Solution solution) throws JMException {
		Double[] variables = ((ArrayReal)solution.getDecisionVariables()[0]).array_;
		
		Object[] arguments = new Object[variables.length];
		for (int i = 0; i < variables.length; i++) {
			arguments[i] = this.getInstance(variables[i], this.parameters[i]);
		}
		
		CBridge bridge = new CBridge();
		
		EventsHandler handler = new EventsHandler();
		BDALListener bdalListener = new BDALListener(cfg);
		
		bridge.getEvents(handler, arguments);
		
		Simulator simulator = new Simulator(cfg, handler.getEvents());
		
		//System.out.println("Simulating with " + StringUtils.join(arguments, " "));
		
		simulator.addListener(bdalListener);
		
		simulator.simulate();
		
		solution.setObjective(0, bdalListener.getNormalizedBranchDistance() + bdalListener.getApproachLevel());
	}
	
	public CFG buildCFG(String pSourceFile, String pFunctionName) throws Exception {
		String code = Utils.readFile(pSourceFile);
		CFG graph = new CFG();
		
		IASTTranslationUnit translationUnit = GCC.getTranslationUnit(code.toCharArray(), pSourceFile);
		CFGVisitor visitor = new CFGVisitor(graph, pFunctionName);
		
		translationUnit.accept(visitor);
		
		return graph;
	}
	
	private Object getInstance(double pValue, Class<Object> pType) {
		if (pType.equals(Integer.class)) {
			return new Integer((int)pValue);
		} else if (pType.equals(Double.class)) {
			return new Double(pValue);
		}
		
		return new Double(pValue);
	}
}
