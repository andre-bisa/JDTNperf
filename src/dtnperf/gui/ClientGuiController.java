package dtnperf.gui;

import dtnperf.client.Client;
import dtnperf.client.ClientCongestionControl;
import dtnperf.client.modes.DataUnit;
import dtnperf.client.modes.Mode;
import it.unibo.dtn.JAL.BundleEID;

public class ClientGuiController {
	
	private Client client;
	private Thread clientThread;
	
	public ClientGuiController() {}
	
	public void startClient(BundleEID dest, BundleEID replyTo, ClientCongestionControl congestionControl, Mode mode, int payloadSizeNumber, DataUnit payloadUnit) {
		this.client = new Client(dest, replyTo, congestionControl, mode, payloadSizeNumber, payloadUnit);
		this.clientThread = this.client.start();
	}
	
	public void stopClient() {
		this.client.stop();
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
	
}
