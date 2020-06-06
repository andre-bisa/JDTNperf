package dtnperf.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dtnperf.header.ClientHeader;
import dtnperf.header.ClientHeaderFile;

public class FileReceiver {

	private static final Map<String, FileInfo> files = new HashMap<>();

	private static final Comparator<Couple> comparator = new Comparator<Couple>() {
		@Override
		public int compare(Couple o1, Couple o2) {
			return o1.from - o2.from;
		}
	};
	
	public static void manage(ClientHeader clientHeader, ByteBuffer buffer) throws IOException {
		if (clientHeader instanceof ClientHeaderFile) {
			ClientHeaderFile header = (ClientHeaderFile) clientHeader;

			final int offset = buffer.getInt();

			final FileInfo fileInfo;
			if (files.containsKey(header.getFilename())) {
				fileInfo = files.get(header.getFilename());
			} else {
				fileInfo = new FileInfo();
				fileInfo.filename = header.getFilename();
				fileInfo.fileSize = header.getFileDimension();

				files.put(header.getFilename(), fileInfo);
			}

			fileInfo.offsets.add(new Couple(offset, offset + buffer.remaining(), buffer));
			fileInfo.offsets.sort(comparator);
			assembleFile(fileInfo);
		}
	}

	private static boolean fileHasArrivedCompletly(FileInfo fileInfo) {
		int oldTo = 0;
		for (Couple couple : fileInfo.offsets) {
			if (couple.from != oldTo)
				return false;
			oldTo = couple.to;
		}
		
		if (oldTo > fileInfo.fileSize) {
			System.err.println("Error, the file arrived is bigger than the file size declared in header");
			files.remove(fileInfo.filename);
			return false;
		} else {		
			return (fileInfo.fileSize == oldTo);
		}
	}

	private static void assembleFile(FileInfo fileInfo) throws IOException {
		if (fileHasArrivedCompletly(fileInfo)) {
			new File(fileInfo.filename).delete();

			FileOutputStream fw = new FileOutputStream(fileInfo.filename);
			final int buf_size = 100;
			final byte[] buf = new byte[buf_size];
			int writtenData = 0;
			for (Couple couple : fileInfo.offsets) {
				while (couple.buffer.hasRemaining()) {
					writtenData = Math.min(couple.buffer.remaining(), buf.length);
					couple.buffer.get(buf, 0, writtenData);
					fw.write(buf, 0, writtenData);
				}
			}
			fw.close();

			files.remove(fileInfo.filename);
		}
	}

	private static class FileInfo {
		public String filename;
		public int fileSize;
		public List<Couple> offsets = new LinkedList<>();
	}

	private static class Couple {
		public int from;
		public int to;
		public ByteBuffer buffer;
		public Couple(int from, int to, ByteBuffer buffer) {
			this.from = from;
			this.to = to;
			this.buffer = buffer;
		}
	}

}
