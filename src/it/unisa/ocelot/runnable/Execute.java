package it.unisa.ocelot.runnable;

import java.io.IOException;

import javax.swing.tree.ExpandVetoException;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGVisitor;
import it.unisa.ocelot.c.compiler.GCC;
import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.EventsHandler;
import it.unisa.ocelot.simulator.Simulator;
import it.unisa.ocelot.simulator.TestSimulatorListener;
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
		arguments[0] = new Integer(12);
		arguments[1] = new Integer(3);
		arguments[2] = new Integer(23);
		
		bridge.getEvents(h, arguments);
		
		System.out.println(h);
		
		System.out.println("------------------------------");
		Simulator simulator = new Simulator(cfg, h.getEvents());
		
		simulator.setListener(new TestSimulatorListener());
		
		simulator.simulate();
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