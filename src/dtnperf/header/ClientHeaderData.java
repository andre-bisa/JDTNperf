package dtnperf.header;

import java.nio.ByteBuffer;

public class ClientHeaderData extends ClientHeader {

	@Override
	public ClientMode getMode() {
		return ClientMode.Data;
	}

	@Override
	void parseSpecificFields(ByteBuffer buffer) {
		// No specific fields to parse
	}

}
