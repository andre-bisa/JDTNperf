package dtnperf.header;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ClientHeaderFile extends ClientHeader {
	private int expiration;
	private String filename;
	private int file_dim;
	
	public int getExpiration() {
		return expiration;
	}

	public void setExpiration(int expiration) {
		this.expiration = expiration;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int getFileDimension() {
		return file_dim;
	}

	public void setFileDimension(int file_dim) {
		this.file_dim = file_dim;
	}

	@Override
	public ClientMode getMode() {
		return ClientMode.File;
	}

	@Override
	void parseSpecificFields(ByteBuffer buffer) {
		this.setExpiration(buffer.getInt());
		final String filename;
		{
			short filename_len = buffer.getShort();
			byte[] filename_arr = new byte[filename_len];
			for (int i = 0; i < filename_len; i++)
				filename_arr[i] = buffer.get();
			filename = new String(filename_arr, StandardCharsets.UTF_8);
		}
		this.setFilename(filename);
		this.setFileDimension(buffer.getInt());
	}
	
}
