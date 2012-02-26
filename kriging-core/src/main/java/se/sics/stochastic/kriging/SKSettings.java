package se.sics.stochastic.kriging;

import org.slf4j.Logger;

public class SKSettings {
	int gammaP;
	int algor;
	int maxEval;

	public SKSettings(int gammaP, int algor, int maxEval) {
		this.algor = algor;
		this.gammaP = gammaP;
		this.maxEval = maxEval;
	}

	public SKSettings() {
		algor = 0;
		gammaP = 2;
		maxEval = 1000000;
	}

	public int getGammaP() {
		return gammaP;
	}

	public void setGammaP(int gammaP) {
		this.gammaP = gammaP;
	}

	public void setAlgor(int algor) {
		this.algor = algor;
	}

	public void setMaxEval(int maxEval) {
		this.maxEval = maxEval;
	}

	public int getAlgor() {
		return algor;
	}

	public int getMaxEval() {
		return maxEval;
	}

	public void printSettings(Logger logger) {
		switch (algor) {
		case 0:
			logger.info("algorithm: interior-point");
			break;
		case 1:
			logger.info("algorithm: trust-reflective-region");
			break;
		case 2:
			logger.info("algorithm: active-set");
			break;
		default:
			logger.info("algorithm: interior-point");
		}
		
		switch (gammaP) {
		case 2:
			logger.info("correlation function: gaussian");
			break;
		case 1:
			logger.info("correlation function: exponential");
			break;
		case 0:
			logger.info("correlation function: cubic");
			break;
		default:
			logger.info("correlation function: gaussian");
		}
		
		logger.info("maxEval={}", maxEval);
	}
}
