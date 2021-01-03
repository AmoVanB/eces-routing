package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.dccr;

/**
 * Data stored at each Node during the DCCR algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class NodeData {
	protected Record records[];

	public NodeData() {
		init();
	}

	public void init() {
		records = null;
	}
}
