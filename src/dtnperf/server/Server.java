package dtnperf.server;

import java.util.concurrent.ConcurrentLinkedDeque;

import dtnperf.event.BundleReceivedListener;
import dtnperf.event.BundleSentListener;
import dtnperf.header.ClientHeader;
import dtnperf.header.ServerHeader;
import it.unibo.dtn.JAL.BPSocket;
import it.unibo.dtn.JAL.Bundle;
import it.unibo.dtn.JAL.BundleEID;
import it.unibo.dtn.JAL.exceptions.JALNotRegisteredException;
import it.unibo.dtn.JAL.exceptions.JALNullPointerException;
import it.unibo.dtn.JAL.exceptions.JALReceiveException;
import it.unibo.dtn.JAL.exceptions.JALReceptionInterruptedException;
import it.unibo.dtn.JAL.exceptions.JALRegisterException;
import it.unibo.dtn.JAL.exceptions.JALSendException;
import it.unibo.dtn.JAL.exceptions.JALTimeoutException;

public class Server implements Runnable {

	private static final int DEMUXNUMBER = 2000;
	private static final String DEMUXSTRING = "dtnperf/server";
	
	private ConcurrentLinkedDeque<BundleSentListener> bundleSentListeners = new ConcurrentLinkedDeque<>();
	private ConcurrentLinkedDeque<BundleReceivedListener> bundleReceivedListeners = new ConcurrentLinkedDeque<>();
	
	private String demuxString;
	private int demuxNumber;
	
	private Boolean running = false;
	private BundleEID localEID;
	
	public Server(String demuxString, int demuxNumber) {
		this.demuxNumber = demuxNumber;
		this.demuxString = demuxString;
	}
	
	public Server() {
		this(DEMUXSTRING, DEMUXNUMBER);
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
		}
	}
	
	public Thread start() {
		Thread result = new Thread(this, "JDTNperf server");
		result.setDaemon(true);
		result.start();
		return result;
	}
	
	public BundleEID getLocalEID() {
		return this.localEID;
	}
	
	@Override
	public void run() {
		synchronized (this.running) {
			if (this.running)
				return;
			this.running = true;
		}
		
		BPSocket socket = null;
		try {
			socket = BPSocket.register(this.demuxString, this.demuxNumber);
		} catch (JALRegisterException e) {
			System.err.println("Error on registering DTN socket.");
			System.exit(1);
		}
		
		this.localEID = socket.getLocalEID();
		
		while (this.isRunning()) {
			Bundle bundle = null;
			try {
				bundle = socket.receive();
				this.signalReceived(bundle);
			} catch (JALTimeoutException | JALReceptionInterruptedException e) {
				continue;
			} catch (JALNotRegisteredException | JALReceiveException e) {
				System.err.println("Error on receiving bundle.");
				System.exit(2);
			}
			
			byte[] data = bundle.getData();
			
			ClientHeader clientHeader = ClientHeader.from(data);
			
			if (clientHeader.isAckClient()) {
				ServerHeader serverHeader = new ServerHeader(bundle.getSource(), bundle.getCreationTimestamp());
				Bundle replyBundle = new Bundle(bundle.getSource());
				
				replyBundle.setData(serverHeader.getHeaderBytes());
				try {
					socket.send(replyBundle);
					this.signalSent(replyBundle);
				} catch (NullPointerException | IllegalArgumentException | IllegalStateException
						| JALNullPointerException | JALNotRegisteredException | JALSendException e) {
					System.err.println("Error on sending bundle ack to " + replyBundle.getDestination());
					System.exit(3);
				}
				
			}
		} // while
		
		this.localEID = null;
		
	}

	private void signalSent(Bundle replyBundle) {
		for (BundleSentListener listener : this.bundleSentListeners) {
			listener.bundleSentEvent(replyBundle);
		}
	}

	private void signalReceived(Bundle bundle) {
		for (BundleReceivedListener listener : this.bundleReceivedListeners) {
			listener.bundleReceivedEvent(bundle);
		}
	}

}
