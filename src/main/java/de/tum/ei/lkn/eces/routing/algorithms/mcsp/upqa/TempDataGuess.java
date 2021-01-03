package de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa;

// Adds a guess to TempData.
public class TempDataGuess extends TempData{

	private double guess;
	private double sum;
	public TempDataGuess() {
		init();
	}


	public double getSum() {
		return sum;
	}

	@Override
	public void init() {
		super.init();
		guess = 0;
		sum = this.cost;
	}

	public double getGuess(){
		return guess;
	}

	@Override
	public void setCost(double cost){
		this.sum = cost + this.getGuess();
		this.cost = cost;
	}

	public void setGuess(double guess) {
		this.sum = guess + this.getCost();
		this.guess = guess;
	}

	@Override
	public int compareTo(TempData other) {
		if(this.sum >((TempDataGuess)other).sum)
			return 1;
		if(this.sum <((TempDataGuess)other).sum)
			return -1;
		if(this.getCost() > other.getCost())
			return -1;
		if(this.getCost() < other.getCost())
			return 1;
		if(this.sqnum > ((TempDataGuess)other).sqnum)
			return 1;
		if(this.sqnum < ((TempDataGuess)other).sqnum)
			return -1;
		return 0;
	}



}
