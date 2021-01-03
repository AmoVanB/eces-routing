package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.meb;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;

import java.util.HashMap;
import java.util.Iterator;

public class NodeData {
	/**
	 * Data associated to best result for each hop count.
	 */
	private HashMap<Integer, Triplet> data = new HashMap<>();

	private Node node;

	public NodeData() {
		init();
	}

	public void init() {
		data.clear();
	}

	public HashMap<Integer, Triplet> getData() {
		return data;
	}

	public void setData(HashMap<Integer, Triplet> data) {
		this.data = data;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}
}

class Triplet {
	private int hopCount;
	private double weight;
	private double cost;
	private double delay;
	private double[] parameters;
	private NodeData node;
	private Edge previousEdge;
	private NodeData previousNode;

	public Triplet(int hopCount, double weight, double cost, double delay, double[] parameters, NodeData node, Edge previousEdge, NodeData previousNode) {
		this.hopCount = hopCount;
		this.weight = weight;
		this.cost = cost;
		this.delay = delay;
		this.parameters = parameters;
		this.node = node;
		this.previousEdge = previousEdge;
		this.previousNode = previousNode;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public double getDelay() {
		return delay;
	}

	public void setDelay(double delay) {
		this.delay = delay;
	}

	public NodeData getNode() {
		return node;
	}

	public void setNode(NodeData node) {
		this.node = node;
	}

	public Edge getPreviousEdge() {
		return previousEdge;
	}

	public void setPreviousEdge(Edge previousEdge) {
		this.previousEdge = previousEdge;
	}

	public NodeData getPreviousNode() {
		return previousNode;
	}

	public void setPreviousNode(NodeData previousNode) {
		this.previousNode = previousNode;
	}

	public int getHopCount() {
		return hopCount;
	}

	public void setHopCount(int hopCount) {
		this.hopCount = hopCount;
	}

	public double[] getParameters() {
		return parameters;
	}

	public void setParameters(double[] parameters) {
		this.parameters = parameters;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
}

class TripletIterator implements Iterator<Edge>, Iterable<Edge> {
	private Triplet triplet;

	public TripletIterator(Triplet triplet) {
		this.triplet = triplet;
	}

	@Override
	public Iterator<Edge> iterator() {
		return new TripletIterator(triplet);
	}

	@Override
	public boolean hasNext() {
		return triplet.getPreviousEdge() != null && triplet.getPreviousNode() != null;
	}

	@Override
	public Edge next() {
		Edge edge = triplet.getPreviousEdge();
		triplet = triplet.getPreviousNode().getData().get(triplet.getHopCount() - 1);
		return edge;
	}
}
