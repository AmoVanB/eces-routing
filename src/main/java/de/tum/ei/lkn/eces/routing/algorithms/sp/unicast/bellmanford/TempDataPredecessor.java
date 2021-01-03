package de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.bellmanford;

import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.TempDataGuess;

public class TempDataPredecessor extends TempDataGuess {
	private TempDataPredecessor predecessor;

	@Override
	public void init(){
		super.init();
		predecessor = null;
	}

	public TempDataPredecessor getPredecessor() {
		return predecessor;
	}

	public void setPredecessor(TempDataPredecessor predecessor) {
		this.predecessor = predecessor;
	}
}
