package dtnperf;

import dtnperf.server.Server;

public class MainServer {

	public static void main(String[] args) throws Exception {
		
		Server server = new Server();
		Thread serverThread = new Thread(server);
		
		serverThread.start();
		serverThread.join();
		
	}

}
