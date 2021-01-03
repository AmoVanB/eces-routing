package de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.multipathwrapper;

import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.sp.SPAlgorithm;
import de.tum.ei.lkn.eces.routing.exceptions.RoutingException;
import de.tum.ei.lkn.eces.routing.interfaces.NToNAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.NToOneAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.OneToNAlgorithm;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;

/**
 * Wrapper around an SPAlgorithm using it to provide the
 * - N to N
 * - One to N
 * - N to One
 * interfaces.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class MultiPathSPAlgorithmWrapper implements NToNAlgorithm, OneToNAlgorithm, NToOneAlgorithm {
	/**
	 * Underlying SPAlgorithm.
	 */
	private SPAlgorithm spAlgorithm;

	/**
	 * Last request for which Path computation has been triggered.
	 */
	private Request latestRequest;

	/**
	 * Latest destination for which Path computation has been triggered.
	 */
	private Node latestDestination;

	/**
	 * Latest source for which Path computation has been triggered.
	 */
	private Node latestSource;

	public MultiPathSPAlgorithmWrapper(SPAlgorithm spAlgorithm) {
		this.latestRequest = null;
		this.latestDestination = null;
		this.latestSource = null;
		this.spAlgorithm = spAlgorithm;
	}

	@Override
	public void computePathsFromAnyNodeToAnyNode(Request request) {
		latestRequest = request;
	}

	@Override
	public void computePathsFromAnyNodeTo(Node destination, Request request) {
		this.latestRequest = request;
		this.latestDestination = destination;
	}

	@Override
	public void computePathsToAnyNodeFrom(Node source, Request request) {
		this.latestRequest = request;
		this.latestSource = source;
	}

	@Override
	public Path getPathFromNodeToNode(Node source, Node destination) {
		if(latestRequest == null)
			throw new RoutingException("The 'computePaths*' method must be called before the 'getPath*' method.");
		UnicastRequest newRequest = (UnicastRequest) latestRequest.clone();
		newRequest.setSource(source);
		newRequest.setDestination(destination);
		return (Path) spAlgorithm.solve(newRequest);
	}

	@Override
	public Path getPathToNodeFrom(Node source) {
		if(latestRequest == null || latestDestination == null)
			throw new RoutingException("The 'computePaths*' method must be called before the 'getPath*' method.");
		UnicastRequest newRequest = (UnicastRequest) latestRequest.clone();
		newRequest.setSource(source);
		newRequest.setDestination(latestDestination);
		return (Path) spAlgorithm.solve(newRequest);
	}

	@Override
	public Path getPathFromNodeTo(Node destination) {
		if(latestRequest == null || latestSource == null)
			throw new RoutingException("The 'computePaths*' method must be called before the 'getPath*' method.");
		UnicastRequest newRequest = (UnicastRequest) latestRequest.clone();
		newRequest.setSource(latestSource);
		newRequest.setDestination(destination);
		return (Path) spAlgorithm.solve(newRequest);
	}
}
