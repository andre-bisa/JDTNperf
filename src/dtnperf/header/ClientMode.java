package dtnperf.header;

public enum ClientMode {
Time(ClientMode.TIME_HEADER), Data(ClientMode.DATA_HEADER), File(ClientMode.FILE_HEADER);
	
	private static final int TIME_HEADER = 0x1;
	private static final int DATA_HEADER = 0X2;
	private static final int FILE_HEADER = 0x4;
	
	private final int val;
	
	private ClientMode(int val) {
		this.val = val;
	}
	
	static ClientMode getFromValue(int val) {
		for (ClientMode mode : ClientMode.values()) {
			if (mode.getValue() == val) {
				return mode;
			}
		}
		throw new IllegalStateException("Error, client mode not found with value=" + val);
	}
	
	int getValue() {
		return this.val;
	}
	
}
