package se.sics.kriging.components;

import se.sics.stochastic.helpers.Statistics;

public class ExperimentPoint {

	private int expId;
	private int repId;
	private double[] designPoint;
	private Statistics statCal;
	private boolean isReady = false;
	private int nTotal = 0;
	private int nCompleted = 0;
	
	public int getnTotal() {
		return nTotal;
	}
	
	public int getnCompleted() {
		return nCompleted;
	}
	
	public void increaseTotal() {
		++nTotal;
	}
	
	public void increaseCompleted() {
		++nCompleted;
	}

	public boolean isReady() {
		return isReady;
	}

	public void setReady(boolean isReady) {
		this.isReady = isReady;
	}

	public ExperimentPoint(int expId, int initRepId, int confidence) {
		this.expId = expId;
		this.repId = initRepId;
		this.designPoint = null;
		statCal = new Statistics(confidence);
	}
	
	public void setDesignPoint(double[] designPoint) {
		this.designPoint = designPoint;
	}

	public int getExpId() {
		return expId;
	}

	public double[] getDesignPoint() {
		return designPoint;
	}

	public Statistics getStatistics() {
		return statCal;
	}


	public int getRepId() {
		return repId;
	}
	
	public void increaseRepId() {
		++repId;
	}
}
