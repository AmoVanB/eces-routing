package de.tum.ei.lkn.eces.routing.algorithms.csp.in.mhlarac;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.csp.CSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.larac.LARACAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.in.SimpleINSPAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastWithINRequest;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.requests.UnicastWithINRequest;
import de.tum.ei.lkn.eces.routing.responses.Response;

/**
 * The LARAC-SN algorithm.
 *
 * 2018
 * "LARAC-SN and Mole in the Hole: Enabling Routing through Service Function Chains"
 * A Van Bemten, JW Guck, P Vizarreta, CM Machuca, W Kellerer.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class INLARACAlgorithm extends CSPAlgorithm implements SolveUnicastRequest, SolveUnicastWithINRequest {
	/**
	 * Underlying LARAC algorithm.
	 */
	private LARACAlgorithm laracAlgorithm;

	public INLARACAlgorithm(Controller controller) {
		super(controller);
		laracAlgorithm = new LARACAlgorithm(controller, new SimpleINSPAlgorithm(controller));
	}

	@Override
	public Response solveNoChecks(UnicastRequest request) {
		return laracAlgorithm.solveNoChecks(request);
	}

	@Override
	public Response solveNoChecks(UnicastWithINRequest request) {
		return laracAlgorithm.solveNoChecks(request);
	}

	@Override
	protected Response solveNoChecks(Request request) {
		return laracAlgorithm.solveNoChecks((UnicastRequest) request);
	}

	@Override
	public boolean isForward() {
		return laracAlgorithm.isForward();
	}

	@Override
	public boolean isOptimal() {
		return laracAlgorithm.isOptimal();
	}

	@Override
	public boolean isComplete() {
		return laracAlgorithm.isComplete();
	}

	@Override
	public boolean isValid() {
		return laracAlgorithm.isValid();
	}

	@Override
	public void setProxy(EdgeProxy edgeProxy) {
		super.setProxy(edgeProxy);
		setProxy();
	}

	@Override
	public void setProxy(PreviousEdgeProxy previousEdgeProxy) {
		super.setProxy(previousEdgeProxy);
		setProxy();
	}

	@Override
	public void setProxy(PathProxy pathProxy) {
		super.setProxy(pathProxy);
		setProxy();
	}

	private void setProxy() {
		laracAlgorithm.setProxy(this.proxy);
	}

}
