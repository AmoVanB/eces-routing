package de.tum.ei.lkn.eces.routing.proxies;

/**
 * List of the different possible types of ProxyTypes depending on the input
 * they need to compute the cost or constraints values associated to an Edge.
 *
 * See: "Routing metrics depending on previous edges: The Mn taxonomy and its corresponding solutions" A Van Bemten,
 * JW Guck, CM Machuca, W Kellerer. 2018.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public enum ProxyTypes {
	// Requires only the considered Edge.
	EDGE_PROXY,
	// Requires the considered Edge and the previous one used to reach it.
	PREVIOUS_EDGE_PROXY,
	// Requires the whole Path taken before the considered Edge.
	PATH_PROXY
}
