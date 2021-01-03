package de.tum.ei.lkn.eces.routing.proxies;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.routing.requests.Request;

/**
 * EdgeProxy.
 * See Proxy and ProxyTypes doc.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public abstract class EdgeProxy extends Proxy {
	/**
	 * Gets the values of the parameters associated to the Path followed by
	 * a given Request if a given EDGE is chosen as next EDGE.
	 * @param edge Given candidate Edge.
	 * @param oldParameters Values of the parameters at the end of the Path
	 *                      followed by the Request before the candidate Edge.
	 * @param request Request for which the parameters must be computed.
	 * @param isForward Whether the algorithm asking information is forward
	 *                  or not (if not, the oldParameters are actually those
	 *                  after the current Edge and the returned newParameters
	 *                  are actually those before the current Edge if it is
	 *                  chosen).
	 * @return The parameters in an array.
	 */
	public abstract double[] getNewParameters(Edge edge, double[] oldParameters, Request request, boolean isForward);

	/**
	 * Checks if a given Request can be routed on a given Edge. This usually
	 * means checking that enough resources are still available on this Edge
	 * for the Request to be routed.
	 * @param edge Edge for which the access has to be checked.
	 * @param request Request for which the access has to be checked.
	 * @return true/false based on whether the Request has access or not.
	 */
	public abstract boolean hasAccess(Edge edge, Request request);

	/**
	 * Gets the value of the cost associated to an Edge for a given Request.
	 * @param edge Edge for which the cost has to be computed.
	 * @param request Request for which the cost has to be computed.
	 * @return The cost the given Edge.
	 */
	public abstract double getCost(Edge edge, Request request);

	/**
	 * Gets the values of the constraints associated to an Edge for a given
	 * Request.
	 * @param edge Edge for which the constraints values have to be computed.
	 * @param request Request for which the constraints values have to be
	 *                computed.
	 * @return The constraints values for the given Edge.
	 */
	public abstract double[] getConstraintsValues(Edge edge, Request request);

	public abstract boolean register(Edge edge, Request request);

	public abstract boolean deregister(Edge edge, Request request);

	@Override
	public EdgeProxy clone() {
		return (EdgeProxy) super.clone();
	}

	@Override
	public ProxyTypes getType() {
		return ProxyTypes.EDGE_PROXY;
	}
}
