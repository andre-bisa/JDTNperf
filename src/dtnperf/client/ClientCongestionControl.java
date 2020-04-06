package dtnperf.client;

import java.util.concurrent.Semaphore;

abstract class ClientCongestionControl implements Runnable {
	
	private final Semaphore semaphore;
	private final Client client;
	private final boolean ackRequired;
	
	protected ClientCongestionControl (Client client, Semaphore semaphore, boolean ackRequired) {
		this.semaphore = semaphore;
		this.client = client;
		this.ackRequired = ackRequired;
	}
	
	protected Client getClient() {
		return this.client;
	}
	
	protected abstract void waitForNext() throws Exception;
	protected abstract void closing();
	
	public void waitToSend() throws InterruptedException {
		this.semaphore.acquire();
	}
	
	public boolean isAckRequired() {
		return this.ackRequired;
	}
	
	@Override
	public void run() {
		try {
			while (this.client.isRunning()) {
				this.waitForNext();
				this.semaphore.release();
			}
		} catch(Exception e) {

		}
		this.closing();
	}
	
	public static ClientCongestionControl of(Client client, CongestionControl congestionControl) {
		if (congestionControl == null || client == null) {
			throw new IllegalArgumentException();
		}
		
		if (congestionControl instanceof RateCongestionControl) {
			return new ClientRateCongestionControl(client, (RateCongestionControl) congestionControl);
		} else if (congestionControl instanceof WindowCongestionControl) {
			return new ClientWindowCongestionControl(client, (WindowCongestionControl) congestionControl);
		} else {
			throw new IllegalStateException();
		}
	}
	
}
