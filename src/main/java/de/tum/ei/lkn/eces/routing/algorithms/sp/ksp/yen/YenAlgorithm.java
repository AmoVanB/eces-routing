package de.tum.ei.lkn.eces.routing.algorithms.sp.ksp.yen;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.sp.ksp.KSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.astar.AStarAlgorithm;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.ProxyTypes;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;

import java.util.*;

/**
 * 1971
 * "Finding the K Shortest Loopless Paths in a Network"
 * Jin Y. Yen.
 *
 * (Nicer pseudo-code:
 *  https://en.wikipedia.org/wiki/Yen%27s_algorithm)
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class YenAlgorithm extends KSPAlgorithm {
	/**
	 * Underlying shortest path algorithm.
	 */
	private AStarAlgorithm spAlgorithm;

	/**
	 * Proxy used to block the access to some of the links.
	 */
	private BlockingProxy blockProxy;

	public YenAlgorithm(Controller controller) {
		this(controller, ProxyTypes.EDGE_PROXY);
	}

	public YenAlgorithm(Controller controller, ProxyTypes maxProxy) {
		super(controller);
		spAlgorithm = new AStarAlgorithm(controller);
		spAlgorithm.setMaximumProxy(maxProxy);
	}

	@Override
	public int getK(UnicastRequest request) {
		return ((YenKSPIterator) this.getCurrentIterator(request)).getK();
	}

	@Override
	public void setProxy(PathProxy proxy) {
		blockProxy = new BlockingProxy(proxy);
		spAlgorithm.setProxy(blockProxy);
		super.setProxy(proxy);
	}

	@Override
	public void setProxy(PreviousEdgeProxy proxy) {
		blockProxy = new BlockingProxy(proxy);
		spAlgorithm.setProxy(blockProxy);
		super.setProxy(proxy);
	}

	@Override
	public void setProxy(EdgeProxy proxy) {
		blockProxy = new BlockingProxy(proxy);
		spAlgorithm.setProxy(blockProxy);
		super.setProxy(proxy);
	}

	@Override
	public Iterator<Path> iterator(UnicastRequest request) {
		return new YenKSPIterator(spAlgorithm, blockProxy, request);
	}

	@Override
	public boolean isForward() {
		return true;
	}

	@Override
	public boolean isOptimal() {
		return this.spAlgorithm.isOptimal() &&
				this.spAlgorithm.isComplete() &&
				proxy.getType() == ProxyTypes.EDGE_PROXY;
	}

	@Override
	public boolean isComplete() {
		return this.spAlgorithm.isOptimal() &&
				this.spAlgorithm.isComplete() &&
				proxy.getType() == ProxyTypes.EDGE_PROXY;
	}

	@Override
	public boolean isValid() {
		return true;
	}

}

class YenKSPIterator implements Iterator<Path> {
	/**
	 * Stores the potential kth shortest paths.
	 */
	protected TreeSet<Path> pathCandidates;

	/**
	 * Stores the latest Path found.
	 */
	private Path lastResult;

	/**
	 * Stores the currently blocked elements for each rootPath.
	 */
	private Map<List<Edge>, NodesAndEdgesSet> blockedElements;

	/**
	 * Proxy used to block the above elements.
	 */
	private BlockingProxy blockProxy;

	/**
	 * Algorithm used for SP routing.
	 */
	private AStarAlgorithm algorithm;

	/**
	 * Request for which the kSP have to be found.
	 */
	private UnicastRequest request;

	/**
	 * Current number of Paths returned by next().
	 */
	private int k;

	/**
	 * Current number of Paths computed by hasNext().
	 */
	private int computedK;

	public YenKSPIterator(AStarAlgorithm algorithm, BlockingProxy blockProxy, UnicastRequest request) {
		this.algorithm = algorithm;
		this.blockProxy = blockProxy;
		this.request = request;
		this.lastResult = null;
		this.blockedElements = new HashMap<>();
		this.pathCandidates = new TreeSet<>((p1, p2) -> {
			if(p1.getCost() < p2.getCost())
				return -1;
			if(p1.getCost() > p2.getCost())
				return 1;
			if(p1.hashCode() < p2.hashCode())
				return -1;
			if(p1.hashCode() > p2.hashCode())
				return 1;
			return 0;
		});
		this.k = 0;
		this.computedK = 0;
	}

	/**
	 * Returns the number of Paths already returned.
	 * @return k.
	 */
	public int getK() {
		return k;
	}

	/**
	 * Returns Edge #id on a given Path.
	 * @param id Number of the Edge to return.
	 * @param path The Path.
	 * @return The EDGE or null if no such EDGE.
	 */
	private Edge getEdge(int id, Path path) {
		if(id < path.getPath().length)
			return path.getPath()[id];
		else
			return null;
	}

	@Override
	public boolean hasNext() {
		if(computedK > k) {
			// We already computed next k, hence return the same answer.
			return lastResult != null;
		}

		if(computedK == 0) {
			Path path = (Path) algorithm.solve(request);
			lastResult = path;
			computedK++;
			return path != null;
		}

		Path currentPath = lastResult;

		Edge[] pathArray = currentPath.getPath();
		LinkedList<Edge> rootPath = new LinkedList<>();

		for(int i = 0; i < pathArray.length; i++) {
			if(i != 0)
				rootPath.add(getEdge(i - 1,currentPath));

			/* Computing the elements to block.
			 * Compared to pseudo-code, we do not have to go through
			 * all the previously returned paths because we store
			 * the blocked elements *per root path*. If the current
			 * root path matches a root path from one of the first
			 * returned paths, then the corresponding elements to
			 * block have already been stored at the iteration after
			 * the latter path. For the current iteration, we can hence
			 * retrieve the elements to block from the previous iterations
			 * and additionally block the nodes of the root path and the
			 * next edge used by the last path.
			 */
			NodesAndEdgesSet listToBlock = blockedElements.get(rootPath);
			if(listToBlock == null) {
				listToBlock = new NodesAndEdgesSet();
				listToBlock.nodes.add(currentPath.getPath()[0].getSource());
				for(int j = 0; j < i; j++)
					listToBlock.nodes.add(currentPath.getPath()[j].getDestination());

				blockedElements.put(new LinkedList<>(rootPath), listToBlock);
			}

			Edge nextEdge = getEdge(i, currentPath);
			if(listToBlock.edges.contains(nextEdge))
				continue;
			// Block next Edge used by the previous Path.
			listToBlock.edges.add(nextEdge);

			// Blocking the elements that have to be blocked.
			NodesAndEdgesSet blockLists = blockedElements.getOrDefault(rootPath, new NodesAndEdgesSet());
			for(Edge edge : blockLists.edges)
				blockProxy.setBlocked(edge);
			for(Node node : blockLists.nodes)
				blockProxy.setBlocked(node);

			// Computing the spur path.
			Path newPath = algorithm.solveNoChecks(request, rootPath);
			if(newPath != null) {
				pathCandidates.add(newPath);
			}
			// Unblock everything for next iteration.
			blockProxy.unblockAll();
		}

		computedK++;
		if(pathCandidates.isEmpty())
			return false;
		else {
			lastResult = pathCandidates.pollFirst();
			return true;
		}
	}

	@Override
	public Path next() {
		while(computedK <= k)
			this.hasNext();
		k++;
		return lastResult;
	}
}

/**
 * Set of Nodes and Edges.
 */
class NodesAndEdgesSet {
	protected Set<Edge> edges = new HashSet<>();
	protected Set<Node> nodes = new HashSet<>();
}
