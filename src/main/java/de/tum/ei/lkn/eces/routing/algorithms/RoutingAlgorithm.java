package de.tum.ei.lkn.eces.routing.algorithms;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.exceptions.RoutingException;
import de.tum.ei.lkn.eces.routing.interfaces.*;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.Proxy;
import de.tum.ei.lkn.eces.routing.proxies.wrappers.PathProxyWrapper;
import de.tum.ei.lkn.eces.routing.requests.*;
import de.tum.ei.lkn.eces.routing.responses.Response;

/**
 * Class representing a RoutingAlgorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public abstract class RoutingAlgorithm {
	/**
	 * PathProxy attached to the Algorithm.
	 */
	protected PathProxy proxy;

	/**
	 * Controller responsible for the algorithm.
	 */
	protected Controller controller;

	/**
	 * If an Algorithm is in debug mode, it will always return the
	 * Path that he found. If not, it will first check if the Path
	 * is valid before returning it. Here, valid means that the Path
	 * satisfies the requirements of the Request.
	 */
	private boolean debugMode;

	/**
	 * Creates a new RoutingAlgorithm.
	 * @param controller Controller responsible for the RoutingAlgorithm.
	 */
	protected RoutingAlgorithm(Controller controller) {
		this.controller = controller;
		this.debugMode = false;
	}

	/**
	 * Sets the Algorithm in EdgeProxy mode.
	 * @param edgeProxy Proxy to use.
	 */
	public void setProxy(EdgeProxy edgeProxy) {
		this.proxy = new PathProxyWrapper(edgeProxy);
	}

	/**
	 * Sets the Algorithm in PreviousEdgeProxy mode.
	 * @param previousEdgeProxy Proxy to use.
	 */
	public void setProxy(PreviousEdgeProxy previousEdgeProxy) {
		this.proxy = new PathProxyWrapper(previousEdgeProxy);
	}

	/**
	 * Sets the Algorithm in PathProxy mode.
	 * @param pathProxy Proxy to use.
	 */
	public void setProxy(PathProxy pathProxy) {
		this.proxy = pathProxy;
	}

	/**
	 * Returns the Proxy currently in use by the Algorithm.
	 * @return The Proxy currently in use by the Algorithm or null if the
	 *         Algorithm has not yet been configured with a Proxy.
	 */
	public Proxy getProxy() {
		return proxy;
	}

	/**
	 * Sets the Algorithm in debug mode.
	 */
	public void setDebugMode() {
		debugMode = true;
	}

	/**
	 * Sets the Algorithm in non-debug mode.
	 */
	public void unsetDebugMode() {
		debugMode = false;
	}

	/**
	 * Runs the Algorithm to compute a Path for a given Request.
	 * If the algorithm is in debug mode, the Path found by the Algorithm
	 * will anyways be returned.
	 * If the algorithm is not in debug mode, the Path found is returned
	 * only if it is valid for the Request. Here, valid means that it
	 * satisfies the requirements of the Request.
	 * @param request Request for which a Path has to be computed.
	 * @return Resulting Path.
	 */
	public final Response solve(Request request) {
		Response response = null;
		if(request instanceof UnicastWithINRequest && this instanceof SolveUnicastWithINRequest)
				response = ((SolveUnicastWithINRequest)this).solveNoChecks((UnicastWithINRequest) request);
		else if(request instanceof UnicastWithINAndCandidatesRequest && this instanceof SolveUnicastWithINAndCandidatesRequest)
				response = ((SolveUnicastWithINAndCandidatesRequest)this).solveNoChecks((UnicastWithINAndCandidatesRequest) request);
		else if(request instanceof UnicastRequest && this instanceof SolveUnicastRequest)
				response = ((SolveUnicastRequest)this).solveNoChecks((UnicastRequest) request);
		else if(request instanceof DisjointRequest && this instanceof SolveDisjointRequest)
				response = ((SolveDisjointRequest)this).solveNoChecks((DisjointRequest) request);
		else if(request instanceof ResilientRequest && this instanceof SolveResilientRequest)
				response = ((SolveResilientRequest)this).solveNoChecks((ResilientRequest) request);
		else
			throw new RoutingException(this.getClass().getName() + " could not solve " + request.getClass().getName() + " request");

		if(debugMode || response == null)
			return response;

		return proxy.checkValidity(response, request, false);
	}

	/**
	 * Checks whether or not the Algorithm is able to handle a given Request.
	 * For example, a shortest-path algorithm (SPAlgorithm) is not able to
	 * handle a Request with E2E constraints.
	 * @param request The given Request.
	 * @return true/false based on whether or not the Algorithm is able to
	 *         handle the given Request.
	 */
	public boolean handle(Request request) {
		boolean ableToHandleMetrics = true;
		switch(this.getMetricsType()) {
			case SP:
				ableToHandleMetrics = this.getProxy().getNumberOfConstraints(request) == 0;
				break;
			case MCSP:
				ableToHandleMetrics = true;
				break;
			case CSP:
				ableToHandleMetrics = this.getProxy().getNumberOfConstraints(request) <= 1;
				break;
			case MCP:
				ableToHandleMetrics = true;
				break;
		}

		return this.getProxy().handle(request, this.isForward()) &&
				ableToHandleMetrics &&
				this.ableToHandleRequest(request);
	}

	/**
	 * Checks if the algorithm is able to handle a given type of request.
	 * Does not check the metrics of the current proxy.
	 * @param request given request.
	 * @return true/false.
	 */
	public boolean ableToHandleRequest(Request request) {
		if(request instanceof UnicastWithINRequest && this instanceof SolveUnicastWithINRequest)
				return true;
		else if(request instanceof UnicastWithINAndCandidatesRequest && this instanceof SolveUnicastWithINAndCandidatesRequest)
				return true;
		else if(request instanceof DisjointRequest && this instanceof SolveDisjointRequest)
				return true;
		else if(request instanceof ResilientRequest && this instanceof SolveResilientRequest)
				return true;
		else if(request instanceof UnicastRequest && this instanceof SolveUnicastRequest)
				return true;

		return false;
	}

	/**
	 * Runs the RoutingAlgorithm.
	 * @param request Request for which the algorithm must be run.
	 * @return Path found by the algorithm or null if the Algorithm
	 *         found nothing.
	 */
	protected abstract Response solveNoChecks(Request request);

	/**
	 * Checks if the algorithm is routing from source to destination using
	 * the normal directed Edges or from destination to source using the
	 * opposite Edges.
	 * Note that the algorithm will always find a path from source to
	 * destination but the forward or backward aspect might influence the
	 * data structures of the algorithm and the meaning of the Previous-
	 * EdgeProxy and PathProxy (see corresponding documentation).
	 * @return true if the algorithm is routing forwards, false otherwise.
	 */
	public abstract boolean isForward();

	/**
	 * Determines whether the algorithm is optimal or not.
	 * An algorithm is optimal if all the Paths it returns have the least
	 * possible cost. It does not imply completeness.
	 * The result might depend on the type of Proxy currently in use.
	 * @return true/false based on whether or not the algorithm is optimal.
	 */
	public abstract boolean isOptimal();

	/**
	 * Determines whether the algorithm is complete or not.
	 * An algorithm is complete if it always finds a Path if one exists.
	 * The result might depend on the type of Proxy currently in use.
	 * @return true/false based on whether or not the algorithm is optimal.
	 */
	public abstract boolean isComplete();

	/**
	 * Determines whether the algorithm is valid or not.
	 * An algorithm is valid if it always compute valid Paths. Here, a Path
	 * is valid if it satisfies the requirements of the Request for which it
	 * has been found *and* if the values stored in it (cost, constraints and
	 * parameters values) are correct.
	 * Usually, an algorithm becomes invalid when using a PreviousEdgeProxy
	 * or a PathProxy because it does not know that costs of Edges might
	 * depend on previously visited Edges. The Algorithm might then not
	 * know what is actually really going on and end up with an invalid
	 * Path.
	 * @return true/false based on whether or not the algorithm is valid.
	 */
	public abstract boolean isValid();

	/**
	 * Determines the class of the algorithm with respect to the type of metrics
	 * it can handle.
	 * @return the type of metrics the algorithm can handle.
	 */
	public abstract MetricTypes getMetricsType();

	@Override
	public String toString() {
		String proxyInfo = "NO_PROXY";
		if(this.proxy != null)
			proxyInfo = this.proxy.getType().toString() + " - " + this.proxy.toString();
		return this.getClass().getSimpleName() + " (" + proxyInfo + ")";
	}
}
