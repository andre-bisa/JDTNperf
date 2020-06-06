package dtnperf.client;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

import dtnperf.event.BundleSentListener;
import dtnperf.header.ClientHeader;
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
	
	//public abstract byte[] getPayloadData(int headerSize);
	public abstract void insertPayloadInByteBuffer(ByteBuffer buffer);
	public abstract boolean isTerminated();
	public abstract ClientHeader getClientHeader();
	protected abstract void bundleSent(Bundle bundle);

	public Client getClient() {
		return client;
	}
	
	public static ClientMode of(Client client, Mode mode) throws FileNotFoundException {
		if (mode == null || client == null) {
			throw new IllegalArgumentException();
		}
		
		if (mode instanceof TimeMode) {
			return new ClientTimeMode(client, (TimeMode) mode);
		} else if (mode instanceof DataMode) {
			return new ClientDataMode(client, (DataMode) mode);
		} else if (mode instanceof FileMode) {
			return new ClientFileMode(client, (FileMode) mode);
		} else {
			throw new IllegalStateException();
		}
	}
	
}
