package it.unisa.ocelot.c.cfg;

import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

import org.jgraph.JGraph;
import org.jgraph.layout.JGraphLayoutAlgorithm;
import org.jgraph.layout.SugiyamaLayoutAlgorithm;
import org.jgrapht.ext.JGraphModelAdapter;

public class CFGWindow extends JFrame {
	private static final long serialVersionUID = 3258731574736285311L;

	private static final Dimension DEFAULT_SIZE = new Dimension(500, 500);

	private CFG cfg;
	
	public CFGWindow(CFG pCFG) {
		this.init(pCFG);
	}

	public void init(CFG pCFG) {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.cfg = pCFG;
		this.setSize(DEFAULT_SIZE);

		// create a visualization using JGraph, via an adapter
		JGraphModelAdapter<CFGNode, LabeledEdge> graphAdapter = new JGraphModelAdapter<CFGNode, LabeledEdge>(
				this.cfg);

		JGraph jgraph = new JGraph(graphAdapter);

		adjustDisplaySettings(jgraph);
		getContentPane().add(jgraph);
		
		List<Object> roots = new ArrayList<Object>();
		Iterator<CFGNode> vertexIter = this.cfg.vertexSet().iterator();
		
		while (vertexIter.hasNext()) {
		    CFGNode vertex = vertexIter.next();
		    if (this.cfg.inDegreeOf(vertex) == 0) {
		        roots.add(graphAdapter.getVertexCell(vertex));
		    }
		}
		
		JGraphLayoutAlgorithm layout = new SugiyamaLayoutAlgorithm();
		JGraphLayoutAlgorithm.applyLayout(jgraph, layout, roots.toArray()); 
	}

	private void adjustDisplaySettings(JGraph jg) {
		jg.setPreferredSize(DEFAULT_SIZE);
	}
}
