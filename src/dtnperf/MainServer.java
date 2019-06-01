package dtnperf;

import dtnperf.event.BundleReceivedListener;
import dtnperf.event.BundleSentListener;
import dtnperf.server.Server;
import it.unibo.dtn.JAL.Bundle;

public class MainServer {

	public static void main(String[] args) throws Exception {
		
		Server server = new Server();
		server.addBundleReceivedListener(new BundleReceivedListener() {
			@Override
			public void bundleReceivedEvent(Bundle bundle) {
				System.out.println("Received bundle size = " + bundle.getData().length + " bytes");
			}
		});
		server.addBundleSentListener(new BundleSentListener() {
			@Override
			public void bundleSentEvent(Bundle bundleSent) {
				System.out.println("Sent bundle ack to " + bundleSent.getDestination());
			}
		});
		Thread serverThread = server.start();
		
		serverThread.join();
		
	}

}
