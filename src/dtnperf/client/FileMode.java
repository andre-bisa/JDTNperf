package dtnperf.client;

public class FileMode extends Mode {
	
	private final String path;
	
	public FileMode(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return this.path;
	}
	
}
