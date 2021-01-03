package de.tum.ei.lkn.eces.routing.proxies;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.routing.requests.Request;

/**
 * PathProxy.
 * See Proxy and ProxyTypes doc.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public abstract class PathProxy extends Proxy {
	/**
	 * Gets the values of the parameters associated to the Path followed by
	 * a given Request if a given EDGE is chosen as next EDGE.
	 * @param path Path followed by the Request before current EDGE (not
	 *             including it). The iterable must iterate from the EDGE before
	 *             the current one towards the source Node.
	 * @param edge Given candidate EDGE.
	 * @param oldParameters Values of the parameters at the end of the Path
	 *                      followed by the Request before the candidate EDGE.
	 * @param request Request for which the parameters must be computed.
	 * @param isForward Whether the algorithm asking information is forward
	 *                  or not (if not, the oldParameters are actually those
	 *                  after the current EDGE, the returned newParameters are
	 *                  actually those before the current EDGE if it is chosen
	 *                  and path is actually the path followed by the Request
	 *                  *after* the current EDGE, from the next EDGE towards the
	 *                  destination).
	 * @return The parameters in an array.
	 */
	public abstract double[] getNewParameters(Iterable<Edge> path, Edge edge, double[] oldParameters, Request request, boolean isForward);

	/**
	 * Checks if a given Request can be routed on a given EDGE. This usually
	 * means checking that enough resources are still available on this EDGE
	 * for the Request to be routed.
	 * @param path Path followed by the Request before current EDGE (not
	 *             including it). The iterable must iterate from the EDGE before
	 *             the current one towards the source Node.
	 * @param edge EDGE for which the access has to be checked.
	 * @param parameters Values of the parameters at the end of the Path
	 *                   followed by the Request *including* the candidate EDGE.
	 * @param request Request for which the access has to be checked.
	 * @param isForward Whether the algorithm asking information is forward
	 *                  or not (if not, the parameters are actually those before
	 *                  the current EDGE and path is actually the path followed
	 *                  by the Request *after* the current EDGE, from the next
	 *                  EDGE towards the destination).
	 * @return true/false based on whether the Request has access or not.
	 */
	public abstract boolean hasAccess(Iterable<Edge> path, Edge edge, double[] parameters, Request request, boolean isForward);

	/**
	 * Gets the value of the cost associated to an EDGE for a given Request.
	 * @param path Path followed by the Request before current EDGE (not
	 *             including it). The iterable must iterate from the EDGE before
	 *             the current one towards the source Node.
	 * @param edge EDGE for which the objectives values have to be computed.
	 * @param parameters Values of the parameters at the end of the Path
	 *                   followed by the Request *including* the candidate EDGE.
	 * @param request Request for which the cost has to be computed.
	 * @param isForward Whether the algorithm asking information is forward
	 *                  or not (if not, the parameters are actually those before
	 *                  the current EDGE and path is actually the path followed
	 *                  by the Request *after* the current EDGE, from the next
	 *                  EDGE towards the destination).
	 * @return The cost the given EDGE.
	 */
	public abstract double getCost(Iterable<Edge> path, Edge edge, double[] parameters, Request request, boolean isForward);

	/**
	 * Gets the values of the constraints associated to an EDGE for a given
	 * Request.
	 * @param path Path followed by the Request before current EDGE (not
	 *             including it). The iterable must iterate from the EDGE before
	 *             the current one towards the source Node.
	 * @param edge EDGE for which the constraints values have to be computed.
	 * @param parameters Values of the parameters at the end of the Path
	 *                   followed by the Request *including* the candidate EDGE.
	 * @param request Request for which the constraints have to be computed.
	 * @param isForward Whether the algorithm asking information is forward
	 *                  or not (if not, path is actually the path followed by
	 *                  the Request *after* the current EDGE, from the next
	 *                  EDGE towards the destination).
	 * @return The constraints values for the given EDGE.
	 */
	public abstract double[] getConstraintsValues(Iterable<Edge> path, Edge edge, double[] parameters, Request request, boolean isForward);

	public abstract boolean register(Iterable<Edge> path, Edge edge, double[] parameters, Request request);

	public abstract boolean deregister(Iterable<Edge> path, Edge edge, double[] parameters, Request request);

	@Override
	public PathProxy clone() {
		return (PathProxy) super.clone();
	}

	@Override
	public ProxyTypes getType() {
		return ProxyTypes.PATH_PROXY;
	}
}
