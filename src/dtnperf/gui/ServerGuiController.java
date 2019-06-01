package dtnperf.gui;

import dtnperf.server.Server;

public class ServerGuiController {
	private Server server;
	private Thread serverThread;
	
	public ServerGuiController() {}
	
	public Server startServer() {
		this.server = new Server();
		this.serverThread = this.server.start();
		return this.server;
	}
	
	public void stopServer() {
		this.server.stop();
	}
	
	public Server getServer() {
		return this.server;
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
