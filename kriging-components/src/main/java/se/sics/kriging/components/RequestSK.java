package se.sics.kriging.components;

import se.sics.kompics.Event;
import se.sics.stochastic.kriging.SKSettings;

public class RequestSK extends Event {
	private SKSettings settings;
	private boolean canProcceed;

	public RequestSK() {
		settings = new SKSettings();
		canProcceed = true;
	}

	public RequestSK(SKSettings settings) {
		this.settings = settings;
		canProcceed = false;
	}

	public boolean isCanProcceed() {
		return canProcceed;
	}

	public SKSettings getSettings() {
		return settings;
	}
}
