package de.tum.ei.lkn.eces.routing;

import de.tum.ei.lkn.eces.core.Entity;
import de.tum.ei.lkn.eces.core.Mapper;
import de.tum.ei.lkn.eces.core.MapperSpace;
import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.larac.LARACAlgorithm;
import de.tum.ei.lkn.eces.routing.mappers.DeleteRequestMapper;
import de.tum.ei.lkn.eces.routing.mappers.DoNotRouteMapper;
import de.tum.ei.lkn.eces.routing.mappers.SelectedRoutingAlgorithmMapper;
import de.tum.ei.lkn.eces.routing.mocks.DummyComponent;
import de.tum.ei.lkn.eces.routing.mocks.DummyEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.util.BaseTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class RoutingSystemTest extends BaseTest {
	private RoutingSystem routingSystem;
	private RoutingAlgorithm ra;
    private Mapper<SelectedRoutingAlgorithm> selectedRoutingAlgorithmMapper;
    private Mapper<DeleteRequest> deleteRequestMapper;

    @Before
	public void setUp() {
		super.setUp();
		routingSystem = new RoutingSystem(controller);
		selectedRoutingAlgorithmMapper = new SelectedRoutingAlgorithmMapper(controller);
		deleteRequestMapper = new DeleteRequestMapper(controller);
		ra = new LARACAlgorithm(controller);
		PathPlumberProxy proxy = new PathPlumberProxy(new int[]{0}, new double[]{1}, new int[]{1}, new int[]{});
		proxy.setProxy(new DummyEdgeProxy(controller));
		ra.setProxy(proxy);
		this.createBaseTopology();
	}

	@Test
	public final void defaultTest() {
		Entity ent = controller.createEntity();
        this.selectedRoutingAlgorithmMapper.attachComponent(ent, new SelectedRoutingAlgorithm(ra));
        UnicastRequest req =  new UnicastRequest(nodes[0], nodes[2]);
		this.requestMapper.attachComponent(ent, req);

		assertTrue("No Edge Path computed", this.edgePathMapper.isIn(ent));
		assertEquals("Hop count wrong", 2, this.edgePathMapper.get(ent).getPath().length);
		assertSame("Wrong edge used", this.edgePathMapper.get(ent).getPath()[0], edges[0]);
		assertSame("Wrong edge used", this.edgePathMapper.get(ent).getPath()[1], edges[1]);
		assertTrue("Request not in the handled requests", routingSystem.handledRequests.size() == 1 && routingSystem.handledRequests.containsKey(req.getId()));

		Entity ent2 = controller.createEntity();
        this.selectedRoutingAlgorithmMapper.attachComponent(ent2, new SelectedRoutingAlgorithm(ra));
        dummyMapper.get(edges[1].getEntity()).use = false;
		this.requestMapper.attachComponent(ent2, new UnicastRequest(nodes[0], nodes[2]));


		assertTrue("No Edge Path computed", this.edgePathMapper.isIn(ent2));
		Path path = this.edgePathMapper.get(ent2);
		assertFalse(Arrays.asList(path.getPath()).contains(edges[1]));
	}

	@Test
	public final void dynamicRoutingSwitchTest() {
		Entity ent = controller.createEntity();

		try(MapperSpace ms = controller.startMapperSpace()) {
            this.selectedRoutingAlgorithmMapper.attachComponent(ent, new SelectedRoutingAlgorithm(ra));
            this.requestMapper.attachComponent(ent, new UnicastRequest(nodes[0], nodes[2]));
			this.dummyMapper.attachComponent(ent, new DummyComponent());
		}

		assertTrue("No Edge Path computed", this.edgePathMapper.isIn(ent));
		assertEquals("Hop count wrong", 2, this.edgePathMapper.get(ent).getPath().length);
		assertSame("Wrong edge used", this.edgePathMapper.get(ent).getPath()[0], edges[0]);
		assertSame("Wrong edge used", this.edgePathMapper.get(ent).getPath()[1], edges[1]);
		assertEquals("Wrong size of the handled request array", 1, routingSystem.handledRequests.size());

		Entity ent2 = controller.createEntity();

		dummyMapper.get(edges[1].getEntity()).use = false;
		try(MapperSpace ms = controller.startMapperSpace()) {
            this.selectedRoutingAlgorithmMapper.attachComponent(ent2, new SelectedRoutingAlgorithm(ra));
            this.requestMapper.attachComponent(ent2, new UnicastRequest(nodes[0], nodes[2]));
			this.dummyMapper.attachComponent(ent2, new DummyComponent());
		}

		assertTrue("No Edge Path computed", this.edgePathMapper.isIn(ent2));
		Path path = this.edgePathMapper.get(ent2);
		assertEquals("Hop count wrong", 4, path.getPath().length);
		assertSame("Wrong edge used", path.getPath()[0], edges[8]);
		assertSame("Wrong edge used", path.getPath()[1], edges[4]);
		assertSame("Wrong edge used", path.getPath()[2], edges[5]);
		assertSame("Wrong edge used", path.getPath()[3], edges[9]);
		assertEquals(2, routingSystem.handledRequests.size());

		deleteRequestMapper.attachComponent(controller.createEntity(), new DeleteRequest(ent.getId()));
        assertEquals(1, routingSystem.handledRequests.size());
        deleteRequestMapper.attachComponent(controller.createEntity(), new DeleteRequest(ent.getId()));
        assertEquals(1, routingSystem.handledRequests.size());
        deleteRequestMapper.attachComponent(controller.createEntity(), new DeleteRequest(ent2.getId()));
        assertEquals(0, routingSystem.handledRequests.size());
    }

	@Test
	public final void doNotRouteTest() {
		Entity ent = controller.createEntity();

		try (MapperSpace ms = controller.startMapperSpace()) {
            this.selectedRoutingAlgorithmMapper.attachComponent(ent, new SelectedRoutingAlgorithm(ra));
            this.requestMapper.attachComponent(ent, new UnicastRequest(nodes[0], nodes[2]));
			new DoNotRouteMapper(controller).attachComponent(ent, new DoNotRoute());
		}

		assertFalse(this.edgePathMapper.isIn(ent));
		assertEquals(0, routingSystem.handledRequests.size());

		Entity ent2 = controller.createEntity();
		dummyMapper.get(edges[1].getEntity()).use = false;

		try (MapperSpace ms = controller.startMapperSpace()) {
            this.selectedRoutingAlgorithmMapper.attachComponent(ent2, new SelectedRoutingAlgorithm(ra));
            this.requestMapper.attachComponent(ent2, new UnicastRequest(nodes[0], nodes[2]));
			new DoNotRouteMapper(controller).attachComponent(ent2, new DoNotRoute());
		}

		assertFalse(this.edgePathMapper.isIn(ent2));
		assertEquals(0, routingSystem.handledRequests.size());
	}
}
