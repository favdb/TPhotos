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
import java.nio.ByteBuffer;
import java.util.*;

/**
 * JpegExif will process a byte[] exif data and gather the information.<br>
 * User can access JpegExif through class Jpeg.
 *
 * adapted from https://github.com/drewnoakes/metadata-extractor
 */
public class JpegExif {

	private boolean bigEndian;
	private int position, gpsOffset = 0, subOffset = 0, ifd1Offset = 0, interoperabilityOffset = 0;
	private int imageWidth = 0, imageHeight = 0;
	private String latRef, longRef;
	private Integer latDegree = null, longDegree = null, latMinute = null, longMinute = null;
	private Double latSecond = null, longSecond = null;

	private LinkedList<JpegExifEntry> gpsEntry, ifd0, subIfd, ifd1, interoperabilityIfd;

	private static final int HEADER_SIZE = 8, RATIONAL_SIZE = 8, SHORT_SIZE = 2, LONG_SIZE = 4;
	private static final int[] DATA_SIZE = {1, 1, 1, 2, 4, 8, 1, 1, 2, 4, 8, 4, 8};

	//Post: everything is set to null.
	public JpegExif() {
		gpsEntry = null;
		ifd0 = null;
		subIfd = null;
		ifd1 = null;
		interoperabilityIfd = null;
	}

	/**
	 * read exif data and assign to associate data fields.
	 */
	public JpegExif(byte[] exif) throws IOException {
		if (exif == null) {
			return;
		}
		position = 0;
		if ((char) exif[position] == 'M' && (char) exif[position + 1] == 'M') {
			bigEndian = true;
		} else if ((char) exif[position] == 'I' && (char) exif[position + 1] == 'I') {
			bigEndian = false;
		} else {
			return;
		}
		position += 2;
		if (bigEndian) {
			if (!((exif[position] & 0xFF) == 0x00 && (exif[position + 1] & 0xFF) == 0x2A)) {
				throw new IOException("Error on tag marker");
			}
		} else if (!((exif[position] & 0xFF) == 0x2A && (exif[position + 1] & 0xFF) == 0x00)) {
			throw new IOException("Error on tag marker");
		}
		position += 2;
		byte[] offsetData = new byte[4];
		for (int i = 0; i < 4; i++) {
			offsetData[i] = exif[position + i];
		}
		int firstIfdOffset = getInt32(offsetData) - HEADER_SIZE;
		position += (LONG_SIZE + firstIfdOffset);
		ifd0 = new LinkedList<>(Arrays.asList(readIfd(exif)));
		if (subOffset != 0) {
			position = subOffset;
			subIfd = new LinkedList<>(Arrays.asList(readIfd(exif)));
			analyzeSubIfd();
		}
		if (ifd1Offset != 0) {
			position = ifd1Offset;
			ifd1 = new LinkedList<>(Arrays.asList(readIfd(exif)));
		}
		if (gpsOffset != 0) {
			position = gpsOffset;
			gpsEntry = new LinkedList<>(Arrays.asList(readIfd(exif)));
			analyzeGps();
		}
		if (interoperabilityOffset != 0) {
			position = interoperabilityOffset;
			interoperabilityIfd = new LinkedList<>(Arrays.asList(readIfd(exif)));
		}

	}

	/**
	 * endian info represented by a boolean. If return value is true, exif is big endian. Exif is in
	 * little endian otherwise.
	 *
	 * @return
	 */
	public boolean isBigEndian() {
		return bigEndian;
	}

	/**
	 * offset to thumbnail format
	 *
	 * @return
	 */
	public int getImageWidth() {
		return imageWidth;
	}

	/**
	 * offset to thumbnail format
	 *
	 * @return
	 */
	public int getImageHeight() {
		return imageHeight;
	}

	/**
	 * a collection of Entry which is gps IFD
	 *
	 * @return
	 */
	public LinkedList<JpegExifEntry> getGpsIfd() {
		return gpsEntry;
	}

	/**
	 * a collection of Entry which is IFD0
	 *
	 * @return
	 */
	public LinkedList<JpegExifEntry> getIfd0() {
		return ifd0;
	}

	/**
	 * a collection of Entry which is sub IFD
	 *
	 * @return
	 */
	public LinkedList<JpegExifEntry> getSubIfd() {
		return subIfd;
	}

	/**
	 * a collection of Entry which is interoperability IFD
	 *
	 * @return
	 */
	public LinkedList<JpegExifEntry> getInterIfd() {
		return interoperabilityIfd;
	}

