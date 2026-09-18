package app.media;

import app.tools.LOG;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.swing.SwingUtilities;

/**
 * tools to extract date from various file type (JPEG, MOV, MP4old)
 *
 * @author favdb
 */
public class Media {

	private static final String TT = "MediaDateExtractor.";

	private static final DateTimeFormatter FORMATTER
			= DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

	/**
	 * test command
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			LOG.setTrace();
			LOG.trace(TT + "main() date="
					+ getDate(new File("/xDev/AlbumPhoto/test.jpg")));
		});
	}

	/**
	 * get internal creation date
	 *
	 * @param file
	 * @return
	 */
	public static String getDate(File file) {
		if (file == null || !file.exists() || file.isDirectory()) {
			return null;
		}
		try {
			if (jpegIs(file)) {
				Jpeg jpeg = new Jpeg(file);
				if (jpeg.exif != null) {
					return jpeg.exif.getDate();
				}
			} else if (mp4Is(file)) {
				return MP4.getDate(file);
			}
			return getFileSystemDate(file);
		} catch (Exception e) {
			LOG.err(TT + " : date extractor error for "
					+ file.getName(), e);
		}
		return null;
	}

	/**
	 * get file system date
	 *
	 * @param file
	 * @return
	 */
	private static String getFileSystemDate(File file) {
		try {
			FileTime ct = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
			if (ct != null) {
				LocalDateTime lt = ct.toInstant()
						.atZone(ZoneId.systemDefault())
						.toLocalDateTime();
				return lt.format(FORMATTER);
			}
		} catch (IOException ex) {
			LOG.err(TT + " : File attributes error " + file.getName(), ex);
		}
		return null;
	}

	/**
	 * check if given File is a photo
	 *
	 * @param file
	 * @return
	 */
	public static boolean jpegIs(File file) {
		if (file.isFile()) {
			String name = file.getName().toLowerCase();
			return (name.endsWith(".jpg") || name.endsWith(".jpeg"));
		}
		return false;
	}

	/**
	 * check if given File is a vido
	 *
	 * @param file
	 * @return
	 */
	public static boolean mp4Is(File file) {
		if (file.isFile()) {
			String name = file.getName().toLowerCase();
			return (name.endsWith(".mp4") || name.endsWith(".mov"));
		}
		return false;
	}

	public static String whichDate(File file) {
		if (Jpeg.hasEXIF(file)) {
			return "EXIF";
		} else if (MP4.hasMVHD(file)) {
			return "MP4";
		}
		return "fichier";
	}

	/**
	 * count the number of JPEG files in given directory
	 *
	 * @param dir
	 * @return
	 */
	public static int countJPEG(File dir) {
		//LOG.trace(TT + "countJPEG(dir=" + dir.getAbsolutePath() + ")");
		File[] files = dir.listFiles();
		if (files == null || files.length == 0) {
			return 0;
		}
		int nb = 0;
		for (File f : files) {
			if (f.isDirectory()) {
				nb += countJPEG(f);
			} else {
				if (Media.jpegIs(f)) {
					nb++;
				}
			}
		}
		return nb;
	}

	/**
	 * count the number of video files (MP4/MOV) in given directory
	 *
	 * @param dir
	 * @return
	 */
	public static int countMP4(File dir) {
		//LOG.trace(TT + "countMP4(dir=" + dir.getAbsolutePath() + ")");
		File[] files = dir.listFiles();
		if (files == null || files.length == 0) {
			return 0;
		}
		int nb = 0;
		for (File f : files) {
			if (f.isDirectory()) {
				nb += countMP4(f);
			} else {
				if (Media.mp4Is(f)) {
					nb++;
				}
			}
		}
		return nb;
	}

	/**
	 * class to store medias counters
	 */
	public static class MediaCount {

		public int jpegCount = 0;
		public int mp4Count = 0;

		public int totalGet() {
			return jpegCount + mp4Count;
		}
	}

	/**
	 * Simultaneous recursive count for number of photos and videos files in the given
	 * directory
	 *
	 * @param dir The directory to scan
	 * @return a MediaCount obect containing separate counters
	 */
	public static MediaCount count(File dir) {
		MediaCount res = new MediaCount();
		countRecursive(dir, res);
		return res;
	}

	/**
	 * recursive count
	 *
	 * @param dir
	 * @param res
	 */
	private static void countRecursive(File dir, MediaCount res) {
		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			return;
		}
		File[] files = dir.listFiles();
		if (files == null || files.length == 0) {
			return;
		}
		for (File f : files) {
			if (f.isDirectory()) {
				countRecursive(f, res);
			} else if (jpegIs(f)) {
				res.jpegCount++;
			} else if (mp4Is(f)) {
				res.mp4Count++;
			}
		}
	}

}
