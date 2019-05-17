package dtnperf.client.modes;

import java.util.concurrent.Semaphore;

import dtnperf.client.Client;
import dtnperf.header.ClientMode;

public abstract class Mode {
	
	private long sentBundles;
	private long dataSent;
	
	private Client client;
	private boolean forceTermination;
	
	private Semaphore semaphore;
	
	protected Mode() {
		this.sentBundles = 0L;
		this.dataSent = 0L;
		this.forceTermination = false;
		this.semaphore = new Semaphore(0);
	}
	
	public boolean isTerminated() {
		if (this.forceTermination || this.isModeTerminated()) {
			this.semaphore.release();
			return true;
		} else {
			return false;
		}
	}
	
	public void waitForTerminating() {
		this.semaphore.acquireUninterruptibly();
		this.semaphore.drainPermits();
	}
	
	public abstract void start();
	
	protected abstract boolean isModeTerminated();
	
	protected abstract void sentBundle();
	
	public abstract ClientMode getClientMode();
	
	public void forceTermination() {
		this.forceTermination = true;
	}
	
	protected Client getClient() {
		return this.client;
	}
	
	public void setClient(Client client) {
		this.client = client;
	}
	
	public void bundleSent() {
		this.sentBundles++;
		this.dataSent += this.client.getPayloadSize();
		this.sentBundle();
	}
	
	public long getSentBundles() {
		return this.sentBundles;
	}

	public long getDataSent() {
		return this.dataSent;
	}
	
}
