package it.unisa.ocelot.c.cfg.dominators;

import java.awt.*;
import java.util.*;
import java.util.List;

import it.unisa.ocelot.c.cfg.edges.FlowEdge;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.edges.TrueEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;

/**
 * Compute dominator of a graph according to:
 * "A Simple, Fast Dominance Algorithm" Cooper et. al.
 * 
 * @author giograno
 *
 * @param <V>
 *            Vertex of graph
 * @param <E>
 *            Edges of graph
 */
public class Dominators<V, E> implements IDominators<V> {
	private DirectedGraph<V, E> graph;
	private Vector<V> vertexPreOrder;
	private Hashtable<V, V> idom = null;
	private Hashtable<V, Integer> preOrderMap;
	private SimpleDirectedGraph<V, DefaultEdge> dominatorTree;


	protected int getOrder(V vertex) {
		return preOrderMap.get(vertex);
	}

	protected V getIDom(V vertex) {
		if (idom == null) {
			computeDominators();
		}

		if(vertex == null)
			System.out.println("Cazzo");
		return idom.get(vertex);
	}

	/**
	 * Constructor of Dominators class, using default pre-order traversal
	 * 
	 * @param graph
	 *            the graph
	 * @param entry
	 *            entry point of the graph
	 */
	public Dominators(DirectedGraph<V, E> graph, V entry) {
		this(graph, dfsPreOrder(graph, entry));
	}

	private static <V, E> Vector<V> dfsPreOrder(DirectedGraph<V, E> graph, V exit) {
		DepthFirstIterator<V, E> iter = new DepthFirstIterator<V, E>(graph, exit);
		iter.setCrossComponentTraversal(false);
		Vector<V> trav = new Vector<V>();
		while (iter.hasNext()) {
			trav.add(iter.next());
		}
		return trav;
	}

	/**
	 * Dominators constructor based on the given pre-order traversal of the
	 * graph.
	 * 
	 * @param graph
	 *            the graph
	 * @param preOrder
	 *            a pre-order DFS traversal of the graph. Its first node is the
	 *            entry point of the graph.
	 */
	public Dominators(DirectedGraph<V, E> graph, Vector<V> preOrder) {
		this.graph = graph;
		this.vertexPreOrder = preOrder;
		this.preOrderMap = new Hashtable<V, Integer>();
		for (int i = 0; i < this.vertexPreOrder.size(); i++) {
			preOrderMap.put(vertexPreOrder.get(i), i);
		}
		// computing dominator here!
//		computeDominators();
		computeDominatorTree();

	}

	public void computeDominators() {
		if (this.idom != null)
			return;
		this.idom = new Hashtable<V, V>();
		V firstElement = vertexPreOrder.firstElement();
		idom.put(firstElement, firstElement);
		if (!graph.incomingEdgesOf(vertexPreOrder.firstElement()).isEmpty())
			throw new AssertionError(
					"The entry of the flow graph is not allowed to have incoming edges");
		boolean changed;
		do {
			changed = false;
			for (V v : vertexPreOrder) {
				if (v.equals(firstElement))
					continue;
				V oldIdom = getIDom(v);
				V newIdom = null;
				for (E edge : graph.incomingEdgesOf(v)) {
					V pre = graph.getEdgeSource(edge);
					if (getIDom(pre) == null) /* not yet analyzed */
						continue;
					if (newIdom == null) {
						/*
						 * If we only have one (defined) predecessor pre,
						 * IDom(v) = pre
						 */
						newIdom = pre;
					} else {
						/*
						 * compute the intersection of all defined predecessors
						 * of v
						 */
						newIdom = intersectIDoms(pre, newIdom);
					}
				}
				if (newIdom == null)
					throw new AssertionError("newIDom == null !, for " + v);
				if (!newIdom.equals(oldIdom)) {
					changed = true;
					this.idom.put(v, newIdom);
				}
			}
		} while (changed);
	}
	
