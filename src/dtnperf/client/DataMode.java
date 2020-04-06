package dtnperf.client;

public class DataMode extends Mode {
	
	private final long maxData;
	
	public DataMode (int number, DataUnit unit) {
		this.maxData = number * unit.getBytes();
	}

	public long getMaxData() {
		return maxData;
	}
	
}
