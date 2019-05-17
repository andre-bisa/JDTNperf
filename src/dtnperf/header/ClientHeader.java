package dtnperf.header;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import it.unibo.dtn.JAL.BundleEID;

public class ClientHeader {
	private static final short BO_ACK_CLIENT_YES = (short) 0x8000;
	private static final short BO_ACK_CLIENT_NO  = (short) 0x0000;
	private static final short BO_SET_EXPIRATION = (short) 0x0080;
	private static final short BO_CRC_ENABLED = (short) 0x0800;
	private static final short BO_CRC_DISABLED = (short) 0x0000;
	private static final short BO_SET_PRIORITY = (short) 0x0040;
	
	private static final ByteOrder BYTEORDER = ByteOrder.nativeOrder(); // Get the endianess from the current system
	
	private ClientMode mode;
	private boolean ackClient;
	private AckToMonitor ackToMonitor = AckToMonitor.Normal;
	private Priority ackPriority = null;
	private boolean crcEnabled = false;
	private boolean setExpiration = false;
	
	private int crc = 0; // TODO calculate
	private int ackExpiration = 60;
	private BundleEID replyTo;
	
	public ClientHeader(BundleEID replyTo, ClientMode mode, boolean ackClient) {
		this.replyTo = replyTo;
		this.mode = mode;
		this.ackClient = ackClient;
	}
	
	public ClientMode getMode() {
		return mode;
	}

	public void setMode(ClientMode mode) {
		this.mode = mode;
	}

	public boolean isAckClient() {
		return ackClient;
	}

	public void setAckClient(boolean ackClient) {
		this.ackClient = ackClient;
	}

	public AckToMonitor getAckToMonitor() {
		return ackToMonitor;
	}

	public void setAckToMonitor(AckToMonitor ackToMonitor) {
		this.ackToMonitor = ackToMonitor;
	}

	public Priority getAckPriority() {
		return ackPriority;
	}

	public void setAckPriority(Priority ackPriority) {
		this.ackPriority = ackPriority;
	}

	public boolean isCrcEnabled() {
		return crcEnabled;
	}

	public void setCrcEnabled(boolean crcEnabled) {
		this.crcEnabled = crcEnabled;
	}

	public boolean isSetExpiration() {
		return setExpiration;
	}

	public void setSetExpiration(boolean setExpiration) {
		this.setExpiration = setExpiration;
	}

	public int getCrc() {
		return crc;
	}

	public void setCrc(int crc) {
		this.crc = crc;
	}

	public int getAckExpiration() {
		return ackExpiration;
	}

	public void setAckExpiration(int ackExpiration) {
		this.ackExpiration = ackExpiration;
	}

	public void insertHeaderInByteBuffer(ByteBuffer buffer) {
		buffer.order(BYTEORDER);
		buffer.putInt(mode.getValue());
		buffer.putShort(this.optionsValue());
		buffer.putInt(this.ackExpiration);
		buffer.putInt(this.crc);
		String stringReplyTo = this.replyTo.getEndpointID();
		buffer.putShort((short) stringReplyTo.length());
		buffer.put(StandardCharsets.UTF_8.encode(stringReplyTo).array());
	}

	private short optionsValue() {
		short result = 0;
		result |= (this.ackClient ? BO_ACK_CLIENT_YES : BO_ACK_CLIENT_NO);
		result |= this.ackToMonitor.getValue();
		if (this.setExpiration)
			result |= BO_SET_EXPIRATION;
		if (this.ackPriority != null) {
			result |= BO_SET_PRIORITY;
			result |= this.ackPriority.getValue();
		}
		result |= (this.crcEnabled ? BO_CRC_ENABLED : BO_CRC_DISABLED);
		return result;
	}
	
}
