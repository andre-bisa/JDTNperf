package dtnperf.client;

import java.util.concurrent.Semaphore;

import dtnperf.client.modes.Mode;
import it.unibo.dtn.JAL.BPSocket;

public abstract class ClientCongestionControl implements Runnable {

	private Semaphore semaphore;
	private BPSocket socket;
	private Mode mode;
	private Client client;

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
