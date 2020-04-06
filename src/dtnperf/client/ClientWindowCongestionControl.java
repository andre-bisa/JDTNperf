package dtnperf.client;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import dtnperf.event.BundleSentListener;
import dtnperf.header.ServerHeader;
import it.unibo.dtn.JAL.Bundle;
import it.unibo.dtn.JAL.BundleEID;
import it.unibo.dtn.JAL.exceptions.JALNotRegisteredException;
import it.unibo.dtn.JAL.exceptions.JALReceiveException;
import it.unibo.dtn.JAL.exceptions.JALReceptionInterruptedException;
import it.unibo.dtn.JAL.exceptions.JALTimeoutException;

class ClientWindowCongestionControl extends ClientCongestionControl {
	
	private final ConcurrentLinkedQueue<OnFlyID> onFlyBundles = new ConcurrentLinkedQueue<>();
	private final BundleSentListener listener = new BundleSentListener() {
		@Override
		public void bundleSentEvent(Bundle bundle) {
			OnFlyID id = new OnFlyID();
			id.source = bundle.getSource();
			id.timestampSec = bundle.getCreationTimestamp().getSeconds();
			id.timestampSeqno = bundle.getCreationTimestamp().getSequenceNumber();
			onFlyBundles.add(id);
		}
	};

	public ClientWindowCongestionControl(Client client, WindowCongestionControl congestionControl) {
		super(client, new Semaphore(congestionControl.getWindow()), congestionControl.isAckRequired());
		
		client.addBundleSentListener(this.listener);
	}

	private boolean checkServerHeader(ServerHeader serverHeader) {
		OnFlyID id = new OnFlyID();
		id.source = serverHeader.getSource();
		id.timestampSec = serverHeader.getTimestampSec();
		id.timestampSeqno = serverHeader.getTimestampSeqno();
		return this.onFlyBundles.remove(id);
	}
	
	@Override
	protected void waitForNext() throws JALNotRegisteredException, JALReceiveException {
		boolean headerOK = false;
		while (!headerOK) {
			Bundle bundle;
			try {
				bundle = this.getClient().receive();
				ServerHeader serverHeader = ServerHeader.from(bundle);
				if (this.checkServerHeader(serverHeader))
					headerOK = true;
			} 
			catch (JALTimeoutException | JALReceptionInterruptedException e) { 
				continue;
			}
		}
	}

	@Override
	protected void closing() {
		this.getClient().removeBundleSentListener(listener);
	}
	
	private class OnFlyID {
		public BundleEID source;
		public int timestampSec;
		public int timestampSeqno;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + ((source == null) ? 0 : source.hashCode());
			result = prime * result + timestampSec;
			result = prime * result + timestampSeqno;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			OnFlyID other = (OnFlyID) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			if (source == null) {
				if (other.source != null)
					return false;
			} else if (!source.equals(other.source))
				return false;
			if (timestampSec != other.timestampSec)
				return false;
			if (timestampSeqno != other.timestampSeqno)
				return false;
			return true;
		}
		private ClientWindowCongestionControl getEnclosingInstance() {
			return ClientWindowCongestionControl.this;
		}
	}

}
