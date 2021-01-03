package de.tum.ei.lkn.eces.routing.algorithms.mcsp.emcop;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.mcp.emcp.EMCPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.MCSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.ksp.yen.YenAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.Proxy;
import de.tum.ei.lkn.eces.routing.proxies.ProxyTypes;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.Response;

import java.util.Iterator;

/**
 * The E_MCOP algorithm.
 *
 * 2002
 * "Heuristic and exact algorithms for QoS routing with multiple constraints"
 * G. Feng, K. Makki, N. Pissinou, and C. Douligeris,
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class EMCOPAlgorithm extends MCSPAlgorithm implements SolveUnicastRequest {
	private YenAlgorithm kspAlgorithm;
	private EMCPAlgorithm emcpAlgorithm;

	public EMCOPAlgorithm(Controller controller) {
		super(controller);
		kspAlgorithm = new YenAlgorithm(controller);
		emcpAlgorithm = new EMCPAlgorithm(controller);
	}

	@Override
	public Response solveNoChecks(UnicastRequest request) {
		Path h = (Path) emcpAlgorithm.solve(request);
		if(h == null)
			return null;

		int numConst = proxy.getNumberOfConstraints(request) + 1;
		int[] ids = new int[numConst];
		double[] multiplier = new double[numConst];
		double[] border = proxy.getConstraintsBounds(request);
		for(int i = 0; i < numConst; i++ ){
			ids[i] = i;
			multiplier[i] = 0;
		}

		PathPlumberProxy plumberProxy = new PathPlumberProxy(ids, multiplier, new int[]{}, ids);
		plumberProxy.setProxy(proxy);
		kspAlgorithm.setProxy(plumberProxy);
		multiplier[0] = 1;
		plumberProxy.setCostMultipliers(multiplier);
		Path lcpath = (Path) kspAlgorithm.solve(request);
		multiplier[0] = 0;
		if(Proxy.fuzzyEquals(h.getCost(), lcpath.getCost()))
			return proxy.createPath(h, request,true);
		multiplier[1] = 1;
		plumberProxy.setCostMultipliers(multiplier);
		Path c0path = (Path) kspAlgorithm.solve(request);
		multiplier[1] = 0;

		multiplier[0] = (border[0] - c0path.getCost() * 0.99)/(h.getCost() - lcpath.getCost());

		System.arraycopy(emcpAlgorithm.getMultiplier(), 0, multiplier, 1,emcpAlgorithm.getMultiplier().length);

		double mcpmax = emcpAlgorithm.getMcpmax() + multiplier[0] * h.getCost();
		plumberProxy.setCostMultipliers(multiplier);
		Iterator<Path> iterator = kspAlgorithm.iterator(request);
		while (iterator.hasNext()){
			Path path = iterator.next();
			if(path.getCost() > mcpmax)
				return  h;
			boolean valid = true;
			for(int i = 1; i < numConst; i++)
				if(Proxy.violatesBound(path.getParametersValues()[i+ proxy.getNumberOfParameters(request)], border[i-1])){
					valid = false;
					break;
				}

			if(valid && path.getParametersValues()[proxy.getNumberOfParameters(request)] < h.getCost()){
				h = proxy.createPath(path, request,true);;
				mcpmax = emcpAlgorithm.getMcpmax() + multiplier[0] * h.getCost();
			}
		}

		return h;
	}

	@Override
	protected Response solveNoChecks(Request request) {
		return null;
	}

	@Override
	public boolean isForward() {
		return true;
	}

	@Override
	public boolean isOptimal() {
		return proxy.getType() == ProxyTypes.EDGE_PROXY;
	}

	@Override
	public boolean isComplete() {
		return proxy.getType() == ProxyTypes.EDGE_PROXY;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public void setProxy(PreviousEdgeProxy previousEdgeProxy) {
		super.setProxy(previousEdgeProxy);
		emcpAlgorithm.setProxy(previousEdgeProxy);
	}

	@Override
	public void setProxy(PathProxy pathProxy) {
		super.setProxy(pathProxy);
		emcpAlgorithm.setProxy(pathProxy);
	}
}
