package de.tum.ei.lkn.eces.routing.mocks;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.Mapper;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.requests.Request;

public class DummyEdgeProxy extends EdgeProxy {
	private double[] bounds = new double[]{1,2,3};
	private Mapper<DummyComponent> dummyMapper;

	public DummyEdgeProxy(Controller controller) {
		dummyMapper = new DummyComponentMapper(controller);
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
		return bounds;
	}

	public void setBounds(double[] bounds){
		this.bounds = bounds;
	}

	@Override
	public boolean register(Edge edge, Request request) {
		dummyMapper.get(edge.getEntity()).count++;
		return true;
	}

	@Override
	public boolean deregister(Edge edge, Request request) {
		dummyMapper.get(edge.getEntity()).count--;
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
		return dummyMapper.getOptimistic(edge.getEntity()).use && dummyMapper.get(edge.getEntity()).count < 100;
	}

	@Override
	public double getCost(Edge edge, Request request) {
		DummyComponent dc = dummyMapper.getOptimistic(edge.getEntity());
		return dc.cost;
	}

	@Override
	public double[] getConstraintsValues(Edge edge, Request request) {
		DummyComponent data = dummyMapper.getOptimistic(edge.getEntity());
		return new double[]{
				data.delay,
				data.loss,
				0.1
		};
	}

}
