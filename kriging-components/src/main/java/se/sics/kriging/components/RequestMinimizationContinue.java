package se.sics.kriging.components;

import se.sics.kompics.Event;
import se.sics.stochastic.kriging.SASettings;

public class RequestMinimizationContinue extends Event {
	private SASettings settings;

	public RequestMinimizationContinue() {
		settings = new SASettings();
	}

	public RequestMinimizationContinue(SASettings settings) {
		this.settings = settings;
	}

	public SASettings getSettings() {
		return settings;
	}
}
