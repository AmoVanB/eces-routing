package de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa;

public class TempData implements Comparable<TempData> {
	private TempIterablePath path = null;
	protected double cost = Double.MAX_VALUE;
	private double[] constraint;
	private double[] parameter;
	boolean visited = false;
	protected int sqnum = 0;
	private boolean valid;
	private boolean removeFirst;

	public TempIterablePath getPath() {
		return path;
	}

	public void setPath(TempIterablePath path) {
		this.path = path;
	}

	public double[] getParameter() {
		return parameter;
	}

	public void setParameter(double[] parameter) {
		this.parameter = parameter;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public double[] getConstraint() {
		return constraint;
	}

	public void setConstraint(double[] constraint) {
		this.constraint = constraint;
	}

	public int getSqnum() {
		return sqnum;
	}

	public void setSqnum(int sqnum) {
		this.sqnum = sqnum;
	}

	public void init(){
		path = new TempIterablePath(null);
		cost = Double.MAX_VALUE;
		constraint = null;
		parameter = null;
		visited = false;
		removeFirst = false;
	}

	void setToRemoveFirst(){
		removeFirst = true;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	@Override
	public int compareTo(TempData other) {
		if(this.getCost() > other.getCost())
			return 1;
		if(this.getCost() < other.getCost())
			return -1;
		if(this.sqnum > (other).sqnum)
			return 1;
		if(this.sqnum < (other).sqnum)
			return -1;
		return 0;
	}

	public boolean isValid() {
		return valid;
	}

	public boolean isRemoveFirst() {
		return removeFirst;
	}
}
