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
	private Client client;

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

	void setClient(Client client) {
		this.client = client;
	}

	@Override
	public void run() {
		try {
			while (!this.mode.isTerminated() && this.client.isRunning()) {
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

		} catch(Exception e) {
			
		}
	}

}
