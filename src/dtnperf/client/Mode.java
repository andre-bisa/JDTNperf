package dtnperf.client;

import java.time.LocalDateTime;
import java.util.concurrent.Semaphore;

import dtnperf.event.BundleSentListener;
import dtnperf.header.ClientMode;
import it.unibo.dtn.JAL.Bundle;

public abstract class Mode {

	private long sentBundles;
	private long dataSent;

	private Client client;
	private boolean forceTermination;
	
	private LocalDateTime start;
	private LocalDateTime stop;

	private Semaphore semaphore;
	private BundleSentListener bundleSentListener = new BundleSentListener() {
		@Override
		public void bundleSentEvent(Bundle bundleSent) {
			bundleSent();
		}
	};;

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

	public void start() {
		this.start = LocalDateTime.now();
		this._start();
	}
	
	protected void setStop() {
		this.stop = LocalDateTime.now();
	}
	
	protected abstract void _start();

	protected abstract boolean isModeTerminated();

	protected abstract void sentBundle();

	public abstract ClientMode getClientMode();

	public void forceTermination() {
		this.forceTermination = true;
	}

	protected Client getClient() {
		return this.client;
	}

	void setClient(Client client) {
		this.client = client;
	}

	private void bundleSent() {
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

	BundleSentListener getBundleSentListener() {
		return this.bundleSentListener ;
	}

	public LocalDateTime getStartTime() {
		return this.start;
	}
	
	public LocalDateTime getStopTime() {
		return this.stop;
	}

}
