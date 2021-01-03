package de.tum.ei.lkn.eces.routing.algorithms;

/**
 * Different type of routing problems based on the metrics used.
 *
 * See: "Unicast QoS Routing Algorithms for SDN: A Comprehensive Survey and Performance Evaluation". JW Guck,
 * A Van Bemten, M Reisslein, W Kellerer. IEEE Communications Surveys &amp; Tutorials. 2017.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public enum MetricTypes {
	SP,   // shortest path
	CSP,  // constrained shortest path
	MCSP, // multi-constrained shortest path
	MCP   // multi-constrained path
}
