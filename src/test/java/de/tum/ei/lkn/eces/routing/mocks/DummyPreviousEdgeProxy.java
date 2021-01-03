package de.tum.ei.lkn.eces.routing.mocks;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.Mapper;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.requests.Request;

import static org.junit.Assert.assertTrue;

public class DummyPreviousEdgeProxy extends PreviousEdgeProxy {
	private double[] bounds = new double[]{1,2,3};
	Mapper<DummyComponent> dummyMapper;
	private Edge accessSwitch = null;
	private Edge costSwitch = null;
	private Edge constSwitch = null;
	private Edge edge = null;

	public DummyPreviousEdgeProxy(Controller controller) {
		dummyMapper = new DummyComponentMapper(controller);
	}

	public void setAccessSwitch(Edge accessSwitch, Edge edge) {
		this.edge = edge;
		this.accessSwitch = accessSwitch;
	}

	public void setCostSwitch(Edge costSwitch, Edge edge) {
		this.edge = edge;
		this.costSwitch = costSwitch;
	}

	public void setConstSwitch(Edge constSwitch, Edge edge) {
		this.edge = edge;
		this.constSwitch = constSwitch;
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
	public boolean register(Edge previousEdge, Edge edge, Request request) {
		dummyMapper.get(edge.getEntity()).count++;
		return true;
	}

	@Override
	public boolean deregister(Edge previousEdge, Edge edge, Request request) {
		dummyMapper.get(edge.getEntity()).count--;
		return true;
	}

	@Override
	public double[] getNewParameters(Edge pe, Edge edge, double[] oldParameters, Request request, boolean isForward) {
		pEdgeTest(pe,edge,isForward);
		return sumup(new double[]{1,2,-1}, oldParameters) ;
	}

	double[] sumup(double[] a , double[] b){
		if(b == null)
			return a;
		double[] result = new double[a.length];
		for(int i = 0; i < a.length; i++){
			result[i] = a[i] +b[i];
		}
		return result;
	}

	@Override
	public boolean hasAccess(Edge pe, Edge edge, Request request, boolean isForward) {
		pEdgeTest(pe,edge,isForward);
		if(this.edge != null && this.accessSwitch != null &&
				((isForward && pe == accessSwitch && this.edge == edge) ||
						(!isForward && edge == accessSwitch && this.edge == pe)))
			return false;
		return dummyMapper.getOptimistic(edge.getEntity()).use && dummyMapper.get(edge.getEntity()).count < 100;
	}

	@Override
	public double getCost(Edge pe, Edge edge, Request request, boolean isForward) {
		pEdgeTest(pe,edge,isForward);
		if(this.edge != null && this.costSwitch != null &&
				((isForward && pe == costSwitch && this.edge == edge) ||
						(!isForward && edge == costSwitch && this.edge == pe)))
			return 1000;
		DummyComponent dc = dummyMapper.getOptimistic(edge.getEntity());
		return dc.cost;
	}

	@Override
	public double[] getConstraintsValues(Edge pe, Edge edge, Request request, boolean isForward) {
		pEdgeTest(pe,edge,isForward);
		if(this.edge != null && this.constSwitch != null &&
				((isForward && pe == constSwitch && this.edge == edge) ||
						(!isForward && edge == constSwitch && this.edge == pe)))
			return new double[]{
					1000,
					1000,
					0.1};
		DummyComponent data = dummyMapper.getOptimistic(edge.getEntity());
		return new double[]{
				data.delay,
				data.loss,
				0.1
		};
	}

	protected void pEdgeTest(Edge pEdge, Edge edge, boolean isForward){
		if(pEdge != null)
			if(isForward){
				if(pEdge.getDestination() != edge.getSource())
					System.out.print("bla");
				assertTrue("The previous EDGE provided by the algorithm is not connected", pEdge.getDestination() == edge.getSource());
			}else{
				assertTrue("The previous EDGE provided by the algorithm is not connected", edge.getDestination() == pEdge.getSource());
			}
	}
}
