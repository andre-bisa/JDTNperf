package dtnperf.client;

import java.util.concurrent.Semaphore;

import dtnperf.client.modes.Mode;
import it.unibo.dtn.JAL.BPSocket;
import it.unibo.dtn.JAL.Bundle;

class ClientSender implements Runnable {

	private Bundle bundle;
	private Semaphore semaphore;
	private BPSocket socket;

	private Mode mode;

	public ClientSender(BPSocket socket, Bundle bundle, Semaphore semaphore) {
		this.socket = socket;
		this.bundle = bundle;
		this.semaphore = semaphore;
	}

	protected Mode getMode() {
		return this.mode;
	}

	void setMode(Mode mode) {
		this.mode = mode;
	}

	@Override
	public void run() {
		try {
			while (!this.mode.isTerminated()) {
				this.semaphore.acquire();
				if (this.mode.isTerminated()) continue;
				try {
					this.socket.send(this.bundle);
					this.mode.bundleSent();
				} catch (Exception e) {
					continue;
				}
			}
		} catch (InterruptedException e) {

		}
	}

}
