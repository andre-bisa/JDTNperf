package dtnperf.client;

public class RateCongestionControl extends CongestionControl {

	private int number;
	private RateUnit rateUnit;
	
	public RateCongestionControl(int number, RateUnit rateUnit) {
		this.number = number;
		this.rateUnit = rateUnit;
	}

	public int getNumber() {
		return number;
	}

	public RateUnit getRateUnit() {
		return rateUnit;
	}

	@Override
	public boolean isAckRequired() {
		return false;
	}
	
}
