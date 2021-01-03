

package de.tum.ei.lkn.eces.routing.mocks;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.requests.Request;

public class DummyProxy extends EdgeProxy {
	public DummyProxy() {

	}

	@Override
	public boolean handle(Request request, boolean isForward) {
		return true;
	}

	@Override
	public int getNumberOfConstraints(Request request) {
		return 3;
	}

	@Override
	public int getNumberOfParameters(Request request) {
		return 3;
	}


	@Override
	public double[] getConstraintsBounds(Request request) {
		return new double[]{1,2,3};
	}

	@Override
	public boolean register(Edge edge, Request request) {
		return true;
	}

	@Override
	public boolean deregister(Edge edge, Request request) {
		return true;
	}
	@Override
	public double[] getNewParameters(Edge e, double[] oldParameters, Request request, boolean isForward) {
		return sumup(new double[]{1,2,-1}, oldParameters) ;
	}

	double[] sumup(double[] a , double[] b){
		if(b == null)
			return a;
		double result[] = new double[a.length];
		for(int i = 0; i < a.length; i++){
			result[i] = a[i] +b[i];
		}
		return result;
	}

	@Override
	public boolean hasAccess(Edge edge, Request request) {
		return true;
	}

	@Override
	public double getCost(Edge edge, Request request) {
		return 1;
	}

	@Override
	public double[] getConstraintsValues(Edge edge, Request request) {
		return new double[]{
				0.1,
				2,
				5};
	}
}

