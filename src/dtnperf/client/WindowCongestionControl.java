package dtnperf.client;

public class WindowCongestionControl extends CongestionControl {
	
	private int window;
	
	public WindowCongestionControl(int window) {
		this.window = window;
	}

	public int getWindow() {
		return window;
	}

	@Override
	public boolean isAckRequired() {
		return true;
	}
	
}