	/**
	 * a collection of Entry which is IFD1
	 *
	 * @return
	 */
	public LinkedList<JpegExifEntry> getIfd1() {
		return ifd1;
	}

	public String getDate() {
		for (JpegExifEntry entry : getIfd0()) {
			if (entry.getTagNumberAsString().equals(JpegExifTag.TAG_CHANGEDATETIME)) {
				return Jpeg.safeChar((String) entry.getValue()).replace(" ", "_");
			}
		}
		return null;
	}

	public String getLatitudeRef() {
		return latRef;
	}

	public String getLongitudeRef() {
		return longRef;
	}

	public Integer getLatitudeDegree() {
		return latDegree;
	}

	public Integer getLongitudeDegree() {
		return longDegree;
	}

	public Integer getLatitudeMinute() {
		return latMinute;
	}

	public Integer getLongitudeMinute() {
		return longMinute;
	}

	public Double getLatitudeSecond() {
		return latSecond;
	}

	public Double getLongitudeSecond() {
		return longSecond;
	}

	/**
	 * a collection of Entry after reading a IFD
	 */
	private JpegExifEntry[] readIfd(byte[] exif) {
		byte[] entryCountData = new byte[2];
		for (int i = 0; i < 2; i++) {
			entryCountData[i] = exif[position + i];
		}
		int entryCount = getInt16(entryCountData);
		position += 2;
		JpegExifEntry[] entryCollection = new JpegExifEntry[entryCount];
		for (int i = 0; i < entryCount; i++) {
			entryCollection[i] = new JpegExifEntry();
			byte[] tagNumber = new byte[2];
			if (bigEndian) {
				tagNumber[0] = exif[position];
				tagNumber[1] = exif[position + 1];
			} else {
				tagNumber[1] = exif[position];
				tagNumber[0] = exif[position + 1];
			}
			entryCollection[i].setTagNumber(tagNumber);
			position += 2;
			byte[] dataFormatValue = new byte[2];
			dataFormatValue[0] = exif[position];
			dataFormatValue[1] = exif[position + 1];
			int dataFormat = getInt16(dataFormatValue);
			entryCollection[i].setDataFormat(dataFormat);
			position += 2;
			byte[] componentCountValue = new byte[4];
			for (int j = 0; j < 4; j++) {
				componentCountValue[j] = exif[position + j];
			}
			int componentCount = getInt32(componentCountValue);
			entryCollection[i].setComponentCount(componentCount);
			position += 4;
			byte[] offset = new byte[4];
			for (int j = 0; j < 4; j++) {
				offset[j] = exif[position + j];
			}
			entryCollection[i].setOffset(offset);
			position += 4;
			Object value = getValue(offset, entryCollection[i].getDataFormat(), entryCollection[i].getComponentCount(), exif);
			entryCollection[i].setValue(value);
			if (!bigEndian) {
				switch (dataFormat) {
					case 4:
					case 5:
					case 9:
					case 10:
					case 11:
					case 12:
						swapByte(offset);
						break;
					case 3:
					case 8:
						if (componentCount * DATA_SIZE[dataFormat] <= 4) {
							byte temp = offset[0];
							offset[0] = offset[1];
							offset[1] = temp;
						} else {
							swapByte(offset);
						}
						break;
					case 2:
						if (componentCount * DATA_SIZE[dataFormat] > 4) {
							swapByte(offset);
						}
						break;
					default:
						break;
				}
			}

			analyzeEntry(entryCollection[i]);
		}
		byte[] ifd1OffsetData = new byte[4];
		if (bigEndian) {
			for (int i = 0; i < 4; i++) {
				ifd1OffsetData[i] = exif[position + i];
			}
		} else {
			for (int i = 0; i < 4; i++) {
				ifd1OffsetData[i] = exif[position + 3 - i];
			}
		}
		int offset = Endian.Big.getInt32(ifd1OffsetData);
		if (offset != 0) {
			ifd1Offset = offset;
		}

		return entryCollection;
	}

