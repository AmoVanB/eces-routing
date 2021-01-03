package de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa;

public class Record {
	private TempData[] data;

	public boolean hasArray(){
		return data != null;
	}

	public void setArray(TempData[] data){
		this.data = data;
	}

	public int getMaxIDnotVisited() {
		int index = -1;
		TempData max = null;
		for(int i = 0; i < data.length; i++){
			if(data[i] == null){
				return i;
			}
			if((max == null || max.getCost() < data[i].cost) && !data[i].isVisited() ){
				max = data[i];
				index = i;
			}
		}
		return index;
	}

	public void initRoot(double[] parameter, double[] constraints) {
		if(data != null) {
			for (TempData datum : data) {
				datum.init();
				datum.cost = 0;
				datum.visited = true;
				datum.setConstraint(constraints);
				datum.setParameter(parameter);
				datum.setPath(new TempIterablePath(null));
			}
		}
	}

	public int getMaxID() {
		int index = -1;
		TempData max = null;
		for(int i = 0; i < data.length; i++){
			if(data[i] == null){
				return i;
			}
			if((max == null || max.getCost() < data[i].cost) ){
				max = data[i];
				index = i;
			}
		}
		return index;
	}

	public int getMinID() {
		int index = -1;
		TempData min = null;
		for(int i = 0; i < data.length; i++){
			if(data[i] != null && (min == null || min.getCost() > data[i].cost)){
				min = data[i];
				index = i;
			}
		}
		return index;
	}

	public TempData getTempData(int index){
		return data[index];
	}

	public void init() {
		if(data != null) {
			for(int i = 0; i < data.length; i++)
				data[i] = null;
		}
	}

	public TempData[] getArray(){
		return data;
	}
}
