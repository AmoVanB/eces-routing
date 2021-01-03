package de.tum.ei.lkn.eces.routing.algorithms.sp.ksp.yen;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.proxies.ProxyTypes;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Adaptation of Yen's algorithm when k is known in advance.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class StaticYenAlgorithm {
	private YenAlgorithm yenAlgorithm;

	public StaticYenAlgorithm(Controller controller) {
		this(controller, ProxyTypes.EDGE_PROXY);
	}

	public StaticYenAlgorithm(Controller controller, ProxyTypes maxProxy) {
		yenAlgorithm = new YenAlgorithm(controller, maxProxy);
	}

	public Set<Path> getkPath(UnicastRequest request, int k) {
		YenKSPIterator iterator = (YenKSPIterator) yenAlgorithm.iterator(request);
		Set<Path> list = new LinkedHashSet<>();
		while(iterator.pathCandidates.size() < k - list.size() && iterator.hasNext()){
			list.add(iterator.next());
		}
		while (list.size() < k && !iterator.pathCandidates.isEmpty()){
			list.add(iterator.pathCandidates.pollFirst());
		}
		return list;
	}

	public void setProxy(PathPlumberProxy proxy) {
		yenAlgorithm.setProxy(proxy);
	}
}
