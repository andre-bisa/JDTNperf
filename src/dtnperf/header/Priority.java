package dtnperf.header;

public enum Priority {
	Bulk(Priority.BO_PRIORITY_BULK), Normal(Priority.BO_PRIORITY_NORMAL), Expedited(Priority.BO_PRIORITY_EXPEDITED), Reserved(Priority.BO_PRIORITY_RESERVED);
	
	private static final short BO_PRIORITY_BULK = (short) 0x0000;
	private static final short BO_PRIORITY_NORMAL = (short) 0x0010;
	private static final short BO_PRIORITY_EXPEDITED = (short) 0x0020;
	private static final short BO_PRIORITY_RESERVED = (short) 0x0030;
	
	private final short val;
	private Priority(short val) {
		this.val = val;
	}
	
	short getValue() {
		return this.val;
	}

	static Priority getPriorityFromValue(short options) {
		for (Priority priority : Priority.values()) {
			if ( (options & priority.getValue()) == priority.getValue() ) {
				return priority;
			}
		}
		throw new IllegalStateException("Error, the priority was not found.");
	}
	
}
