package dtnperf.client;

import java.util.concurrent.Semaphore;


public class ClientCongestionControlRate extends ClientCongestionControl {

	private int number;
	private RateUnit rateUnit;
	
	public ClientCongestionControlRate(int number, RateUnit rateUnit) {
		super(new Semaphore(1));
		
		this.number = number;
		this.rateUnit = rateUnit;
	}

	@Override
	protected boolean waitForNext() {
		double sleepInMillis = this.rateUnit.sleepInSeconds(this.getClient()) * 1000 / this.number;
		long timeToSleepMillis = (long) sleepInMillis;
		int timeToSleepNanos = (int) ((sleepInMillis - timeToSleepMillis) * 1000000);
		try {
			Thread.sleep(timeToSleepMillis, timeToSleepNanos);
		} catch (InterruptedException e) {}
		return true;
	}

	@Override
	public boolean isAckRequired() {
		return false;
	}

}
