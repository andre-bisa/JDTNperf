package dtnperf.client;

import java.util.Collection;
import java.util.concurrent.Semaphore;

import dtnperf.event.BundleReceivedListener;
import it.unibo.dtn.JAL.BPSocket;
import it.unibo.dtn.JAL.Bundle;

public abstract class ClientCongestionControl implements Runnable {

	private Semaphore semaphore;
	private BPSocket socket;
	private Mode mode;
	private Client client;
	
	private Collection<BundleReceivedListener> listeners;

	protected ClientCongestionControl(Semaphore semaphore) {
		this.semaphore = semaphore;
	}

	protected Mode getMode() {
		return this.mode;
	}

	void setMode(Mode mode) {
		this.mode = mode;
	}

	void setClient(Client client) {
		this.client = client;
	}

	protected Client getClient() {
		return this.client;
	}

	public Semaphore getSemaphore() {
		return this.semaphore;
	}

	void setSocket(BPSocket socket) {
		this.socket = socket;
	}
	
	void setBundleReceivedListeners(Collection<BundleReceivedListener> listeners) {
		this.listeners = listeners;
	}
	
	protected final void signal(Bundle bundle) {
		for (BundleReceivedListener listener : this.listeners) {
			listener.bundleReceivedEvent(bundle);
		}
	}

	protected BPSocket getSocket() {
		return this.socket;
	}

	public abstract boolean isAckRequired();

	protected abstract boolean waitForNext();

	@Override
	public void run() {
		try {
			while (!this.mode.isTerminated() && this.client.isRunning()) {
				if (!this.waitForNext())
					continue;
				this.semaphore.release();
			}
		} catch(Exception e) {

		}
	}

}
