package app.media;

import app.tools.LOG;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * class to get the creation date of a MP4/MOV file, reading 'mvhd' atom.
 */
public class MP4 {

	private static final String TT = "MP4.";
	// Epoch QuickTime / MP4 : 1st January 1904 00:00:00 UTC en secondes
	private static final long SECONDS_FROM_1904_TO_1970 = 2082844800L;

	/**
	 * Get the creation date of the video (format yyyyMMdd_HHmmss). If atom is absent or
	 * not readable, change to file system date
	 *
	 * @param file The video file(.mp4, .mov)
	 * @return The formatted date or null if file doesn't exist.
	 */
	public static String getDate(File file) {
		if (file == null || !file.exists() || file.isDirectory()) {
			return null;
		}
		try {
			long epochSeconds = readCreationTimeFromMvhd(file);
			if (epochSeconds > 0) {
				long unixEpochSeconds = epochSeconds - SECONDS_FROM_1904_TO_1970;
				if (unixEpochSeconds > 0) {
					LocalDateTime ldt = Instant.ofEpochSecond(unixEpochSeconds)
							.atZone(ZoneId.systemDefault())
							.toLocalDateTime();
					return ldt.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
				}
			}
		} catch (IOException ex) {
			LOG.err(TT + "getDate() error for " + file.getName(), ex);
		}
		return getFileSystemDate(file);
	}

	/**
	 * Parse MP4 container to search 'mvhd' atom (moov -> mvhd).
	 */
	private static long readCreationTimeFromMvhd(File file) throws IOException {
		try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
			byte[] header = new byte[8];
			while (in.read(header) == 8) {
				long atomSz = Endian.Big.getLong32(header[0], header[1], header[2],
						header[3]) & 0xFFFFFFFFL;
				String atomTp = new String(header, 4, 4, "ISO-8859-1");
				if (atomSz == 1) {
					byte[] extSz = new byte[8];
					if (in.read(extSz) != 8) {
						break;
					}
					atomSz = (Endian.Big.getLong32(extSz[4], extSz[5], extSz[6],
							extSz[7]) & 0xFFFFFFFFL) - 8;
				}
				if (atomSz < 8) {
					break;
				}
				if ("moov".equals(atomTp)) {
					continue;
				}
				if ("mvhd".equals(atomTp)) {
					byte[] mvhd = new byte[12];
					if (in.read(mvhd) != 12) {
						return -1;
					}
					int version = mvhd[0] & 0xFF;
					if (version == 1) {
						byte[] timeB = new byte[8];
						if (in.read(timeB) != 8) {
							return -1;
						}
						return Endian.Big.getLong32(timeB[4], timeB[5],
								timeB[6], timeB[7]) & 0xFFFFFFFFL;
					} else {
						return Endian.Big.getLong32(mvhd[4], mvhd[5],
								mvhd[6], mvhd[7]) & 0xFFFFFFFFL;
					}
				}
				long skipBytes = atomSz - 8;
				long skipped = 0;
				while (skipped < skipBytes) {
					long currentSkipped = in.skip(skipBytes - skipped);
					if (currentSkipped <= 0) {
						break;
					}
					skipped += currentSkipped;
				}
			}
		}
		return -1;
	}

	private static String getFileSystemDate(File file) {
		try {
			FileTime ct = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
			if (ct != null) {
				LocalDateTime lt = ct.toInstant()
						.atZone(ZoneId.systemDefault()).toLocalDateTime();
				return lt.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
			}
		} catch (IOException ex) {
			LOG.err(TT + "getFileSystemDate() error", ex);
		}
		return null;
	}

	/**
	 * Check if the MP4/MOV file contains a 'mvhd' atom with correct creation date.
	 *
	 * @param file The video file to check
	 * @return true if metadat exists and is valid, else return false.
	 */
	public static boolean hasMVHD(File file) {
		if (file == null || !file.exists() || file.isDirectory()) {
			return false;
		}
		try {
			long epochSeconds = readCreationTimeFromMvhd(file);
			return epochSeconds > 0;
		} catch (IOException ex) {
			LOG.err(TT + "hasMVHD() error for " + file.getName(), ex);
		}
		return false;
	}

}
