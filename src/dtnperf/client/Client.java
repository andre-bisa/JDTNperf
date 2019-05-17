package dtnperf.client;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import dtnperf.client.modes.DataUnit;
import dtnperf.client.modes.Mode;
import dtnperf.header.ClientHeader;
import it.unibo.dtn.JAL.BPSocket;
import it.unibo.dtn.JAL.Bundle;
import it.unibo.dtn.JAL.BundleEID;
import it.unibo.dtn.JAL.BundlePayload;
import it.unibo.dtn.JAL.exceptions.JALUnregisterException;

public class Client implements Runnable {
	
	private static final int DEMUXNUMBER = 10;
	private static final String DEMUXSTRING = "dtnperf/client_" + System.currentTimeMillis();
	
	private final BundleEID dest;
	private final BundleEID replyTo;
	private int payloadSize;
	
	private long totalExecutionTime;
	
	private ClientCongestionControl congestionControl;
	private final Mode mode;
	
	public Client(BundleEID dest, BundleEID replyTo, ClientCongestionControl congestionControl, Mode mode, int payloadSizeNumber, DataUnit payloadUnit) {
		this.dest = dest;
		this.replyTo = replyTo;
		this.congestionControl = congestionControl;
		this.mode = mode;
		this.mode.setClient(this);
		this.payloadSize = (int) (payloadSizeNumber * payloadUnit.getBytes());
		this.totalExecutionTime = 0L;
	}
	
	public Client(BundleEID dest, BundleEID replyTo, ClientCongestionControl congestionControl, Mode mode) {
		this(dest, replyTo, congestionControl, mode, 10,  DataUnit.KILOBYTES); // Default payload: 10KiB 
	}
	
	private static byte[] defaultByteBuffer(int size) {
		byte[] result = new byte[size];
		Arrays.fill(result, StandardCharsets.UTF_8.encode("X").array()[0]);
		return result;
	}
	
	public long getTotalExecutionTime() {
		return this.totalExecutionTime;
	}
	
	public int getPayloadSize() {
		return this.payloadSize;
	}

	public long getSentBundles() {
		return this.mode.getSentBundles();
	}

	public long getDataSent() {
		return this.mode.getDataSent();
	}
	
	public String getResultString() {
		StringBuilder result = new StringBuilder();
		
		if (this.congestionControl.isAckRequired()) { // Goodput
			result.append("Goodput");
		} else { // Throughput
			result.append("Throughtput");
		}
		
		double res = this.getDataSent() * 8d / this.totalExecutionTime * 10;
		String resUnit;
		if (res / DataUnit.GIGABYTES.getBytes() >= 1)
		{
			res /= DataUnit.GIGABYTES.getBytes();
			resUnit = "Gbit/s";
		}
		else if (res / DataUnit.MEGABYTES.getBytes() >= 1)
		{
			res /= DataUnit.MEGABYTES.getBytes();
			resUnit = "Mbit/s";
		}
		else if (res / DataUnit.KILOBYTES.getBytes() >= 1) {
			res /= DataUnit.KILOBYTES.getBytes();
			resUnit = "Kbit/s";
		}
		else
			resUnit = "bit/s";
		
		result.append(" = ");
		result.append(res);
		result.append(' ');
		result.append(resUnit);
		
		return result.toString();
	}
	
	@Override
	public void run() {
		try {
			BPSocket socket = BPSocket.register(DEMUXSTRING, DEMUXNUMBER);
			
			this.congestionControl.setSocket(socket);
			this.congestionControl.setMode(this.mode);
			this.congestionControl.setClient(this);
			
			Bundle bundle = new Bundle(this.dest);
			bundle.setReplyTo(this.replyTo);
			
			ByteBuffer buffer = ByteBuffer.wrap(defaultByteBuffer(this.payloadSize));
			ClientHeader header = new ClientHeader(bundle.getReplyTo(), this.mode.getClientMode(), this.congestionControl.isAckRequired());
			header.insertHeaderInByteBuffer(buffer);
			bundle.setPayload(BundlePayload.of(buffer.array()));
			
			ClientSender clientSender = new ClientSender(socket, bundle, this.congestionControl.getSemaphore());
			clientSender.setMode(this.mode);
			
			Thread congestionControlThread = new Thread(this.congestionControl);
			congestionControlThread.setDaemon(true);
			
			Thread clientSenderThread = new Thread(clientSender);
			clientSenderThread.setDaemon(true);
			
			final LocalDateTime start = LocalDateTime.now();
			mode.start();
			congestionControlThread.start();
			clientSenderThread.start();
			
			this.mode.waitForTerminating();
			
			final LocalDateTime stop = LocalDateTime.now();
			
			this.totalExecutionTime = ChronoUnit.MILLIS.between(start, stop) / 100;
			
			clientSenderThread.interrupt();
			congestionControlThread.interrupt();
			
			congestionControlThread.join();
			clientSenderThread.join();
			
			try {
				socket.unregister();
			} catch (JALUnregisterException e) {}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}


