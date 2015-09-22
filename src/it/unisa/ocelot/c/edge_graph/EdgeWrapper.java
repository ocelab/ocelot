package it.unisa.ocelot.c.edge_graph;

public class EdgeWrapper<E> {
	private E wrappedEdge;
	
	public EdgeWrapper(E pWrappedEdge) {
		this.wrappedEdge = pWrappedEdge;
	}
	
	public E getWrappedEdge() {
		return wrappedEdge;
	}
}
