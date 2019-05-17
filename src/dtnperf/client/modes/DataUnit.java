package dtnperf.client.modes;

public enum DataUnit {
BYTES, KIBIBYTES, KILOBYTES, MEBIBYTES, MEGABYTES, GIBIBYTES, GIGABYTES, TEBIBYTES, TERABYTES, PEBIBYTES, PETABYTES;
	
	private static final long B = 1L;
	private static final long KB = B * 1000L;
	private static final long MB = KB * 1000L;
	private static final long GB = MB * 1000L;
	private static final long TB = GB * 1000L;
	private static final long PB = TB * 1000L;
	
	private static final long KiB = B * 1024L;
	private static final long MiB = KiB * 1024L;
	private static final long GiB = MiB * 1024L;
	private static final long TiB = GiB * 1024L;
	private static final long PiB = TiB * 1024L;
	
	public long getBytes() {
		switch (this) {
			case BYTES: return B;
			case KIBIBYTES: return KiB;
			case KILOBYTES: return KB;
			case MEBIBYTES: return MiB;
			case MEGABYTES: return MB;
			case GIBIBYTES: return GiB;
			case GIGABYTES: return GB;
			case TEBIBYTES: return TiB;
			case TERABYTES: return TB;
			case PEBIBYTES: return PiB;
			case PETABYTES: return PB;
			default:
				throw new IllegalStateException("DataUnit not found");
		}
	}
	
}
