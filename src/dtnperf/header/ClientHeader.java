package dtnperf.header;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import it.unibo.dtn.JAL.BundleEID;

public abstract class ClientHeader {
	private static final short BO_ACK_CLIENT_YES = (short) 0x8000;
	private static final short BO_ACK_CLIENT_NO  = (short) 0x0000;
	private static final short BO_SET_EXPIRATION = (short) 0x0080;
	private static final short BO_CRC_ENABLED = (short) 0x0800;
	private static final short BO_CRC_DISABLED = (short) 0x0000;
	private static final short BO_SET_PRIORITY = (short) 0x0040;

	private static final ByteOrder BYTEORDER = ByteOrder.nativeOrder(); // Get the endianess from the current system

	private boolean ackClient;
	private AckToMonitor ackToMonitor = AckToMonitor.Normal;
	private Priority ackPriority = null;
	private boolean crcEnabled = false;
	private boolean setExpiration = false;

	private int crc = 0; // TODO calculate
	private int ackExpiration = 60;
	private  BundleEID replyTo;

	protected ClientHeader() {}

	/***** ABSTRACT METHODS *****/
	public abstract ClientMode getMode();
	abstract void parseSpecificFields(ByteBuffer buffer);
	
	public boolean isAckClient() {
		return ackClient;
	}

	public void setReplyTo(BundleEID replyTo) {
		this.replyTo = replyTo;
	}
	
	public BundleEID getReplyTo() {
		return this.replyTo;
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
		buffer.putInt(this.getMode().getValue());
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
	
	/*****  STATIC METHODS *****/
	
	public static ClientHeader of(ClientMode mode) throws IllegalArgumentException {
		switch (mode) {
		case Data:
			return new ClientHeaderData();
		case File:
			return new ClientHeaderFile();
		case Time:
			return new ClientHeaderTime();

		default:
			throw new IllegalArgumentException();
		}
	}
	
	public static ClientHeader from(ByteBuffer buffer) throws BufferUnderflowException {
		final ClientHeader result;
		buffer.order(BYTEORDER);
		ClientMode mode = ClientMode.getFromValue(buffer.getInt());
		
		result = ClientHeader.of(mode);
		
		insertCommonFieldsInClientHeader(result, buffer);
		
		result.parseSpecificFields(buffer);
		
		return result;
	}
	
	private static void insertCommonFieldsInClientHeader(ClientHeader header, ByteBuffer buffer) {
		short options = buffer.getShort();
		insertOptionInClientHeader(header, options);

		header.setAckExpiration(buffer.getInt());
		header.setCrc(buffer.getInt());
		final BundleEID replyTo;
		{
			short replytoSize = buffer.getShort();
			byte[] replyto = new byte[replytoSize];
			for (int i = 0; i < replytoSize; i++)
				replyto[i] = buffer.get();
			replyTo = BundleEID.of(new String(replyto, StandardCharsets.UTF_8));
		}
		header.setReplyTo(replyTo);
	}

	private static void insertOptionInClientHeader(ClientHeader result, short options) {
		result.ackClient = (options & BO_ACK_CLIENT_YES) == BO_ACK_CLIENT_YES;
		result.ackToMonitor = AckToMonitor.getAckToMonitorFromValue(options);
		result.setExpiration = (options & BO_SET_EXPIRATION) == BO_SET_EXPIRATION;
		if ((options & BO_SET_PRIORITY) == BO_SET_PRIORITY) {
			result.ackPriority = Priority.getPriorityFromValue(options);
		} else {
			result.ackPriority = null;
		}
		result.crcEnabled = (options & BO_CRC_ENABLED) == BO_CRC_ENABLED;
	}

}
