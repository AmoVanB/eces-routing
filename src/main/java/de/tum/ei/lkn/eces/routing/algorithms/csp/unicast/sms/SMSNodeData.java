package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.sms;

import de.tum.ei.lkn.eces.graph.Edge;

import java.util.Iterator;

/**
 * Data stored at each Node during the DCUR algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class SMSNodeData {
	/**
	 * Data on the previous Node on the Path to the current Node.
	 */
	private SMSNodeData previousNodeData;

	/**
	 * Edge used to reach the current Node.
	 */
	private Edge previousEdge;

	/**
	 * Values of the parameters for the Path used to reach the current Node.
	 */
	private double[] parametersSoFar;

	/**
	 * Cost of the Path used to reach the current Node.
	 */
	private double costSoFar;

	/**
	 * Delay of the Path used to reach the current Node.
	 */
	private double delaySoFar;

	public SMSNodeData() {
		init();
	}

	public void init() {
		previousNodeData = null;
		parametersSoFar = null;
		previousEdge = null;
		costSoFar = 0;
		delaySoFar = 0;
	}

	public double getCostSoFar() {
		return costSoFar;
	}

	public void setCostSoFar(double costSoFar) {
		this.costSoFar = costSoFar;
	}

	public double[] getParametersSoFar() {
		return parametersSoFar;
	}

	public void setParametersSoFar(double[] parametersSoFar) {
		this.parametersSoFar = parametersSoFar;
	}

	public double getDelaySoFar() {
		return delaySoFar;
	}

	public void setDelaySoFar(double delaySoFar) {
		this.delaySoFar = delaySoFar;
	}

	public SMSNodeData getPreviousNodeData() {
		return previousNodeData;
	}

	public void setPreviousNodeData(SMSNodeData previousNodeData) {
		this.previousNodeData = previousNodeData;
	}

	public Edge getPreviousEdge() {
		return previousEdge;
	}

	public void setPreviousEdge(Edge previousEdge) {
		this.previousEdge = previousEdge;
	}
}

class SMSNodeDataIterator implements Iterator<Edge>, Iterable<Edge> {
	private SMSNodeData data;
	SMSNodeDataIterator(SMSNodeData data) {
		this.data = data;
	}

	@Override
	public boolean hasNext() {
		return data.getPreviousEdge() != null;
	}

	@Override
	public Edge next() {
		Edge edge = data.getPreviousEdge();
		data = data.getPreviousNodeData();
		return edge;
	}

	@Override
	public Iterator<Edge> iterator() {
		return this;
	}
}
