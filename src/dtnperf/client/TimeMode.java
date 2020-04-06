package dtnperf.client;

import java.time.temporal.ChronoUnit;

public class TimeMode extends Mode {
	
	private final int time;
	private final ChronoUnit unit;
	
	public TimeMode(int time, ChronoUnit unit) {
		this.time = time;
		this.unit = unit;
	}

	public int getTime() {
		return time;
	}

	public ChronoUnit getUnit() {
		return unit;
	}
	
}
