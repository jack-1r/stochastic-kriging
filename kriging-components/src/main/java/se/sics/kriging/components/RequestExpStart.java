package se.sics.kriging.components;

import java.io.Serializable;

import se.sics.kompics.Request;

public class RequestExpStart extends Request implements Serializable{
	/**
     * 
     */
    private static final long serialVersionUID = -8240981906985076219L;
    
    private String JarPath;
	private double[] lb;
	private double[] ub;
	private boolean[] intVector; 
	private int num;
	private int confidenceLevel;
	private double threshold;
	private double confidenceLength;
	private int nRuns;
	private boolean isFixedRun;
	private int nFixedRun;
		
	public int getEffnCores() {
		return nRuns;
	}
	public void setnRuns(int nRuns) {
		this.nRuns = nRuns;
	}
	public boolean[] getIntVector() {
		return intVector;
	}
	public void setIntVector(boolean[] intVector) {
		this.intVector = intVector;
	}
	public int getConfidenceLevel() {
		return confidenceLevel;
	}
	public void setConfidenceLevel(int confidenceLevel) {
		this.confidenceLevel = confidenceLevel;
	}
	public double getConfidenceLength() {
		return confidenceLength;
	}
	public void setConfidenceLength(double confidenceLength) {
		this.confidenceLength = confidenceLength;
	}
	
	public double getRatio() {
		return confidenceLength;
	}
	public void setJarPath(String JarPath) {
		this.JarPath = JarPath;
	}
	public void setLb(double[] lb) {
		this.lb = lb;
	}
	public void setUb(double[] ub) {
		this.ub = ub;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public void setConfidence(int confidence) {
		this.confidenceLevel = confidence;
	}
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
	public void setRatio(double ratio) {
		this.confidenceLength = ratio;
	}
	public String getJarPath() {
		return JarPath;
	}
	public double[] getLb() {
		return lb;
	}
	public double[] getUb() {
		return ub;
	}
	public int getNum() {
		return num;
	}
	public int getConfidence() {
		return confidenceLevel;
	}
	public double getThreshold() {
		return threshold;
	}
	public void setnFixedRun(int nFixedRun) {
		this.nFixedRun = nFixedRun;
	}
	public int getnFixedRun() {
		return nFixedRun;
	}
	public void setFixedRun(boolean isFixedRun) {
		this.isFixedRun = isFixedRun;
	}
	public boolean isFixedRun() {
		return isFixedRun;
	}
}
