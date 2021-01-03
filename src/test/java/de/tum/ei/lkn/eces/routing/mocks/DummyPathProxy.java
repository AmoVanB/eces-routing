package de.tum.ei.lkn.eces.routing.mocks;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.Mapper;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.requests.Request;

import static org.junit.Assert.assertTrue;

public class DummyPathProxy extends PathProxy {
	private double[] bounds = new double[]{1,2,3};
	Mapper<DummyComponent> dummyMapper;

	public DummyPathProxy(Controller controller) {
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
	public double[] getNewParameters(Iterable<Edge> path, Edge edge, double[] oldParameters, Request request, boolean isForward) {
		pathTest(path,edge,isForward);
		return sumup(new double[]{1,2,-1}, oldParameters) ;
	}

	@Override
	public boolean hasAccess(Iterable<Edge> path, Edge edge, double patrameters[], Request request, boolean isForward) {
		pathTest(path,edge,isForward);
		return dummyMapper.getOptimistic(edge.getEntity()).use && dummyMapper.get(edge.getEntity()).count < 100;
	}

	@Override
	public double getCost(Iterable<Edge> path, Edge edge, double patrameters[], Request request, boolean isForward) {
		pathTest(path,edge,isForward);
		DummyComponent dc = dummyMapper.getOptimistic(edge.getEntity());
		return dc.cost;
	}

	@Override
	public double[] getConstraintsValues(Iterable<Edge> path, Edge edge, double patrameters[], Request request, boolean isForward) {
		pathTest(path,edge,isForward);
		DummyComponent data = dummyMapper.getOptimistic(edge.getEntity());
		return new double[]{
				data.delay,
				data.loss,
				0.1
		};
	}

	@Override
	public boolean register(Iterable<Edge> path, Edge edge, double[] parameters, Request request) {
		dummyMapper.get(edge.getEntity()).count++;
		return true;
	}

	@Override
	public boolean deregister(Iterable<Edge> path, Edge edge, double[] parameters, Request request) {
		dummyMapper.get(edge.getEntity()).count--;
		return true;
	}

	protected void pathTest(Iterable<Edge> path, Edge edge, boolean isForward){
		Edge testedge = edge;
		for(Edge myEdge: path){
			if(isForward){
				assertTrue("The Path provided by the algorithm is not connected", testedge.getSource() == myEdge.getDestination());
			}else{
				assertTrue("The Path provided by the algorithm is not connected", myEdge.getSource() == testedge.getDestination());
			}
			testedge = myEdge;
		}
	}
}
