package dtnperf.header;

public enum AckToMonitor {
	Normal(AckToMonitor.BO_ACK_MON_NORMAL), ForceYes(AckToMonitor.BO_ACK_MON_FORCE_YES), ForceNo(AckToMonitor.BO_ACK_MON_FORCE_NO);
	
	private static final short BO_ACK_MON_NORMAL = (short)0x0000;
	private static final short BO_ACK_MON_FORCE_YES = (short)0x2000;
	private static final short BO_ACK_MON_FORCE_NO = (short)0x1000;
	
	private final short val;
	private AckToMonitor(short val) {
		this.val = val;
	}
	
	short getValue() {
		return this.val;
	}
	
}
