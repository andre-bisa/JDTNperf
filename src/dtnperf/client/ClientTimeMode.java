package dtnperf.client;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

import it.unibo.dtn.JAL.Bundle;

class ClientTimeMode extends ClientMode {

	private final byte[] payloadCache;
	
	private final LocalDateTime stopTime;

	protected ClientTimeMode(Client client, TimeMode mode) {
		super(client);
		
		this.stopTime = this.getClient().getStartTime().plus(Duration.of(mode.getTime(), mode.getUnit()));
		
		this.payloadCache = new byte[this.getClient().getPayloadSize()];
		Arrays.fill(this.payloadCache, StandardCharsets.UTF_8.encode("X").array()[0]);
	}

	@Override
	public byte[] getPayloadData() {
		return payloadCache;
	}

	@Override
	public boolean isTerminated() {
		return LocalDateTime.now().isAfter(this.stopTime);
	}

	@Override
	protected void bundleSent(Bundle bundle) {
		// ignore, doesn't matter
	}

	@Override
	public dtnperf.header.ClientMode getClientMode() {
		return dtnperf.header.ClientMode.Time;
	}

}
