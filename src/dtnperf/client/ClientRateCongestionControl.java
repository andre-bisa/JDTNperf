package dtnperf.client;

import java.util.concurrent.Semaphore;

class ClientRateCongestionControl extends ClientCongestionControl {

	private final int number;
	private final RateUnit rateUnit;
	
	
	public ClientRateCongestionControl(Client client, RateCongestionControl congestionControl) {
		super(client, new Semaphore(0), congestionControl.isAckRequired());
		
		this.number = congestionControl.getNumber();
		this.rateUnit = congestionControl.getRateUnit();
	}

	@Override
	protected void waitForNext() {
		double sleepInMillis = this.rateUnit.sleepInSeconds(this.getClient()) * 1000 / this.number;
		long timeToSleepMillis = (long) sleepInMillis;
		int timeToSleepNanos = (int) ((sleepInMillis - timeToSleepMillis) * 1000000);
		try {
			Thread.sleep(timeToSleepMillis, timeToSleepNanos);
		} catch (InterruptedException e) {}
	}

	@Override
	protected void closing() {
	}

}
