package se.sics.kriging.components;

import se.sics.kompics.Event;
import se.sics.stochastic.kriging.SASettings;

public class RequestMSEMinimization extends Event {
	private SASettings settings;

	public RequestMSEMinimization() {
		settings = new SASettings();
	}

	public RequestMSEMinimization(SASettings settings) {
		this.settings = settings;
	}

	public SASettings getSettings() {
		return settings;
	}
}
