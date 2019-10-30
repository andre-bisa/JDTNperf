package dtnperf.client;

import dtnperf.header.ClientMode;

public class DataMode extends Mode {

	private final long maxData;
	
	public DataMode (int number, DataUnit unit) {
		this.maxData = number * unit.getBytes();
	}
	
	@Override
	public boolean isModeTerminated() {
		final boolean result = (this.getDataSent() + this.getClient().getPayloadSize() > this.maxData); // I can send one more bundle
		if (result) {
			super.setStop();
		}
		return result;
	}

	@Override
	protected void sentBundle() {
	}

	@Override
	public ClientMode getClientMode() {
		return ClientMode.Data;
	}

	@Override
	public void _start() {
	}

}
