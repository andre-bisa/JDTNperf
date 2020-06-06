package dtnperf.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import dtnperf.header.ClientHeader;
import dtnperf.header.ClientHeaderFile;
import it.unibo.dtn.JAL.Bundle;

public class ClientFileMode extends ClientMode {

	private final FileSender fileSender;
	private final String path;
	
	public ClientFileMode(Client client, FileMode mode) throws FileNotFoundException {
		super(client);
		this.fileSender = new FileSender(mode.getPath());
		this.path = new File(mode.getPath()).getName();
	}
	
	@Override
	public void insertPayloadInByteBuffer(ByteBuffer buffer) {
		buffer.putInt(this.fileSender.offset);
		while (buffer.hasRemaining() && this.fileSender.hasNext()) {
			buffer.put(this.fileSender.next());
		}
		this.fileSender.offset++;
	}

	@Override
	public boolean isTerminated() {
		return ! this.fileSender.hasNext();
	}
	
	@Override
	public ClientHeader getClientHeader() {
		ClientHeaderFile result = new ClientHeaderFile();
		result.setFilename(this.path);
		result.setFileDimension((int) new File(this.path).length());
		result.setExpiration(this.getClient().getTimeToLive()); // XXX Check this is correct
		return result;
	}

	@Override
	protected void bundleSent(Bundle bundle) {
		// Nothing to do
	}

	private class FileSender {
		public int offset = 0;
		private final FileInputStream file;
		public FileSender(String path) throws FileNotFoundException {
			this.file = new FileInputStream(path);
		}
		public boolean hasNext() {
			try {
				return file.available() > 0;
			} catch (IOException e) {
				return false;
			}
		}
		public Byte next() {
			try {
				return (byte) file.read();
			} catch (IOException e) {
				return null;
			}
		}
	}
}
