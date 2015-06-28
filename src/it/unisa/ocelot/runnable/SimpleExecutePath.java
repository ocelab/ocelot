package it.unisa.ocelot.runnable;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGBuilder;
import it.unisa.ocelot.c.cfg.CFGVisitor;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.c.compiler.GCC;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.nodes.NodeDistanceListener;
import it.unisa.ocelot.genetic.paths.PathDistanceListener;
import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.EventsHandler;
import it.unisa.ocelot.simulator.Simulator;
import it.unisa.ocelot.simulator.listeners.NodePrinterListener;
import it.unisa.ocelot.util.Utils;

@Deprecated
public class SimpleExecutePath {
	private static final String CONFIG_FILENAME = "config.properties";
	
	static {
		System.loadLibrary("Test");
	}
	
	public static void main(String[] args) throws Exception {
		ConfigManager.setFilename(CONFIG_FILENAME);
		ConfigManager config = ConfigManager.getInstance();
		
		CFG cfg = CFGBuilder.build(config.getTestFilename(), config.getTestFunction());
		
		CBridge bridge = new CBridge();
		EventsHandler h = new EventsHandler();
		
		Object[] arguments = config.getTestArguments();
		
		bridge.getEvents(h, arguments);
		
		List<LabeledEdge> targetPath = config.getTestTargetPath(cfg);
		
		System.out.println("Target path:" + targetPath.toString());
		
		Simulator simulator = new Simulator(cfg, h.getEvents());
		
		PathDistanceListener bdalListener = new PathDistanceListener(cfg, targetPath);
		
		simulator.addListener(new NodePrinterListener());
		simulator.addListener(bdalListener);

		System.out.println("Simulating with " + StringUtils.join(arguments, " "));
		
		simulator.simulate();
		
		System.out.println("AL: " + bdalListener.getPathDistance());
		System.out.println("NBD: " + bdalListener.getNormalizedBranchDistance());
		
		if (simulator.isSimulationCorrect())
			System.out.println("Simulation correct!");
		else
			System.out.println("Simulation error!");
	}
}