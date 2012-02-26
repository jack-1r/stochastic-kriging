package se.sics.stochastic.sampling;

public interface Sampler {
	public double[] getNext();
	public boolean isEmpty();
}