	/**
	 * print all tag in exif
	 */
	public void print() {
		if (ifd0 != null) {
			System.out.println("IFD0:");
			for (JpegExifEntry e : ifd0) {
				System.out.println(e);
			}
		}
		if (subIfd != null) {
			System.out.println("sub IFD:");
			for (JpegExifEntry e : subIfd) {
				System.out.println(e);
			}
		}
		if (ifd1 != null) {
			System.out.println("IFD1:");
			for (JpegExifEntry e : ifd1) {
				System.out.println(e);
			}
		}
		if (gpsEntry != null) {
			System.out.println("GPS data:");
			for (JpegExifEntry e : gpsEntry) {
				System.out.println(e);
			}
		}
		if (interoperabilityIfd != null) {
			System.out.println("Interoperability data:");
			for (JpegExifEntry e : interoperabilityIfd) {
				System.out.println(e);
			}
		}
	}

	/**
	 * Analyze the entry to find the gps ifd
	 */
	private void analyzeEntry(JpegExifEntry entry) {
		byte[] tagNumber = entry.getTagNumber();
		//set offset to gps IFD
		if ((tagNumber[0] & 0xFF) == 0x88 && (tagNumber[1] & 0xFF) == 0x25) {
			gpsOffset = getObjectValue(entry.getValue());
		} //set offset to subIFD
		else if ((tagNumber[0] & 0xFF) == 0x87 && (tagNumber[1] & 0xFF) == 0x69) {
			subOffset = getObjectValue(entry.getValue());
		}
	}

	/**
	 * the value associate to data format and offset is returned as an Object
	 */
	private Object getValue(byte[] offset, int format, int componentCount, byte[] exif) {
		int size = componentCount * DATA_SIZE[format];
		byte[] value = new byte[size];
		if (size > 4) {
			int valueAddress = getInt32(offset);
			for (int i = 0; i < size; i++) {
				value[i] = exif[valueAddress + i];
			}
		} else {
			value = offset;
		}
		switch (format) {
			case 1: //unsigned byte
				return value[0];
			case 2: //ASCII string
				return new String(value);
			case 3: //unsigned short
				if (size == 2) {
					byte[] shortValue = new byte[2];
					shortValue[0] = value[0];
					shortValue[1] = value[1];
					return getInt16(shortValue);
				} else {
					int[] result = new int[size / SHORT_SIZE];
					for (int i = 0; i < size / SHORT_SIZE; i++) {
						byte[] shortValue = new byte[2];
						shortValue[0] = value[2 * i];
						shortValue[1] = value[2 * i + 1];
						result[i] = getInt16(shortValue);
					}
					return result;
				}
			case 4: //unsigned long
				if (size == LONG_SIZE) {
					return getLong32(value);
				} else {
					long[] result = new long[size / LONG_SIZE];
					for (int i = 0; i < size / LONG_SIZE; i++) {
						byte[] longValue = new byte[4];
						for (int j = 0; j < 4; j++) {
							longValue[j] = value[2 * i + j];
						}
						result[i] = getLong32(longValue);
					}
					return result;
				}
			case 5: //unsigned rational
			case 10: //signed rational
				if (size == RATIONAL_SIZE) {
					byte[] numeratorData = new byte[4];
					byte[] denominatorData = new byte[4];
					for (int i = 0; i < 4; i++) {
						numeratorData[i] = value[i];
						denominatorData[i] = value[i + 4];
					}
					int[] result = new int[2];
					result[0] = getInt32(numeratorData);
					result[1] = getInt32(denominatorData);
					return result;
				} else {
					int[] result = new int[size / RATIONAL_SIZE * 2];
					for (int i = 0; i < size / RATIONAL_SIZE; i++) {
						byte[] numeratorData = new byte[4];
						byte[] denominatorData = new byte[4];
						for (int j = 0; j < 4; j++) {
							numeratorData[j] = value[j + RATIONAL_SIZE * i];
							denominatorData[j] = value[j + RATIONAL_SIZE * i + 4];
						}
						int numerator = getInt32(numeratorData);
						int denominator = getInt32(denominatorData);
						result[2 * i] = numerator;
						result[2 * i + 1] = denominator;
					}
					return result;
				}
			case 6: //signed byte
				return (char) value[0];
			case 7: //undefined
				return value;
			case 8: //signed short
				if (size == 2) {
					byte[] shortValue = new byte[2];
					shortValue[0] = value[0];
					shortValue[1] = value[1];
					return bigEndian ? Endian.Big.getSignedShort(shortValue) : Endian.Little.getSignedShort(shortValue);
				} else {
					short[] result = new short[size / SHORT_SIZE];
					for (int i = 0; i < size / SHORT_SIZE; i++) {
						byte[] shortValue = new byte[2];
						shortValue[0] = value[2 * i];
						shortValue[1] = value[2 * i + 1];
						result[i] = bigEndian ? Endian.Big.getSignedShort(shortValue) : Endian.Little.getSignedShort(shortValue);
					}
					return result;
				}
			case 9: //signed long
				if (size == LONG_SIZE) {
					return getInt32(value);
				} else {
					int[] result = new int[size / LONG_SIZE];
					for (int i = 0; i < size / LONG_SIZE; i++) {
						byte[] longValue = new byte[4];
						for (int j = 0; j < 4; j++) {
							longValue[j] = value[2 * i + j];
						}
						result[i] = getInt32(longValue);
					}
					return result;
				}
			case 11: //single float
				if (!bigEndian) {
					for (int i = 0; i < 2; i++) {
						byte temp = value[i];
						value[i] = value[4 - i];
						value[4 - i] = temp;
					}
				}
				return ByteBuffer.wrap(value).getFloat();
			case 12: //single double
				if (!bigEndian) {
					for (int i = 0; i < 4; i++) {
						byte temp = value[i];
						value[i] = value[4 - i];
						value[4 - i] = temp;
					}
				}
				return ByteBuffer.wrap(value).getDouble();
			default:
				return "unknown data";
		}
	}

