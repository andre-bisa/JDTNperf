package dtnperf.client;

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
		final boolean result = LocalDateTime.now().isAfter(this.stopTime);
		if (result) {
			super.setStop();
		}
		return result;
	}

	@Override
	protected void sentBundle() {
	}

	@Override
	public ClientMode getClientMode() {
		return ClientMode.Time;
	}

	@Override
	public void _start() {
		this.stopTime = super.getStartTime().plus(Duration.of(this.time, this.unit));
	}

}
