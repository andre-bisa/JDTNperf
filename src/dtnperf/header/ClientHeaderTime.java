package dtnperf.header;

import java.nio.ByteBuffer;

public class ClientHeaderTime extends ClientHeader {

	@Override
	public ClientMode getMode() {
		return ClientMode.Time;
	}

	@Override
	void parseSpecificFields(ByteBuffer buffer) {
		// No specific fields to parse
	}

}
