package se.sics.stochastic.kriging;

public class ExpPoint {
	private double fMin;
	private double[] oPoint;

	public ExpPoint(double fmin, double[] oPoint) {
		this.fMin = fmin;
		this.oPoint = oPoint;
	}

	public double getfMin() {
		return fMin;
	}

	public double[] getoPoint() {
		return oPoint;
	}
}
