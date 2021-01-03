package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.dccr;

import de.tum.ei.lkn.eces.graph.Node;

/**
 * Heap item as defined in Appendix A of reference paper.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class HeapItem implements Comparable<HeapItem> {
	private Node node;
	private double weight;
	private int id;
	private int sqnum = 0;

	public HeapItem(Node node, double weight, int id) {
		this.node = node;
		this.weight = weight;
		this.id = id;
	}

	public Node getNode() {
		return node;
	}

	public double getWeight() {
		return weight;
	}

	public int getId() {
		return id;
	}

	public void setSqnum(int sqnum) {
		this.sqnum = sqnum;
	}

	@Override
	public int compareTo(HeapItem other) {
		// Least weight is extracted first.
		if(weight < other.weight)
			return -1;
		if(weight > other.weight)
			return 1;
		if(this.sqnum > other.sqnum) // For determinism.
			return 1;
		if(this.sqnum < other.sqnum)
			return -1;
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof HeapItem && this.node == ((HeapItem) obj).node && this.id == ((HeapItem) obj).id;
	}
}
