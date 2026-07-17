/*
 * Copyright (C) 2024 favdb
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package tools.jpeg;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import javax.swing.SwingUtilities;
import tools.LOG;

/**
 * Jpeg class is used to store all information of a jpeg file. It contains all structures of a jpeg.
 * adapted from https://github.com/drewnoakes/metadata-extractor
 */
public class Jpeg {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			LOG.setTrace();
			LOG.trace("date=" + getDate(new File("/xDev/AlbumPhoto/test.jpg")));
		});
	}

	public byte[] jfif, exifMarker, exifData, compressedData;
	public LinkedList<byte[]> remainSegment;
	public JpegExif exif;
	private boolean notFinishSegmentReading, notReadExif, notReadJfif;
	private static final int HEADER_SIZE = 10;

	/**
	 * read jpeg file and divide data area into jfif.
	 *
	 * @param f
	 * @throws IOException if the file contains wrong or unreadable information
	 */
	public Jpeg(File f) throws Exception {
		//read the marker to make sure it is a jpeg
		try (BufferedInputStream buff = new BufferedInputStream(new FileInputStream(f))) {
			//read the marker to make sure it is a jpeg
			byte[] fileMarker = new byte[2];
			if (buff.read(fileMarker) == -1) {
				buff.close();
				throw new Exception("File does not have enough data on marker");
			}
			//check the file type
			if (!((fileMarker[0] & 0xFF) == 0xFF && (fileMarker[1] & 0xFF) == 0xD8)) {
				buff.close();
				throw new Exception(f.getName() + " is not a jpeg/jpg file");
			}
			notReadExif = true;
			notReadJfif = true;
			remainSegment = new LinkedList<>();
			//process first 2 segments to find potential JFIF and EXIF segment.
			for (int i = 0; i < 2; i++) {
				byte[] segment = readSegment(buff);
				if (notReadJfif && (segment[0] & 0xFF) == 0xFF && (segment[1] & 0xFF) == 0xE0) {
					jfif = segment;
					notReadJfif = false;
				} else if (notReadExif && (segment[0] & 0xFF) == 0xFF && (segment[1] & 0xFF) == 0xE1) {
					processExif(segment);
					exif = new JpegExif(exifData);
					notReadExif = false;
				} else {
					remainSegment.add(segment);
				}
			}
			//finish remaining segment reading
			notFinishSegmentReading = true;
			while (notFinishSegmentReading) {
				byte[] segment = readSegment(buff);
				remainSegment.add(segment);
			}
			//read compressed jpeg data
			compressedData = new byte[buff.available()];
			buff.read(compressedData);
		}
	}

	/**
	 * exif segment is divided into marker and content.
	 */
	private void processExif(byte[] exifSegment) {
		exifMarker = Arrays.copyOfRange(exifSegment, 0, HEADER_SIZE);
		exifData = Arrays.copyOfRange(exifSegment, HEADER_SIZE, exifSegment.length);
	}

	/**
	 * a byte array which contains an segment
	 */
	private byte[] readSegment(BufferedInputStream f) throws IOException {
		byte[] header = new byte[2];
		if (f.read(header) == -1) {
			throw new IOException("There is not sufficient data on reading segment header");
		}
		byte[] sizeData = new byte[2];
		if (f.read(sizeData) == -1) {
			throw new IOException("There is not sufficient data on reading segment size");
		}
		int size = Endian.Big.getInt16(sizeData[0], sizeData[1]);
		if ((header[0] & 0xFF) == 0xFF) {
			if ((header[1] & 0xFF) == 0xDA) {
				notFinishSegmentReading = false;
			}
		} else {
			System.out.printf("Error segment %02x %02x %n", header[0], header[1]);
			return null;
		}
		byte[] content = new byte[size + 2]; //content will include header information
		content[0] = header[0];
		content[1] = header[1];
		content[2] = sizeData[0];
		content[3] = sizeData[1];
		for (int i = 4; i < size + 2; i++) {
			content[i] = (byte) f.read();
		}
		return content;
	}

	public static String getDate(File file) {
		try {
			if (!file.exists()) {
				return null;
			}
			String str = null;
			Jpeg jpeg = new Jpeg(file);
			if (jpeg.exif != null) {
				for (JpegExifEntry entry : jpeg.exif.getIfd0()) {
					if (entry.getTagNumberAsString().equals(JpegExifTag.TAG_CHANGEDATETIME)) {
						str = safeChar((String) entry.getValue()).replace(" ", "_");
						return str;
					}
				}
			}
			FileTime ct = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
			if (ct != null) {
				LocalDateTime lt = ct.toInstant()
						.atZone(ZoneId.systemDefault()).toLocalDateTime();
				return lt.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
			}
		} catch (Exception ex) {
			LOG.err("Jpeg.getDate() error", ex);
		}
		return null;
	}

	public static String safeChar(String input) {
		char[] allowed = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ ".toCharArray();
		char[] charArray = input.toCharArray();
		StringBuilder result = new StringBuilder();
		for (char c : charArray) {
			for (char a : allowed) {
				if (c == a) {
					result.append(a);
				}
			}
		}
		return result.toString();
	}

}
