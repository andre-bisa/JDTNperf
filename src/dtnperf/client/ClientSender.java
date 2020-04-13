package dtnperf.client;

import java.nio.ByteBuffer;

import dtnperf.header.AckToMonitor;
import dtnperf.header.ClientHeader;
import dtnperf.header.Priority;
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
		
		ClientHeader header = ClientHeader.of(this.clientMode.getClientMode());
		header.setAckClient(this.congestionControl.isAckRequired());
		header.setReplyTo(bundle.getReplyTo());
		header.setAckExpiration(60);
		header.setAckPriority(Priority.Normal);
		header.setAckToMonitor(AckToMonitor.Normal);
		header.setCrcEnabled(false);
		header.setSetExpiration(false);
		
		header.insertHeaderInByteBuffer(buffer);
		bundle.setData(buffer.array());
	}

}
