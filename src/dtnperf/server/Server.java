package dtnperf.server;

import dtnperf.header.ClientHeader;
import dtnperf.header.ServerHeader;
import it.unibo.dtn.JAL.BPSocket;
import it.unibo.dtn.JAL.Bundle;
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
	
	@Override
	public void run() {
		BPSocket socket = null;
		try {
			socket = BPSocket.register(DEMUXSTRING, DEMUXNUMBER);
		} catch (JALRegisterException e) {
			System.err.println("Error on registering DTN socket.");
			System.exit(1);
		}
		while (true) {
			Bundle bundle = null;
			try {
				bundle = socket.receive();
			} catch (JALTimeoutException e) {
				continue;
			} catch (JALReceptionInterruptedException e) {
				continue;
			} catch (JALNotRegisteredException | JALReceiveException e) {
				System.err.println("Error on receiving bundle.");
				System.exit(2);
			}
			
			byte[] data = bundle.getData();
			
			ClientHeader clientHeader = ClientHeader.from(data);
			
			System.out.println("Received bundle size = " + data.length + " bytes");
			
			if (clientHeader.isAckClient()) {
				ServerHeader serverHeader = new ServerHeader(bundle.getSource(), bundle.getCreationTimestamp());
				Bundle replyBundle = new Bundle(bundle.getSource());
				
				replyBundle.setData(serverHeader.getHeaderBytes());
				try {
					socket.send(replyBundle);
				} catch (NullPointerException | IllegalArgumentException | IllegalStateException
						| JALNullPointerException | JALNotRegisteredException | JALSendException e) {
					System.err.println("Error on sending bundle ack to " + replyBundle.getDestination());
					System.exit(3);
				}
				
				System.out.println("Sent bundle ack to " + replyBundle.getDestination());
			}
		}
	}

}
