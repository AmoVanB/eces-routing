package de.tum.ei.lkn.eces.routing.algorithms.mcsp.kspmc;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.MCSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.ksp.KSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.ksp.yen.YenAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.Response;

import java.util.Iterator;

/**
 * Finds the optimal constraint Path by looking for successive least-cost paths
 * until a valid one is found.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class KSPMCAlgorithm extends MCSPAlgorithm implements SolveUnicastRequest {
	/**
	 * kSPAlgorithm used.
	 */
	private KSPAlgorithm kspAlgorithm;

	/**
	 * Plumber proxy for hiding constraints to the kSPAlgorithm.
	 */
	private PathPlumberProxy plumberProxy;

	public KSPMCAlgorithm(Controller controller) {
		this(controller, new YenAlgorithm(controller));
	}

	public KSPMCAlgorithm(Controller controller, KSPAlgorithm kspAlgorithm) {
		super(controller);
		// kSP only cares about the cost.
		plumberProxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[0],
				new int[0]);
		this.kspAlgorithm = kspAlgorithm;
		this.kspAlgorithm.setProxy(plumberProxy);
	}

	@Override
	public void setProxy(PathProxy proxy) {
		super.setProxy(proxy);
		plumberProxy.setProxy(proxy);
	}

	@Override
	public void setProxy(PreviousEdgeProxy proxy) {
		super.setProxy(proxy);
		plumberProxy.setProxy(proxy);
	}

	@Override
	public void setProxy(EdgeProxy proxy) {
		super.setProxy(proxy);
		plumberProxy.setProxy(proxy);
	}

	@Override
	protected Response solveNoChecks(Request request) {
		return this.computePath((UnicastRequest) request);
	}
	@Override
	public Response solveNoChecks(UnicastRequest request) {
		return this.computePath( request);
	}
	protected Path computePath(UnicastRequest request) {
		Iterator<Path> kspIterator = kspAlgorithm.iterator(request);
		while(kspIterator.hasNext()) {
			Path path = proxy.createPath(kspIterator.next(), request, kspAlgorithm.isForward());
			if(path != null && proxy.isValid(path, request))
				return path;
		}

		return null;
	}

	@Override
	public boolean isForward() {
		return kspAlgorithm.isForward();
	}

	@Override
	public boolean isOptimal() {
		return kspAlgorithm.isOptimal() && kspAlgorithm.isComplete();
	}

	@Override
	public boolean isComplete() {
		return kspAlgorithm.isComplete() && kspAlgorithm.isOptimal();
	}

	@Override
	public boolean isValid() {
		return true;
	}
}
