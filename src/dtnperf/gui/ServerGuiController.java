package dtnperf.gui;

import dtnperf.server.Server;

public class ServerGuiController {
	private Server server;
	private Thread serverThread;
	
	public ServerGuiController() {}
	
	public void startServer() {
		this.server = new Server();
		this.serverThread = this.server.start();
	}
	
	public void stopServer() {
		this.server.stop();
	}
	
	public void waitForTerminating() {
		try {
			this.serverThread.join();
		} catch (InterruptedException e) {}
	}
	
	public boolean isServerRunning() {
		if (this.serverThread == null)
			return false;
		return this.serverThread.isAlive();
	}
	
}
