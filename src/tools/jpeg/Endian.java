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

/**
 * adapted from https://github.com/drewnoakes/metadata-extractor
 */
public class Endian {

	public static class Big {//for BigEndian

		public static int getInt16(byte data1, byte data2) {
			int result = 0;
			result = result | (data1 & 0xFF);
			result = result << 8;
			result = result | (data2 & 0xFF);
			return result;
		}

		public static int getInt32(byte data1, byte data2, byte data3, byte data4) {
			int result = 0;
			result = result | (data1 & 0xFF);
			result = result << 8;
			result = result | (data2 & 0xFF);
			result = result << 8;
			result = result | (data3 & 0xFF);
			result = result << 8;
			result = result | (data4 & 0xFF);
			return result;
		}

		public static long getLong32(byte data1, byte data2, byte data3, byte data4) {
			long result = 0;
			result = result | (data1 & 0xFF);
			result = result << 8;
			result = result | (data2 & 0xFF);
			result = result << 8;
			result = result | (data3 & 0xFF);
			result = result << 8;
			result = result | (data4 & 0xFF);
			return result;
		}

		public static short getSignedShort(byte data1, byte data2) {
			short signed_short = 0;
			signed_short = (short) (signed_short | (data1 & 0xFF));
			signed_short = (short) (signed_short << 8);
			signed_short = (short) (signed_short | (data2 & 0xFF));
			return signed_short;
		}

		public static int getInt32(byte[] value) {
			return getInt32(value[0], value[1], value[2], value[3]);
		}

		public static short getSignedShort(byte[] value) {
			return getSignedShort(value[0], value[1]);
		}

		public static long getLong32(byte[] value) {
			return getLong32(value[0], value[1], value[2], value[3]);
		}

		public static int getInt16(byte[] value) {
			return getInt16(value[0], value[1]);
		}
	}

	public static class Little {// fpr LittleEndian

		public static int getInt16(byte data1, byte data2) {
			int result = 0;
			result = result | (data2 & 0xFF);
			result = result << 8;
			result = result | (data1 & 0xFF);
			return result;
		}

		public static int getInt32(byte data1, byte data2, byte data3, byte data4) {
			int result = 0;
			result = result | (data4 & 0xFF);
			result = result << 8;
			result = result | (data3 & 0xFF);
			result = result << 8;
			result = result | (data2 & 0xFF);
			result = result << 8;
			result = result | (data1 & 0xFF);
			return result;
		}

		public static long getLong32(byte data1, byte data2, byte data3, byte data4) {
			long result = 0;
			result = result | (data4 & 0xFF);
			result = result << 8;
			result = result | (data3 & 0xFF);
			result = result << 8;
			result = result | (data2 & 0xFF);
			result = result << 8;
			result = result | (data1 & 0xFF);
			return result;
		}

		public static short getSignedShort(byte data1, byte data2) {
			short signed_short = 0;
			signed_short = (short) (signed_short | (data2 & 0xFF));
			signed_short = (short) (signed_short << 8);
			signed_short = (short) (signed_short | (data1 & 0xFF));
			return signed_short;
		}

		public static int getInt32(byte[] value) {
			return getInt32(value[0], value[1], value[2], value[3]);
		}

		public static short getSignedShort(byte[] value) {
			return getSignedShort(value[0], value[1]);
		}

		public static long getLong32(byte[] value) {
			return getLong32(value[0], value[1], value[2], value[3]);
		}

		public static int getInt16(byte[] value) {
			return getInt16(value[0], value[1]);
		}

	}

}
