package dtnperf.client;

import dtnperf.client.modes.DataUnit;

public enum RateUnit {
KILOBIT, MEGABIT, BUNDLE;
	
	public double sleepInSeconds(Client client) {
		switch (this) {
		case KILOBIT: return (client.getPayloadSize() * 8d / DataUnit.KILOBYTES.getBytes());
		case MEGABIT: return (client.getPayloadSize() * 8d / DataUnit.MEGABYTES.getBytes());
		case BUNDLE: return 1;
		
		default:
			throw new IllegalStateException("RateUnit not found.");
		}
	}
	
}
