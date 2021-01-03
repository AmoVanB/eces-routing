package de.tum.ei.lkn.eces.routing.util;

import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.mocks.DummyEdgeProxy;
import de.tum.ei.lkn.eces.routing.requests.UnicastWithINRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Base test class for intermediate nodes algorithms algorithm.
 * Tests aren't run here but in the subclasses specific to each algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public abstract class INAlgorithmTest extends ShortestPathAlgorithmTest {
	protected RoutingAlgorithm referenceINAlgorithm;

	@Test
	public void simpleIntermediatNodeTest() {
		this.createGridTopology();
		proxy.setProxy(new DummyEdgeProxy(controller));

		Node source = nodes[0];
		Node[] intermediateNodes = new Node[]{nodes[5], nodes[14]};
		Node destination = nodes[nodes.length-1];

		checkIntermediateNodeResult(new UnicastWithINRequest(source, intermediateNodes, destination));
	}

	@Test
	public void firstINIsSource() {
		this.createGridTopology();
		proxy.setProxy(new DummyEdgeProxy(controller));

		Node source = nodes[0];
		Node[] intermediateNodes = new Node[]{nodes[0], nodes[14]};
		Node destination = nodes[nodes.length-1];

		checkIntermediateNodeResult(new UnicastWithINRequest(source, intermediateNodes, destination));
	}

	@Test
	public void secondINIsSource() {
		this.createGridTopology();
		proxy.setProxy(new DummyEdgeProxy(controller));

		Node source = nodes[0];
		Node[] intermediateNodes = new Node[]{nodes[4], nodes[0]};
		Node destination = nodes[5];

		checkIntermediateNodeResult(new UnicastWithINRequest(source, intermediateNodes, destination));

	}

	@Test
	public void twoConsecutiveIdenticalIN() {
		this.createGridTopology();
		proxy.setProxy(new DummyEdgeProxy(controller));

		Node source = nodes[0];
		Node[] intermediateNodes = new Node[]{nodes[14], nodes[14]};
		Node destination = nodes[nodes.length-1];

		checkIntermediateNodeResult(new UnicastWithINRequest(source, intermediateNodes, destination));
	}


	@Test
	public void firstINIsDestination() {
		this.createGridTopology();
		proxy.setProxy(new DummyEdgeProxy(controller));

		Node source = nodes[0];
		Node[] intermediateNodes = new Node[]{nodes[10], nodes[14]};
		Node destination = nodes[10];

		checkIntermediateNodeResult(new UnicastWithINRequest(source, intermediateNodes, destination));
	}

	@Test
	public void lastINIsDestination() {
		this.createGridTopology();
		proxy.setProxy(new DummyEdgeProxy(controller));

		Node source = nodes[0];
		Node[] intermediateNodes = new Node[]{nodes[14] ,nodes[10]};
		Node destination = nodes[10];

		checkIntermediateNodeResult(new UnicastWithINRequest(source, intermediateNodes, destination));
	}

	@Test
	public void twoINsAreTheSource() {
		this.createGridTopology();
		proxy.setProxy(new DummyEdgeProxy(controller));

		Node source = nodes[0];
		Node[] intermediateNodes = new Node[]{nodes[0], nodes[0]};
		Node destination = nodes[5];

		checkIntermediateNodeResult(new UnicastWithINRequest(source, intermediateNodes, destination));
	}

	@Test
	public void twoIdenticalINsButSeparated() {
		this.createGridTopology();
		proxy.setProxy(new DummyEdgeProxy(controller));

		Node source = nodes[0];
		Node[] intermediateNodes = new Node[]{nodes[14], nodes[13], nodes[14]};
		Node destination = nodes[12];
		checkIntermediateNodeResult(new UnicastWithINRequest(source, intermediateNodes, destination));

	}

	@Test
	public void waybackIntermediatNodeTest() {
		this.createLineTopology(5);
		proxy.setProxy(new DummyEdgeProxy(controller));

		for(int i = 1; i < nodes.length-2; i++) {
			Node source = nodes[i];
			Node[] intermediateNodes = new Node[]{nodes[0]};
			Node destination = nodes[nodes.length - 1];
			checkIntermediateNodeResult(new UnicastWithINRequest(source, intermediateNodes, destination));
		}
	}

	private void checkIntermediateNodeResult(UnicastWithINRequest request) {
		routingAlgorithmUnderTest.setDebugMode();
		Path autPath = (Path) routingAlgorithmUnderTest.solve(request);
		Path refPath = (Path) referenceINAlgorithm.solve(request);
		assertTrue(routingAlgorithmUnderTest + ": No path found", autPath != null);
		// Following also verifies that intermediate nodes are visited
		assertTrue(routingAlgorithmUnderTest + ": Proxy detects invalid path", proxy.isValid(autPath, request));
		assertTrue(routingAlgorithmUnderTest + ": Path does not have the same cost as the one found by ref alglorithm\nref:\t" + refPath + "\n\tcost: " + refPath.getCost() + "\nfound:\t" + autPath + "\n\tcost: " + autPath.getCost(), autPath.getCost() == refPath.getCost());

		//assertTrue(routingAlgorithmUnderTest + ": Path not equal to the one found by reference algorithm\nref:\t" + refPath + "\n\tcost: " + refPath.getCost() + "\nfound:\t" + autPath + "\n\tcost: " + autPath.getCost(), autPath.equals(refPath));
	}
}
