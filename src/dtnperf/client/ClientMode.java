package dtnperf.client;

import dtnperf.event.BundleSentListener;
import it.unibo.dtn.JAL.Bundle;

abstract class ClientMode {
	
	private final Client client;
	
	protected ClientMode(Client client) {
		this.client = client;
		
		client.addBundleSentListener(new BundleSentListener() {
			@Override
			public void bundleSentEvent(Bundle bundle) {
				bundleSent(bundle);
			}
		});
	}
	
	public abstract byte[] getPayloadData();
	public abstract boolean isTerminated();
	public abstract dtnperf.header.ClientMode getClientMode();
	protected abstract void bundleSent(Bundle bundle);

	public Client getClient() {
		return client;
	}
	
	public static ClientMode of(Client client, Mode mode) {
		if (mode == null || client == null) {
			throw new IllegalArgumentException();
		}
		
		if (mode instanceof TimeMode) {
			return new ClientTimeMode(client, (TimeMode) mode);
		} else if (mode instanceof DataMode) {
			return new ClientDataMode(client, (DataMode) mode);
		} else {
			throw new IllegalStateException();
		}
	}
	
}
