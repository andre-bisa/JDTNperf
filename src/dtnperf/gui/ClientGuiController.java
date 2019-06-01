package dtnperf.gui;

import dtnperf.client.Client;
import dtnperf.client.ClientCongestionControl;
import dtnperf.client.DataUnit;
import dtnperf.client.Mode;
import it.unibo.dtn.JAL.BundleEID;

public class ClientGuiController {

	private Client client;
	private Thread clientThread;

	public ClientGuiController() {}

	public Client startClient(BundleEID dest, BundleEID replyTo, ClientCongestionControl congestionControl, Mode mode, int payloadSizeNumber, DataUnit payloadUnit) {
		this.client = new Client(dest, replyTo, congestionControl, mode, payloadSizeNumber, payloadUnit);
		this.clientThread = this.client.start();
		return this.client;
	}

	public void stopClient() {
		this.client.stop();
	}

	public Client getClient() {
		return this.client;
	}

	public void waitForTerminating() {
		try {
			this.clientThread.join();
		} catch (InterruptedException e) {}
	}

	public boolean isClientRuning() {
		if (this.clientThread == null)
			return false;
		return this.clientThread.isAlive();
	}

	private String cacheToString = null;
	@Override
	public String toString() {
		if (this.cacheToString == null) {
			if (this.client != null) {
				StringBuilder result = new StringBuilder();
				result.append(this.client.toString());
				this.cacheToString = result.toString();
			} else
				this.cacheToString = "ClientGuiController without a client!";
		}
		return this.cacheToString;
	}

}
