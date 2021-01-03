package de.tum.ei.lkn.eces.routing.algorithms.mcsp.astarprune;

import de.tum.ei.lkn.eces.graph.Edge;

import java.util.Iterator;

/**
 * Iterator over a HeadPath.
 */
class HeadPathIterator implements Iterator<Edge>, Iterable<Edge> {
	/**
	 * Current HeadPath.
	 */
	private HeadPath currentHeadPath;

	public HeadPathIterator(HeadPath headPath) {
		this.currentHeadPath = headPath;
	}

	@Override
	public boolean hasNext() {
		return (this.currentHeadPath != null && this.currentHeadPath.getLastEdge() != null);
	}

	@Override
	public Edge next() {
		Edge edge = currentHeadPath.getLastEdge();
		this.currentHeadPath = currentHeadPath.getSourcePath();
		return edge;
	}

	@Override
	public Iterator<Edge> iterator() {
		return new HeadPathIterator(currentHeadPath);
	}
}
