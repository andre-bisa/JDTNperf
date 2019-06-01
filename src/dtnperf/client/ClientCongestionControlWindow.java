package dtnperf.client;

import java.util.concurrent.Semaphore;

import dtnperf.header.ServerHeader;
import it.unibo.dtn.JAL.Bundle;
import it.unibo.dtn.JAL.exceptions.JALReceptionInterruptedException;
import it.unibo.dtn.JAL.exceptions.JALTimeoutException;

public class ClientCongestionControlWindow extends ClientCongestionControl {

	public ClientCongestionControlWindow(int window) {
		super(new Semaphore(window));
	}

	/**
	 * Checks if the serverHeader is valid
	 * @param serverHeader the ServerHeader
	 * @return If it is valid or not
	 */
	private boolean checkServerHeader(ServerHeader serverHeader) {
		return true;
	}
	
	@Override
	protected boolean waitForNext() {
		boolean headerOK = false;
		while (!headerOK) {
			Bundle bundle;
			try {
				bundle = this.getSocket().receive();
				this.signal(bundle);
				ServerHeader serverHeader = ServerHeader.from(bundle);
				if (this.checkServerHeader(serverHeader))
					headerOK = true;
			} catch (JALReceptionInterruptedException e) {continue;}
			catch (JALTimeoutException e) { return false; }
			catch (Exception e) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isAckRequired() {
		return true;
	}

}
