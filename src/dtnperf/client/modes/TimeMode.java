package dtnperf.client.modes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import dtnperf.header.ClientMode;

public class TimeMode extends Mode {

	private LocalDateTime stopTime;
	private final int time;
	private final ChronoUnit unit;
	
	public TimeMode(int time, ChronoUnit unit) {
		this.time = time;
		this.unit = unit;
	}
	
	@Override
	public boolean isModeTerminated() {
		return LocalDateTime.now().isAfter(this.stopTime);
	}

	@Override
	protected void sentBundle() {
	}

	@Override
	public ClientMode getClientMode() {
		return ClientMode.Time;
	}

	@Override
	public void start() {
		this.stopTime = LocalDateTime.now().plus(Duration.of(this.time, this.unit));
	}

}
