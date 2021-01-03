package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.dcbf;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.LocalMapper;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.QueueMode;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.Record;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.RecordLocal;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.UniversalPriorityQueueAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.bellmanford.BellmanFordAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.dijkstra.DijkstraAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.NToOneAlgorithm;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.Proxy;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.EdgePlumberProxy;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PreviousEdgePlumberProxy;
import de.tum.ei.lkn.eces.routing.proxies.wrappers.PathProxyWrapper;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;

import java.util.Set;

/**
 * Proxy wrapping a Proxy to grant access to an Edge only if the delay test
 * (defined in [1], Equation (11)) is passed.
 *
 * [1] Zhanfeng Jia and Pravin Varaiya, "Heuristic Methods for Delay-Constrained
 * Least-Cost Routing Problem Using k-Shortest-Path Algorithms" (2001).
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class DelayTestProxy extends PathProxyWrapper {
	/**
	 * Sources to destination algorithm used to compute the remaining delay
	 * component of the delay test.
	 */
	private NToOneAlgorithm remainingDelayAlgorithm;

	/**
	 * Proxy used by the sources to destination algorithm.
	 */
	private PathPlumberProxy plumberProxyForRemainingDelay;

	/**
	 * LocalMapper owned by the Bellman-Ford Algorithm using this Proxy to
	 * retrieve internal data of the Algorithm.
	 */
	private LocalMapper<Record> nodeDataLocalMapper;

	/**
	 * Whether BD is enable or not.
	 */
	private boolean BDFeature = false;

	/**
	 * true if the proxy has to recompute paths to the destination.
	 */
	private boolean init = true;


	private double costToDestination;

	public DelayTestProxy(Controller controller, BellmanFordAlgorithm bfAlgorithm) {
		this(controller, bfAlgorithm, new DijkstraAlgorithm(controller));
	}

	public DelayTestProxy(Controller controller, BellmanFordAlgorithm bfAlgorithm, int k) {
		this(controller, bfAlgorithm, new UniversalPriorityQueueAlgorithm(controller, QueueMode.NODE,true,false, k));
	}

	/**
	 * Creates a new DelayTestProxy.
	 * @param controller Controller responsible for the Proxy.
	 * @param bfAlgorithm Algorithm which will use the Proxy.
	 * @param remainingDelayAlgorithm Algorithm used for computing the
	 *                                remaining delay.
	 */
	public DelayTestProxy(Controller controller, BellmanFordAlgorithm bfAlgorithm, NToOneAlgorithm remainingDelayAlgorithm) {
		this.nodeDataLocalMapper = controller.getLocalMapper(bfAlgorithm, RecordLocal.class);
		this.remainingDelayAlgorithm = remainingDelayAlgorithm;
		// The remaining delay algorithm needs the delay as cost.
		this.plumberProxyForRemainingDelay = new PathPlumberProxy(new int[]{1}, new double[]{1}, new int[0], new int[]{0});
		((RoutingAlgorithm) this.remainingDelayAlgorithm).setProxy(plumberProxyForRemainingDelay);
	}

	public void setProxy(Proxy proxy) {
		/* The Proxy must be set to the current wrapper (use of super) and to
		 * the plumber proxy used by the remaining delay algorithm. */

		/*-- Current Wrapper --*/
		/* The Proxy is first plumbered to include the delay as parameter.
		 * It will then be wrapped for ease of implementation of the current
		 * class. */
		if(proxy instanceof PathProxy)
			this.proxy = new PathPlumberProxy(((PathProxy) proxy), new int[]{0}, new double[]{1}, new int[]{1}, new int[]{1});
		if(proxy instanceof EdgeProxy)
			this.proxy = new PathProxyWrapper(new EdgePlumberProxy(((EdgeProxy) proxy), new int[]{0}, new double[]{1}, new int[]{1}, new int[]{1}));
		if(proxy instanceof PreviousEdgeProxy)
			this.proxy = new PathProxyWrapper(new PreviousEdgePlumberProxy(((PreviousEdgeProxy) proxy), new int[]{0}, new double[]{1}, new int[]{1}, new int[]{1}));

		/*-- Remaining delay algorithm --*/
		if(proxy instanceof PathProxy)
			this.plumberProxyForRemainingDelay.setProxy((PathProxy) proxy);
		if(proxy instanceof EdgeProxy)
			this.plumberProxyForRemainingDelay.setProxy((EdgeProxy) proxy);
		if(proxy instanceof PreviousEdgeProxy)
			this.plumberProxyForRemainingDelay.setProxy((PreviousEdgeProxy) proxy);
	}

	@Override
	public boolean hasAccess(Iterable<Edge> path, Edge edge, double[] parameters, Request request, boolean isForward) {
		int costID = plumberProxyForRemainingDelay.getPlumberParameterId(0, (UnicastRequest) request);
		// Getting deadline.
		double deadline;
		if(this.getNumberOfConstraints(request) > 0)
			deadline = this.getConstraintsBounds(request)[0];
		else
			// If there is no deadline, delay test is always passed.
			return super.hasAccess(path, edge, parameters, request, isForward);

		// Getting the delay so far (before the new Edge).
		double delaySoFarPlusEdge = parameters[parameters.length - 1];

		if(edge.getDestination() == ((UnicastRequest)request).getDestination()) {
			costToDestination = 0;
			if (Proxy.violatesBound(delaySoFarPlusEdge, deadline))
				return false;
		}else {
			// If request is different, we update the Paths.
			if(this.init) {
				if(BDFeature) {
					if(remainingDelayAlgorithm instanceof DijkstraAlgorithm)
						((DijkstraAlgorithm) remainingDelayAlgorithm).computePathsFromAnyNodeTo(((UnicastRequest)request).getDestination(), request, deadline);
					else
						((UniversalPriorityQueueAlgorithm) remainingDelayAlgorithm).computePathsFromAnyNodeTo(((UnicastRequest)request).getDestination(), request, deadline);
				}
				else
					remainingDelayAlgorithm.computePathsFromAnyNodeTo(((UnicastRequest)request).getDestination(), request);
				this.init = false;
			}
			Path remainingPath = null;
			if(remainingDelayAlgorithm instanceof UniversalPriorityQueueAlgorithm){
				Set<Path> paths = ((UniversalPriorityQueueAlgorithm) remainingDelayAlgorithm).getkPathToNodeFrom(edge.getDestination());
				for(Path put: paths){
					if(put != null && !Proxy.violatesBound(delaySoFarPlusEdge + put.getCost(), deadline)){
						if(remainingPath == null){
							remainingPath = put;
						} else {
							if(remainingPath.getParametersValues()[costID] > put.getParametersValues()[costID]){
								remainingPath = put;
							}
						}
					}
				}
			} else {
				remainingPath = remainingDelayAlgorithm.getPathToNodeFrom(edge.getDestination());
				// Checking the delay test.
				if(remainingPath == null || Proxy.violatesBound(delaySoFarPlusEdge + remainingPath.getCost() , deadline))
					return false;
			}
			if(remainingPath == null)
				return false;

			costToDestination = remainingPath.getParametersValues()[costID];

		}
		return super.hasAccess(path, edge, parameters, request, isForward);
	}


	public double getCostToDestination(){
		return costToDestination;
	}

	@Override
	public DelayTestProxy clone() {
		DelayTestProxy clone = (DelayTestProxy) super.clone();
		clone.proxy = proxy.clone();
		return clone;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + this.proxy.toString() + ")";
	}

	public void reinit() {
		this.init = true;
	}

	public void enableBD() {
		this.BDFeature = true;
	}

	public void disableBD() {
		this.BDFeature = false;
	}
}
