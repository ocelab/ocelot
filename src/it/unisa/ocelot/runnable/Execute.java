package it.unisa.ocelot.runnable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGVisitor;
import it.unisa.ocelot.c.compiler.GCC;
import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.EventsHandler;
import it.unisa.ocelot.simulator.Simulator;
import it.unisa.ocelot.simulator.listeners.ApproachLevelListener;
import it.unisa.ocelot.simulator.listeners.CoverageSimulatorListener;
import it.unisa.ocelot.simulator.listeners.TestSimulatorListener;
import it.unisa.ocelot.util.Utils;

public class Execute {
	static {
		System.loadLibrary("Test");
	}
	
	public static void main(String[] args) throws Exception {
		CFG cfg = buildCFG("testobject/main.c");
		
		CBridge bridge = new CBridge();
		EventsHandler h = new EventsHandler();
		
		Object[] arguments = new Object[4];
		arguments[0] = new Integer(3); //10
		arguments[1] = new Integer(12); //10
		arguments[2] = new Integer(4);  //5
		
		bridge.getEvents(h, arguments);
		
		cfg.setTarget(cfg.getStart().navigate(cfg).goFlow().goFlow().goFalse().goFlow().goFlow().goFalse().goFlow().goFalse().goTrue().node());
		System.out.println("Target node:" + cfg.getTarget().toString());
		
		Simulator simulator = new Simulator(cfg, h.getEvents());
		
		CoverageSimulatorListener listener = new CoverageSimulatorListener(cfg);
		
		ApproachLevelListener approachLevelListener = new ApproachLevelListener(cfg);
		
		simulator.addListener(new TestSimulatorListener());
		simulator.addListener(listener);
		simulator.addListener(approachLevelListener);
		
		System.out.println("Simulating with " + StringUtils.join(arguments, " "));
		System.out.println("------------------------------");
		simulator.simulate();
		System.out.println("------------------------------");
		
		System.out.println("Branch coverage: " + listener.getBranchCoverage());
		System.out.println("Block coverage: " + listener.getBlockCoverage());
		System.out.println("------------------------------");
		System.out.println("Approach level: " + approachLevelListener.getApproachLevel());
		System.out.println("------------------------------");
		if (simulator.isSimulationCorrect())
			System.out.println("Simulation correct!");
		else
			System.out.println("Simulation error!");
	}
	
	public static CFG buildCFG(String pSourceFile) throws Exception {
		String code = Utils.readFile(pSourceFile);
		CFG graph = new CFG();
		
		IASTTranslationUnit translationUnit = GCC.getTranslationUnit(code.toCharArray(), pSourceFile);
		CFGVisitor visitor = new CFGVisitor(graph);
		
		translationUnit.accept(visitor);
		
		return graph;
	}
}