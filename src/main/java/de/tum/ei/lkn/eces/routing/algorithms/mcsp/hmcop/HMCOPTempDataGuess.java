package de.tum.ei.lkn.eces.routing.algorithms.mcsp.hmcop;

import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.TempData;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.TempDataGuess;

public class HMCOPTempDataGuess extends TempDataGuess {
	private boolean valid;
	private double g = Double.MAX_VALUE;

	public double getG() {
		return g;
	}

	public void setG(double g) {
		this.g = g;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	@Override
	public void init(){
		super.init();
		g = Double.MAX_VALUE;
		valid = false;
	}

	@Override
	public int compareTo(TempData other) {
		if((this.getSum() < ((TempDataGuess)other).getSum() || other.getCost() == Double.NaN) && this.isValid())
			return -1;
		if((this.getSum() > ((TempDataGuess)other).getSum() || this.getCost() == Double.NaN) && other.isValid())
			return 1;
		if(this.g < ((HMCOPTempDataGuess)other).g)
			return -1;
		if(this.g > ((HMCOPTempDataGuess)other).g)
			return 1;
		if(this.sqnum > (other).getSqnum())
			return 1;
		if(this.sqnum < (other).getSqnum())
			return -1;
		return 0;
	}

}
