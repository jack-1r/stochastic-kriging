package se.sics.stochastic.kriging;

import org.slf4j.Logger;

public class SASettings {
	
	private int nt;
	private int neps;
	private int maxEval;
	private double functol;
	private double paramtol;
	
	
	public SASettings() {
		nt = 90;
		neps = 20;
		maxEval = 20000;
		functol = 0.1;
		paramtol = 0.01;
	}
	
	public SASettings(int maxEval) {
		nt = 90;
		neps = 20;
		maxEval = 20000;
		functol = 0.1;
		paramtol = 0.01;
		this.maxEval = maxEval;
	}

	public int getNt() {
		return nt;
	}

	public void setNt(int nt) {
		this.nt = nt;
	}

	public int getNeps() {
		return neps;
	}

	public void setNeps(int neps) {
		this.neps = neps;
	}

	public int getMaxEval() {
		return maxEval;
	}

	public void setMaxEval(int maxEval) {
		this.maxEval = maxEval;
	}

	public double getFunctol() {
		return functol;
	}

	public void setFunctol(double functol) {
		this.functol = functol;
	}

	public double getParamtol() {
		return paramtol;
	}

	public void setParamtol(double paramtol) {
		this.paramtol = paramtol;
	}	
	
	public void display(Logger logger) {
		logger.info("nt={}", nt);
		logger.info("neps={}", neps);
		logger.info("Func_tol={}", functol);
		logger.info("param_tol={}", paramtol);
		logger.info("maxEval={}", maxEval);
	}
}
