package it.unisa.ocelot.c.edge_graph;

public class EdgeWrapper<E> {
	private E wrappedEdge;
	
	public EdgeWrapper(E pWrappedEdge) {
		this.wrappedEdge = pWrappedEdge;
	}
	
	public E getWrappedEdge() {
		return wrappedEdge;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		
		if (!(obj instanceof EdgeWrapper))
			return false;
		
		@SuppressWarnings("rawtypes")
		EdgeWrapper wrapper = (EdgeWrapper)obj;
		
		if (wrapper.getWrappedEdge() == null)
			return (this.getWrappedEdge() == null);
		
		return wrapper.getWrappedEdge().equals(this.getWrappedEdge());
	}
}
