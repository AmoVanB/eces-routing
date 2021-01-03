package de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa;

import de.tum.ei.lkn.eces.graph.Edge;

import java.util.Iterator;

public class TempIterablePath implements Iterable<Edge>, Iterator<Edge> {
	private Edge edge;
	private TempIterablePath presegment = null;

	public TempIterablePath( Edge edge){
		this.edge = edge;
	}
	public TempIterablePath(TempIterablePath path, Edge edge){
		this(edge);
		presegment = path;
	}

	@Override
	public boolean hasNext() {
		return edge != null;
	}

	@Override
	public Edge next() {
		Edge edge = this.edge;
		if(this.presegment != null){
			this.edge = presegment.getEdge();
			this.presegment = presegment.presegment;
		} else {
			this.edge = null;
		}
		return edge;
	}

	public Edge getEdge(){
		return edge;
	}

	@Override
	public Iterator<Edge> iterator() {
		return new TempIterablePath(presegment, edge);
	}
}
