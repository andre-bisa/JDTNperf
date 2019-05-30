package dtnperf.header;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import it.unibo.dtn.JAL.Bundle;
import it.unibo.dtn.JAL.BundleEID;
import it.unibo.dtn.JAL.BundleTimestamp;

public class ServerHeader {
	private static final int DSA_HEADER = 0x8;
	
	private static final ByteOrder BYTEORDER = ByteOrder.nativeOrder(); // Get the endianess from the current system
	
	private int header = DSA_HEADER;
	private BundleEID source;
	private int timestampSec;
	private int timestampSeqno;
	
	private ServerHeader() {}
	
	public ServerHeader(BundleEID source, BundleTimestamp bundleTimestamp) {
		this.source = source;
		this.timestampSec = bundleTimestamp.getSeconds();
		this.timestampSeqno = bundleTimestamp.getSequenceNumber();
	}

	public byte[] getHeaderBytes() {
		byte[] sourceEndpointData = this.source.getEndpointID().getBytes();
		short sourceSize = (short) sourceEndpointData.length;
		
		byte[] result = new byte[(Integer.SIZE * 3 + Short.SIZE + Byte.SIZE * sourceSize) / 8];
		ByteBuffer buffer = ByteBuffer.wrap(result);
		
		buffer.order(BYTEORDER);
		buffer.putInt(this.header);
		buffer.putShort(sourceSize);
		buffer.put(sourceEndpointData);
		buffer.putInt(this.timestampSec);
		buffer.putInt(this.timestampSeqno);
		
		return buffer.array();
	}
	
	public static ServerHeader from(Bundle bundle) throws BufferUnderflowException {
		ServerHeader result = new ServerHeader();
		ByteBuffer buffer = ByteBuffer.wrap(bundle.getData());
		buffer.order(BYTEORDER);
		result.header = buffer.getInt();
		short sourceSize = buffer.getShort();
		byte[] source = new byte[sourceSize];
		for (int i = 0; i < sourceSize; i++)
			source[i] = buffer.get();
		result.source = BundleEID.of(new String(source, StandardCharsets.UTF_8));
		result.timestampSec = buffer.getInt();
		result.timestampSeqno = buffer.getInt();
		return result;
	}

	public int getHeader() {
		return header;
	}

	public BundleEID getSource() {
		return source;
	}

	public int getTimestampSec() {
		return timestampSec;
	}

	public int getTimestampSeqno() {
		return timestampSeqno;
	}
	
}
