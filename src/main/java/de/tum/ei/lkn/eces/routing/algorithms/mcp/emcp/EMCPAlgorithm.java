package de.tum.ei.lkn.eces.routing.algorithms.mcp.emcp;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.mcp.MCPAlgorithm;
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
 * The E_MCP algorithm.
 *
 * 2002
 * "Heuristic and exact algorithms for QoS routing with multiple constraints"
 * G. Feng, K. Makki, N. Pissinou, and C. Douligeris,
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class EMCPAlgorithm extends MCPAlgorithm implements SolveUnicastRequest {
	private YenAlgorithm kspAlgorithm;
	private double[] multiplier;
	private double mcpmax;

	public EMCPAlgorithm(Controller controller) {
		this(controller, Double.POSITIVE_INFINITY);
	}

	public EMCPAlgorithm(Controller controller, double lambda) {
		super(controller);
		kspAlgorithm = new YenAlgorithm(controller);
	}

	@Override
	protected Response solveNoChecks(Request request) {
	    if (request instanceof UnicastRequest)
	        return solveNoChecks((UnicastRequest) request);
		return null;
	}

	@Override
	public Response solveNoChecks(UnicastRequest request) {
		int numConst = proxy.getNumberOfConstraints(request);
		if(numConst == 0){
			kspAlgorithm.setProxy(proxy);
			Path path = (Path) kspAlgorithm.solve(request);
			return path;
		}

		int[] ids = new int[numConst];
		multiplier = new double[numConst];
		double[] border = proxy.getConstraintsBounds(request);
		for(int i = 0; i < numConst; i++ ){
			ids[i] = i+1;
			multiplier[i] = 0;
		}

		PathPlumberProxy plumberProxy = new PathPlumberProxy(ids, multiplier, new int[0], ids);
		plumberProxy.setProxy(proxy);
		kspAlgorithm.setProxy(plumberProxy);
		Path[] paths = new Path[numConst];
		for(int i = 0; i < numConst; i++ ){
			multiplier[i] = 1;
			plumberProxy.setCostMultipliers(multiplier);
			paths[i] = (Path) kspAlgorithm.solve(request);
			if(paths[i] == null || Proxy.violatesBound(paths[i].getCost(), border[i]) )
				return null;
			multiplier[i] = 0;
		}

		multiplier[0] = 1;
		double ratio = border[0] - paths[0].getCost() * 0.99;
		mcpmax= border[0];
		for(int i = 1; i < numConst; i++ ){
			multiplier[i] = ratio/(border[i] - paths[i].getCost() * 0.99);
			mcpmax += multiplier[i] * border[i] * 1.02;
		}

		plumberProxy.setCostMultipliers(multiplier);
		Iterator<Path> iterator = kspAlgorithm.iterator(request);

		while (iterator.hasNext()) {
			Path path = iterator.next();
			if(path.getCost() > mcpmax)
				return null;
			boolean valid = true;
			for(int i = 0; i < numConst; i++)
				if(Proxy.violatesBound(path.getParametersValues()[i+ proxy.getNumberOfParameters(request)], border[i])){
					valid = false;
					break;
				}
			if(valid)
				return proxy.createPath(path, request,true);
		}

		return null;
	}

	public double[] getMultiplier() {
		return multiplier;
	}

	public double getMcpmax() {
		return mcpmax;
	}
	@Override
	public boolean isForward() {
		return kspAlgorithm.isForward();
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
	}

	@Override
	public void setProxy(PathProxy pathProxy) {
		super.setProxy(pathProxy);
	}
}