	protected void computeDominatorTree() {
		SimpleDirectedGraph<V, DefaultEdge> domTree = new SimpleDirectedGraph<V, DefaultEdge>(
				DefaultEdge.class);
		for (V node : graph.vertexSet()) {
			domTree.addVertex(node);
			V idom = getIDom(node);
			if (idom != null && !node.equals(idom)) {
				domTree.addVertex(idom);
				domTree.addEdge(idom, node);
			}
		}
		this.dominatorTree = domTree;
	}

	private V intersectIDoms(V v1, V v2) {
		while (v1 != v2) {
			if (getOrder(v1) < getOrder(v2)) {
				v2 = getIDom(v2);
			} else {
				v1 = getIDom(v1);
			}
		}
		return v1;
	}

	/**
	 * Get the table of immediate dominators. Note that by convention, the
	 * graph's entry is dominated by itself (so <code>IDOM(n)</code> is a total
	 * function).</br> Note that the set <code>DOM(n)</code> is given by
	 * <ul>
	 * <li/><code>DOM(Entry) = Entry</code>
	 * <li/><code>DOM(n) = n \cup DOM(IDOM(n))</code>
	 * </ul>
	 * 
	 * @return
	 */
	public Hashtable<V, V> getIDoms() {
		computeDominators();
		return this.idom;
	}

	/**
	 * Check wheter a node dominates another one.
	 * 
	 * @param dominator
	 * @param dominated
	 * @return true, if <code>dominator</code> dominates <code>dominated</code>
	 *         w.r.t to the entry node
	 */
	public boolean dominates(V dominator, V dominated) {
		computeDominators();
		if (dominator.equals(dominated))
			return true;
		V dom = getIDom(dominated);
		// as long as dominated >= dominator
		while (dom != null && getOrder(dom) >= getOrder(dominator) && !dom.equals(dominator)) {
			dom = getIDom(dom);
		}
		return dominator.equals(dom);
	}

	/**
	 * Return nodes strictly dominators of a given node
	 * 
	 * @param node
	 *            the node for which we need dominators
	 * @return a Set of V dominators nodes
	 */
	public Set<V> getStrictDominators(V node) {
		computeDominators();
//		System.out.println(node);
		Set<V> strictDoms = new HashSet<V>();
		V dominated = node;
		V iDom = getIDom(node);
		while (iDom != dominated) {
			strictDoms.add(iDom);
			dominated = iDom;
			iDom = getIDom(dominated);
		} 
		/* my custom addition */
		strictDoms.add(node);
		/* addition of current node on dominators */
		return strictDoms;
	}

	/**
	 * Returns the set of nodes that are dominated by an edge
	 * 
	 * @param edge
	 *            the edge for which look dominated nodes
	 * @return a set of nodes dominated
	 */
	public Set<V> getDominatedNodes(E edge) {
		computeDominators();
		Set<V> dominatedNodes = new HashSet<>();
		V dominator = this.graph.getEdgeTarget(edge);

//		SimpleDirectedGraph<V, DefaultEdge> dominatorTree = getDominatorTree();

		DepthFirstIterator<V, DefaultEdge> depthFirstIterator = new DepthFirstIterator<>(
				this.dominatorTree, dominator);

		while (depthFirstIterator.hasNext()) {
			V currentNode = depthFirstIterator.next();			
			if (currentNode != dominator)
				dominatedNodes.add(currentNode);
		}

		return dominatedNodes;
	}

	/**
	 * Get the dominator tree
	 * 
	 * @return a dominator tree
	 */
	public SimpleDirectedGraph<V, DefaultEdge> getDominatorTree() {
		computeDominators();
		computeDominatorTree();
		return this.dominatorTree;
	}
	
	public List<V> getNonDominators() {
		List<V> result = new ArrayList<V>();
		
		SimpleDirectedGraph<V, DefaultEdge> dominatorTree = this.getDominatorTree();
		
		for (V testNode : dominatorTree.vertexSet()) {
			if (dominatorTree.outgoingEdgesOf(testNode).size() == 0)
				result.add(testNode);
		}
		
		return result;
	}
}