	/**
	 * a byte[] which is swapped
	 */
	private void swapByte(byte[] b) {
		for (int i = 0; i < b.length / 2; i++) {
			byte temp = b[i];
			b[i] = b[b.length - i - 1];
			b[b.length - i - 1] = temp;
		}
	}

	/**
	 * image width and height would be available
	 */
	private void analyzeSubIfd() {
		for (JpegExifEntry e : subIfd) {
			byte[] tagNumber = e.getTagNumber();
			if ((tagNumber[0] & 0xFF) == 0xa0 && (tagNumber[1] & 0xFF) == 0x02) {
				imageWidth = getObjectValue(e.getValue());
			} else if ((tagNumber[0] & 0xFF) == 0xa0 && (tagNumber[1] & 0xFF) == 0x03) {
				imageHeight = getObjectValue(e.getValue());
			} else if ((tagNumber[0] & 0xFF) == 0xa0 && (tagNumber[1] & 0xFF) == 0x05) {
				interoperabilityOffset = getObjectValue(e.getValue());
			}
		}
	}

	/**
	 * analyze gps IFD and set lat and long
	 */
	private void analyzeGps() {
		for (JpegExifEntry e : gpsEntry) {
			byte[] tagNumber = e.getTagNumber();
			if ((tagNumber[0] & 0xFF) == 0x00 && (tagNumber[1] & 0xFF) == 0x01) {
				latRef = (String) e.getValue();
			} else if ((tagNumber[0] & 0xFF) == 0x00 && (tagNumber[1] & 0xFF) == 0x02) {
				int[] latData = (int[]) e.getValue();
				latDegree = latData[0] / latData[1];
				latMinute = latData[2] / latData[3];
				latSecond = (double) latData[4] / latData[5];
			} else if ((tagNumber[0] & 0xFF) == 0x00 && (tagNumber[1] & 0xFF) == 0x03) {
				longRef = (String) e.getValue();
			} else if ((tagNumber[0] & 0xFF) == 0x00 && (tagNumber[1] & 0xFF) == 0x04) {
				int[] longData = (int[]) e.getValue();
				longDegree = longData[0] / longData[1];
				longMinute = longData[2] / longData[3];
				longSecond = (double) longData[4] / longData[5];
			}
		}
	}

	//Pre: Object should be int or long
	/**
	 * an int that represent by the object
	 */
	private int getObjectValue(Object obj) {
		if (obj instanceof Integer) {
			return (int) (obj);
		} else {
			return (int) ((long) obj);
		}
	}

	/**
	 * an signed int which 4 bytes represent.
	 */
	private int getInt32(byte[] value) {
		return bigEndian ? Endian.Big.getInt32(value) : Endian.Little.getInt32(value);
	}

	/**
	 * a long which 4 bytes represent
	 */
	private long getLong32(byte[] value) {
		return bigEndian ? Endian.Big.getLong32(value) : Endian.Little.getLong32(value);
	}

	/**
	 * an int which 2 bytes represent
	 */
	private int getInt16(byte[] value) {
		return bigEndian ? Endian.Big.getInt16(value) : Endian.Little.getInt16(value);
	}
}
