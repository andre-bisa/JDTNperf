package dtnperf.client;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import dtnperf.event.BundleReceivedListener;
import dtnperf.event.BundleSentListener;
import it.unibo.dtn.JAL.BPSocket;
import it.unibo.dtn.JAL.Bundle;
import it.unibo.dtn.JAL.BundleDeliveryOption;
import it.unibo.dtn.JAL.BundleEID;
import it.unibo.dtn.JAL.BundlePayloadLocation;
import it.unibo.dtn.JAL.BundlePriority;
import it.unibo.dtn.JAL.BundlePriorityCardinal;
import it.unibo.dtn.JAL.exceptions.JALNotRegisteredException;
import it.unibo.dtn.JAL.exceptions.JALNullPointerException;
import it.unibo.dtn.JAL.exceptions.JALReceiveException;
import it.unibo.dtn.JAL.exceptions.JALReceiverException;
import it.unibo.dtn.JAL.exceptions.JALReceptionInterruptedException;
import it.unibo.dtn.JAL.exceptions.JALSendException;
import it.unibo.dtn.JAL.exceptions.JALTimeoutException;

public class Client implements Runnable {
	private static final String DEMUXSTRINGPREFIX = "dtnperf/client_";

	private final int demuxNumber;
	private final String demuxString;

	private final ConcurrentLinkedDeque<BundleSentListener> bundleSentListeners = new ConcurrentLinkedDeque<>();
	private final ConcurrentLinkedDeque<BundleReceivedListener> bundleReceivedListeners = new ConcurrentLinkedDeque<>();

	// Mandatory parameters
	private final BundleEID destination;
	private final Mode mode;

	// Optional parameters
	private int timeToLive;
	private BundleEID replyTo;
	private CongestionControl congestionControl;
	private int payloadSize;
	private final Set<BundleDeliveryOption> deliveryOptions = new HashSet<>();
	private BundlePayloadLocation bundleLocation;
	private BundlePriority bundlePriority;

	// Internal state
	private final AtomicBoolean running = new AtomicBoolean(false);

	// Internal variables
	private BPSocket socket;

	// Execution variables
	private LocalDateTime startTime;
	private long totalExecutionTime;
	private AtomicLong sentData = new AtomicLong(0);
	private AtomicLong sentBundles = new AtomicLong(0);

	private void resetState() {
		this.stop();
		this.socket = null;
		this.sentData.set(0);
		this.sentBundles.set(0);
		this.startTime = null;
		this.totalExecutionTime = 0;
	}

	private void setDefaultsParams() {
		// Set defaults
		deliveryOptions.add(BundleDeliveryOption.DeliveryReceipt);
		this.setPayloadSize(10, DataUnit.KILOBYTES);
		this.setTimeToLive(60);
		this.setCongestionControl(new WindowCongestionControl(1));
		this.setReplyTo(BundleEID.NoneEndpoint);
		this.setBundleLocation(BundlePayloadLocation.File);
		this.setBundlePriority(BundlePriorityCardinal.Normal);
	}

	public Client(BundleEID destination, Mode mode) {
		this.destination = destination;
		this.mode = mode;

		this.setDefaultsParams();

		this.demuxNumber = (int) (System.currentTimeMillis() % 100000 + 10000);
		this.demuxString = DEMUXSTRINGPREFIX + System.currentTimeMillis();
		this.resetState();
	}

	public BundlePriority getBundlePriority() {
		return bundlePriority;
	}

	public void setBundlePriority(BundlePriorityCardinal bundlePriority) {
		this.bundlePriority = new BundlePriority(bundlePriority);
	}

	public BundlePayloadLocation getBundleLocation() {
		return bundleLocation;
	}

	public void setBundleLocation(BundlePayloadLocation bundleLocation) {
		this.bundleLocation = bundleLocation;
	}

	public CongestionControl getCongestionControl() {
		return congestionControl;
	}

	public void setCongestionControl(CongestionControl congestionControl) {
		this.congestionControl = congestionControl;
	}

	public void setReplyTo(BundleEID replyTo) {
		this.replyTo = replyTo;
	}

	public void setPayloadSize(int payloadSize) {
		this.setPayloadSize(payloadSize, DataUnit.KILOBYTES);
	}

	public void setPayloadSize(int payloadSize, DataUnit dataUnit) {
		this.payloadSize = (int) (payloadSize * dataUnit.getBytes());
	}

	public int getPayloadSize() {
		return this.payloadSize;
	}

	public int getTimeToLive() {
		return timeToLive;
	}

	public void addDeliveryOption(BundleDeliveryOption deliveryOption) {
		this.deliveryOptions.add(deliveryOption);
	}

	public void addDeliveryOption(Collection<BundleDeliveryOption> deliveryOptions) {
		this.deliveryOptions.addAll(deliveryOptions);
	}

	public boolean removeDeliveryOption(BundleDeliveryOption deliveryOption) {
		return this.deliveryOptions.remove(deliveryOption);
	}

	public boolean removeDeliveryOption(Collection<BundleDeliveryOption> deliveryOptions) {
		return this.deliveryOptions.removeAll(deliveryOptions);
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

	@Override
	public void run() throws IllegalStateException {
		if (this.isRunning())
			throw new IllegalStateException();

		this.resetState();
		if (this.bundleLocation.equals(BundlePayloadLocation.Memory) && this.payloadSize > (50 * DataUnit.KILOBYTES.getBytes())) {
			this.setBundleLocation(BundlePayloadLocation.File);
		}
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
			bundle.setDeliveryOptions(deliveryOptions);
			bundle.setPriority(this.getBundlePriority());

			ClientMode clientMode = ClientMode.of(this, this.mode);

			ClientCongestionControl clientCongestionControl = ClientCongestionControl.of(this, this.congestionControl);
			Thread congestionControlThread = new Thread(clientCongestionControl, "JDTNperf client - congestion control");
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
			try {
				this.socket.unregister();
			} catch (Exception e) {}
			
			this.totalExecutionTime = ChronoUnit.MILLIS.between(this.startTime, stopTime) / 100;
			
		} catch (Exception e) {
			this.stop();
			System.err.println("Error. ");
			e.printStackTrace();
		}

		this.removeBundleSentListener(bundleSent);

	}

	// Package functions

	Bundle receive() throws JALTimeoutException, JALReceptionInterruptedException, JALNotRegisteredException, JALReceiveException {
		final Bundle bundle = this.socket.receive(bundleLocation, 0);

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
