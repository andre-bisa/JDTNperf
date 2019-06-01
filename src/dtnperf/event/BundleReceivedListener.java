package dtnperf.event;

import it.unibo.dtn.JAL.Bundle;

public interface BundleReceivedListener {
	
	public void bundleReceivedEvent(Bundle bundle);
	
}
