package it.unisa.ocelot.runnable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGVisitor;
import it.unisa.ocelot.c.compiler.GCC;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.EventsHandler;
import it.unisa.ocelot.simulator.Simulator;
import it.unisa.ocelot.simulator.listeners.BDALListener;
import it.unisa.ocelot.simulator.listeners.TestSimulatorListener;
import it.unisa.ocelot.util.Utils;

public class SimpleExecute {
	private static final String CONFIG_FILENAME = "config.properties";
	
	static {
		System.loadLibrary("Test");
	}
	
	public static void main(String[] args) throws Exception {
		ConfigManager.setFilename(CONFIG_FILENAME);
		ConfigManager config = ConfigManager.getInstance();
		
		CFG cfg = buildCFG(config.getTestFilename(), config.getTestFunction());
		
		CBridge bridge = new CBridge();
		EventsHandler h = new EventsHandler();
		
		Object[] arguments = config.getTestArguments();
		
		bridge.getEvents(h, arguments);
		
		cfg.setTarget(config.getTestTarget(cfg));
		System.out.println("Target node:" + cfg.getTarget().toString());
		
		Simulator simulator = new Simulator(cfg, h.getEvents());
		
		BDALListener bdalListener = new BDALListener(cfg);
		
		simulator.addListener(new TestSimulatorListener());
		simulator.addListener(bdalListener);

		System.out.println("Simulating with " + StringUtils.join(arguments, " "));
		
		simulator.simulate();
		
		System.out.println("AL: " + bdalListener.getApproachLevel());
		System.out.println("NBD: " + bdalListener.getNormalizedBranchDistance());
		
		if (simulator.isSimulationCorrect())
			System.out.println("Simulation correct!");
		else
			System.out.println("Simulation error!");
	}
	
	public static CFG buildCFG(String pSourceFile, String pFunctionName) throws Exception {
		String code = Utils.readFile(pSourceFile);
		CFG graph = new CFG();
		
		IASTTranslationUnit translationUnit = GCC.getTranslationUnit(code.toCharArray(), pSourceFile);
		CFGVisitor visitor = new CFGVisitor(graph, pFunctionName);
		
		translationUnit.accept(visitor);
		
		return graph;
	}
}