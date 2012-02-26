package se.sics.kriging.components;

import se.sics.kompics.Event;
import se.sics.stochastic.kriging.ExpPoint;

public class NotifyExpResult extends Event {
	private ExpPoint optimal;
	
	public NotifyExpResult(ExpPoint point) {
		this.optimal = point;
	}

	public ExpPoint getOptimal() {
		return optimal;
	}
}
