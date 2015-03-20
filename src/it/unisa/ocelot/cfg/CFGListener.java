package it.unisa.ocelot.cfg;

import org.jgrapht.DirectedGraph;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.jgrapht.event.VertexSetListener;

/**
 * When the first vertex is added to the graphs, links the "START" vertex to this.  
 * @author simone
 *
 */
public class CFGListener implements VertexSetListener<CFGNode> {
	private CFGNode startNode;
	private CFGNode endNode;
	
	public CFGListener(CFGNode pStartNode) {
		this.startNode = pStartNode;
		this.endNode = null;
	}
	@Override
	public void vertexAdded(GraphVertexChangeEvent<CFGNode> e) {
		CFG graph = (CFG)e.getSource();
		
		if (graph.vertexSet().size() == 1) {
			graph.addVertex(this.startNode); //Starter node
			graph.setStart(this.startNode);
			graph.addEdge(this.startNode, e.getVertex(), new LabeledEdge("goto")); //Start edge
		}
		
		this.endNode = e.getVertex();
	}
	
	public CFGNode getEndNode() {
		return endNode;
	}
	
	public CFGNode getStartNode() {
		return startNode;
	}

	@Override
	public void vertexRemoved(GraphVertexChangeEvent<CFGNode> e) {
	}
}
