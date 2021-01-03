package de.tum.ei.lkn.eces.routing.algorithms.mcsp.hmcop;

import de.tum.ei.lkn.eces.core.LocalComponent;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import de.tum.ei.lkn.eces.routing.RoutingSystem;

@ComponentBelongsTo(system = RoutingSystem.class)
public class HMCOPTempDataGuessLocal extends LocalComponent {

	@Override
	public Object init() {
		return new HMCOPTempDataGuess();
	}
}
