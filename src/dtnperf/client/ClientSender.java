package dtnperf.client;

import java.nio.ByteBuffer;

import dtnperf.header.ClientHeader;
import it.unibo.dtn.JAL.Bundle;

class ClientSender implements Runnable {

	private final Client client;
	private final ClientCongestionControl congestionControl;
	private final ClientMode clientMode;
	private final Bundle bundle;

	public ClientSender(Client client, ClientCongestionControl congestionControl, ClientMode clientMode, Bundle bundle) {
		this.client = client;
		this.congestionControl = congestionControl;
		this.clientMode = clientMode;
		this.bundle = bundle;
	}

	@Override
	public void run() {
		try {
			while (this.client.isRunning()) {
				this.congestionControl.waitToSend();
				
				if (this.clientMode.isTerminated()) {
					this.client.stop();
				} else {
					this.setHeaderAndData(this.bundle);
					this.client.send(this.bundle);
				}
				
			}
		} catch (Exception e) {
			this.client.stop();
		}
	}
	
	private void setHeaderAndData(Bundle bundle) {
		ByteBuffer buffer = ByteBuffer.wrap(this.clientMode.getPayloadData());
		ClientHeader header = new ClientHeader(bundle.getReplyTo(), this.clientMode.getClientMode(), this.congestionControl.isAckRequired());
		header.insertHeaderInByteBuffer(buffer);
		bundle.setData(buffer.array());
	}

}
