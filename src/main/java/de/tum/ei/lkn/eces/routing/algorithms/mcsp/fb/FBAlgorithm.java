package de.tum.ei.lkn.eces.routing.algorithms.mcsp.fb;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.MCSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.SPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.astar.AStarAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.BD;
import de.tum.ei.lkn.eces.routing.interfaces.BDifiable;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.proxies.wrappers.PathProxyWrapper;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.Response;

/**
 * The Fallback algorithm.
 *
 * 1995
 * "Routing Subject to Quality of Service Constraints in Integrated
 *  Communication Networks"
 * Whay C. Lee, Michael G. Hluchyj and Pierre A. Humblet.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class FBAlgorithm extends MCSPAlgorithm implements SolveUnicastRequest, BDifiable {
	/**
	 * SPAlgorithm used.
	 */
	private SPAlgorithm spAlgorithm;

	/**
	 * Underlying proxy containing all constraints.
	 */
	private PathProxy pathProxy;

	/**
	 * Whether BD is activate or not.
	 */
	private boolean BDfeature = false;

	public FBAlgorithm(Controller controller) {
		this(controller, new AStarAlgorithm(controller));
	}

	public FBAlgorithm(Controller controller, SPAlgorithm spAlgorithm) {
		super(controller);
		this.spAlgorithm = spAlgorithm;
		this.pathProxy = null;
	}

	@Override
	public void setProxy(PathProxy proxy) {
		super.setProxy(proxy);
		this.pathProxy = proxy;
	}

	@Override
	public void setProxy(PreviousEdgeProxy proxy) {
		super.setProxy(proxy);
		this.pathProxy = new PathProxyWrapper(proxy);
	}

	@Override
	public void setProxy(EdgeProxy proxy) {
		super.setProxy(proxy);
		this.pathProxy = new PathProxyWrapper(proxy);
	}

	@Override
	protected Response solveNoChecks(Request request) {
		return this.computePath((UnicastRequest) request);
	}
	@Override
	public Response solveNoChecks(UnicastRequest request) {
		return this.computePath(request);
	}
	protected Path computePath(UnicastRequest request) {
		/* Successively trying using the cost and then the different
		 * constraints as optimization metric. */
		for(int i = 0; i <= proxy.getNumberOfConstraints(request); i++) {
			PathPlumberProxy plumberProxy = new PathPlumberProxy(pathProxy,
					new int[]{i},
					new double[]{1},
					new int[0],
					new int[0]);
			this.spAlgorithm.setProxy(plumberProxy);
			Path result;
			if(BDfeature && i != 0)
				result = (Path) ((BD) spAlgorithm).solve(request, proxy.getConstraintsBounds(request)[i - 1]);
			else
				result = (Path) spAlgorithm.solve(request);

			if(result != null && proxy.isValid(result, request))
				return proxy.createPath(result, request, this.isForward());
		}

		return null;
	}

	@Override
	public boolean isForward() {
		return spAlgorithm.isForward();
	}

	@Override
	public boolean isOptimal() {
		return false;
	}

	@Override
	public boolean isComplete() {
		return false;
	}

	@Override
	public boolean isValid() {
		// Valid even if SPAlgorithm is not valid since it always checks.
		return true;
	}

	@Override
	public void enableBD() {
		this.BDfeature = true;
	}

	@Override
	public void disableBD() {
		this.BDfeature = false;
	}
}
