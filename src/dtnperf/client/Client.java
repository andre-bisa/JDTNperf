package dtnperf.client;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedDeque;

import dtnperf.event.BundleReceivedListener;
import dtnperf.event.BundleSentListener;
import dtnperf.header.ClientHeader;
import it.unibo.dtn.JAL.BPSocket;
import it.unibo.dtn.JAL.Bundle;
import it.unibo.dtn.JAL.BundleDeliveryOption;
import it.unibo.dtn.JAL.BundleEID;
import it.unibo.dtn.JAL.exceptions.JALUnregisterException;

public class Client implements Runnable {
	
	private static final String DEMUXSTRING = "dtnperf/client_";
	
	private ConcurrentLinkedDeque<BundleSentListener> bundleSentListeners = new ConcurrentLinkedDeque<>();
	private ConcurrentLinkedDeque<BundleReceivedListener> bundleReceivedListeners = new ConcurrentLinkedDeque<>();
	private Boolean running = false;
	private BundleEID localEID;
	
	private final BundleEID dest;
	private final BundleEID replyTo;
	private int payloadSize;
	
	private long totalExecutionTime;
	
	private ClientCongestionControl congestionControl;
	private final Mode mode;
	
	private int demuxNumber;
	private String demuxString;
	
	public Client(BundleEID dest, BundleEID replyTo, ClientCongestionControl congestionControl, Mode mode, int payloadSizeNumber, DataUnit payloadUnit) {
		this.dest = dest;
		this.replyTo = replyTo;
		this.congestionControl = congestionControl;
		this.mode = mode;
		this.mode.setClient(this);
		this.payloadSize = (int) (payloadSizeNumber * payloadUnit.getBytes());
		this.totalExecutionTime = 0L;
		
		this.bundleSentListeners.add(this.mode.getBundleSentListener()); // Add mode listener to the listeners
		
		this.demuxNumber = (int) (System.currentTimeMillis() % 100000 + 10000);
		this.demuxString = DEMUXSTRING + System.currentTimeMillis();
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
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("JDTNperf client ");
		final boolean running;
		synchronized (this.running) {
			running = this.running;
		}
		if (!running) {
			result.append("not running");
		} else {
			result.append("running on ");
			result.append(this.localEID);
		}
		return result.toString();
	}
	
	public void addBundleSentListener(BundleSentListener listener) {
		this.bundleSentListeners.add(listener);
	}
	
	public boolean removeBundleSentListener(BundleSentListener listener) {
		return this.bundleSentListeners.remove(listener);
	}
	
	public void addBundleReceivedListener(BundleReceivedListener listener) {
		this.bundleReceivedListeners.add(listener);
	}
	
	public boolean removeBundleReceivedListener(BundleReceivedListener listener) {
		return this.bundleReceivedListeners.remove(listener);
	}
	
	public boolean isRunning() {
		synchronized (this.running) {
			return this.running;
		}
	}
	
	public void stop() {
		synchronized (this.running) {
			this.running = false;
			this.mode.forceTermination();
		}
	}
	
	public Thread start() {
		Thread result = new Thread(this, "JDTNperf client");
		result.setDaemon(true);
		result.start();
		return result;
	}
	
	@Override
	public void run() {
		synchronized (this.running) {
			if (this.running)
				return;
			this.running = true;
		}
		try {
			BPSocket socket = BPSocket.register(this.demuxString, this.demuxNumber);
			this.localEID = socket.getLocalEID();
			
			this.congestionControl.setSocket(socket);
			this.congestionControl.setMode(this.mode);
			this.congestionControl.setClient(this);
			
			Bundle bundle = new Bundle(this.dest);
			bundle.setReplyTo(this.replyTo);
			bundle.addDeliveryOption(BundleDeliveryOption.DeliveryReceipt);
			
			ByteBuffer buffer = ByteBuffer.wrap(defaultByteBuffer(this.payloadSize));
			ClientHeader header = new ClientHeader(bundle.getReplyTo(), this.mode.getClientMode(), this.congestionControl.isAckRequired());
			header.insertHeaderInByteBuffer(buffer);
			bundle.setData(buffer.array());
			
			ClientSender clientSender = new ClientSender(socket, bundle, this.congestionControl.getSemaphore());
			clientSender.setMode(this.mode);
			clientSender.setClient(this);
			
			clientSender.setBundleSentListeners(this.bundleSentListeners);
			congestionControl.setBundleReceivedListeners(this.bundleReceivedListeners);
			
			Thread congestionControlThread = new Thread(this.congestionControl, "JDTNperf congestion control");
			congestionControlThread.setDaemon(true);
			
			Thread clientSenderThread = new Thread(clientSender, "JDTNperf client sender");
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
			this.stop();
			e.printStackTrace();
		}
	}
	
}


