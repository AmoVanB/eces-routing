package de.tum.ei.lkn.eces.routing.proxies.plumbers;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.routing.mocks.DummyProxy;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.assertTrue;

public class PathPlumberProxyTest {

	@Before
	public final void setup() throws Exception {
	}

	@Test
	public void summationTest(){
		PathPlumberProxy proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1,2,3},
				new int[]{0,1,2,3} );
		proxy.setProxy(new DummyProxy());
		LinkedList<Edge> path = new LinkedList<>();
		double[] parameters = null;
		double[] result = {1.0,
				2.0,
				-1.0,
				1.0,
				0.1,
				2.0,
				5.0};
		for(int i = 0; i < 10; i++) {
			parameters = proxy.getNewParameters(path,null,parameters,null,true);
			for(int j = 0; j < parameters.length; j++){
				assertTrue("Parameter summation ( " +j+ " ) is wrong should be " + result[j] * (i+1) + " but is " + parameters[j],result[j] * (i+1) - parameters[j] < 0.00001 );
			}
			double cost = proxy.getCost(path,null,parameters,null,true);
			assertTrue("Cost sould be 1 but is " + cost, cost == 1);
			double[] constraint = proxy.getConstraintsValues(path, null, parameters, null, true);
			assertTrue("constraint[0] sould be 1 but is " + constraint[0], constraint[0] == 0.1);
			assertTrue("constraint[1] sould be 1 but is " + constraint[1], constraint[1] == 2.0);
			assertTrue("constraint[2] sould be 1 but is " + constraint[2], constraint[2] == 5.0);
		}
	}
}
