package dtnperf.client;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import dtnperf.event.BundleReceivedListener;
import dtnperf.event.BundleSentListener;
import it.unibo.dtn.JAL.BPSocket;
import it.unibo.dtn.JAL.Bundle;
import it.unibo.dtn.JAL.BundleDeliveryOption;
import it.unibo.dtn.JAL.BundleEID;
import it.unibo.dtn.JAL.exceptions.JALNotRegisteredException;
import it.unibo.dtn.JAL.exceptions.JALNullPointerException;
import it.unibo.dtn.JAL.exceptions.JALReceiveException;
import it.unibo.dtn.JAL.exceptions.JALReceiverException;
import it.unibo.dtn.JAL.exceptions.JALReceptionInterruptedException;
import it.unibo.dtn.JAL.exceptions.JALSendException;
import it.unibo.dtn.JAL.exceptions.JALTimeoutException;

public class Client implements Runnable {
	
	private static final String DEMUXSTRING = "dtnperf/client_";

	private final int demuxNumber;
	private final String demuxString;
	
	private final ConcurrentLinkedDeque<BundleSentListener> bundleSentListeners = new ConcurrentLinkedDeque<>();
	private final ConcurrentLinkedDeque<BundleReceivedListener> bundleReceivedListeners = new ConcurrentLinkedDeque<>();
	
	private final BundleEID destination;
	private final BundleEID replyTo;
	private final CongestionControl congestionControl;
	private final Mode mode;
	private final int payloadSize;
	
	private int timeToLive = 60;

	private final AtomicBoolean running = new AtomicBoolean(false);
	private BPSocket socket;
	private LocalDateTime startTime;
	private long totalExecutionTime;
	private AtomicLong sentData = new AtomicLong(0);
	private AtomicLong sentBundles = new AtomicLong(0);;

	public Client(BundleEID destination, BundleEID replyTo, CongestionControl congestionControl, Mode mode, int payloadSizeNumber, DataUnit payloadUnit) {
		this.destination = destination;
		this.replyTo = replyTo;
		this.congestionControl = congestionControl;
		this.mode = mode;
		this.payloadSize = (int) (payloadSizeNumber * payloadUnit.getBytes());
		
		this.demuxNumber = (int) (System.currentTimeMillis() % 100000 + 10000);
		this.demuxString = DEMUXSTRING + System.currentTimeMillis();
		this.reset();
	}
	
	public Client(BundleEID dest, BundleEID replyTo, CongestionControl congestionControl, Mode mode) {
		this(dest, replyTo, congestionControl, mode, 10,  DataUnit.KILOBYTES); // Default payload: 10KiB 
	}
	
	public int getPayloadSize() {
		return this.payloadSize;
	}
	
	public int getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}
	
	public BundleEID getReplyTo() {
		return this.replyTo;
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
	
	public long getDataSent() {
		return this.sentData.get();
	}
	
	public long getSentBundles() {
		return this.sentBundles.get();
	}
	
	public long getTotalExecutionTime() {
		return this.totalExecutionTime;
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
	
	public boolean isRunning() {
		return this.running.get();
	}
	
	public Thread start() throws IllegalStateException {
		if (this.isRunning())
			throw new IllegalStateException();
		
		Thread result = new Thread(this, "JDTNperf client " + this.demuxString);
		result.setDaemon(true);
		result.start();
		return result;
	}
	
	public void stop() {
		this.running.set(false);
	}
	
	private void reset() {
		this.stop();
		this.socket = null;
		this.sentData.set(0);
		this.sentBundles.set(0);
		this.startTime = null;
		this.totalExecutionTime = 0;
	}
	
	@Override
	public void run() {
		this.reset();
		this.running.set(true);
		this.startTime = LocalDateTime.now();
		
		BundleSentListener bundleSent = new BundleSentListener() {
			@Override
			public void bundleSentEvent(Bundle bundle) {
				sentData.addAndGet(bundle.getPayload().getData().length);
				sentBundles.incrementAndGet();
			}
		};
		this.addBundleSentListener(bundleSent);
		
		try {
			this.socket = BPSocket.register(this.demuxString, this.demuxNumber);
			
			Bundle bundle = new Bundle(destination, this.getTimeToLive());
			bundle.setReplyTo(this.getReplyTo());
			bundle.addDeliveryOption(BundleDeliveryOption.DeliveryReceipt);
			
			ClientMode clientMode = ClientMode.of(this, this.mode);
			
			ClientCongestionControl clientCongestionControl = ClientCongestionControl.of(this, this.congestionControl);
			Thread congestionControlThread = new Thread(this, "JDTNperf client - congestion control");
			congestionControlThread.setDaemon(true);
			congestionControlThread.start();
			
			ClientSender sender = new ClientSender(this, clientCongestionControl, clientMode, bundle);
			Thread senderThread = new Thread(sender, Thread.currentThread().getName() + " - sender");
			senderThread.setDaemon(true);
			senderThread.start();
			
			senderThread.join();
			congestionControlThread.interrupt(); // To force exiting in case of sleep
			congestionControlThread.join();
			
			LocalDateTime stopTime = LocalDateTime.now();
			this.totalExecutionTime = ChronoUnit.MILLIS.between(this.startTime, stopTime) / 100;
			
		} catch (Exception e) {
			this.stop();
		}
		
		this.removeBundleSentListener(bundleSent);
		
	}
	
	Bundle receive() throws JALTimeoutException, JALReceptionInterruptedException, JALNotRegisteredException, JALReceiveException {
		final Bundle bundle = this.socket.receive(30);
		
		// signal
		for (BundleReceivedListener listener : this.bundleReceivedListeners) {
			listener.bundleReceivedEvent(bundle);
		}
		
		return bundle;
	}
	
	void send(Bundle bundle) throws JALReceiverException, NullPointerException, IllegalArgumentException, IllegalStateException, JALNullPointerException, JALNotRegisteredException, JALSendException {
		this.socket.send(bundle);
		
		// signal
		for (BundleSentListener listener : this.bundleSentListeners) {
			listener.bundleSentEvent(bundle);
		}
	}
	
	LocalDateTime getStartTime() {
		return startTime;
	}
	
}
