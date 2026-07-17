/*
 * Copyright (C) 2017 FaVdB
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tools;

import java.text.DecimalFormat;
import java.text.Normalizer;

/**
 * String utilities
 *
 * @author FaVdB
 */
public class StringUtil {

	private static final String TT = "StringUtil.";

	private static final String[][] repJava = {
		{"\"", "\\\""},
		{"\\", "\\\\"},
		{"\b", "\\b"},
		{"\n", "\\n"},
		{"\t", "\\t"},
		{"\f", "\\f"},
		{"\r", "\\r"}
	};
	private static final String[][] repHtml = {
		{"\"", "&quot;"},
		{"&", "&amp;"},
		{"<", "&lt;"},
		{">", "&gt;"}
	};
	private static final String EMPTY = "";

	/**
	 * check if the String contains one space
	 *
	 * @param str
	 * @return
	 */
	public static boolean containsSpace(String str) {
		if (str != null) {
			for (char c : str.toCharArray()) {
				if (c == ' ') {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * capitalize the given String (suppressing underscore)
	 *
	 * @param str
	 * @return
	 */
	public static String capitalizeKEY(String str) {
		String[] k = str.split("_");
		StringBuilder b = new StringBuilder();
		for (String s : k) {
			b.append(s.substring(0, 1).toUpperCase() + s.substring(1));
		}
		return b.toString();
	}

	/**
	 * capitalize the given String
	 *
	 * @param str
	 * @return
	 */
	public static String capitalize(final String str) {
		return capitalize(str, null);
	}

	/**
	 * capitalize the given String with specific delimiters
	 *
	 * @param str
	 * @param delimiters optional
	 * @return
	 */
	public static String capitalize(final String str, final char... delimiters) {
		final int delimLen = delimiters == null ? -1 : delimiters.length;
		if (str == null || str.isEmpty() || delimLen == 0) {
			return str;
		}
		int strLen = str.length();
		int[] newCodePoints = new int[strLen];
		int outOffset = 0;

		boolean capitalizeNext = true;
		for (int index = 0; index < strLen;) {
			final int codePoint = str.codePointAt(index);
			if (isDelimiter(codePoint, delimiters)) {
				capitalizeNext = true;
				newCodePoints[outOffset++] = codePoint;
				index += Character.charCount(codePoint);
			} else if (capitalizeNext) {
				int titleCaseCodePoint = Character.toTitleCase(codePoint);
				newCodePoints[outOffset++] = titleCaseCodePoint;
				index += Character.charCount(titleCaseCodePoint);
				capitalizeNext = false;
			} else {
				newCodePoints[outOffset++] = codePoint;
				index += Character.charCount(codePoint);
			}
		}
		return new String(newCodePoints, 0, outOffset);
	}

	/**
	 * check if character is a delimiter
	 *
	 * @param codePoint
	 * @param delimiters
	 * @return
	 */
	public static boolean isDelimiter(final int codePoint, final char[] delimiters) {
		if (delimiters == null) {
			return Character.isWhitespace(codePoint);
		}
		for (int index = 0; index < delimiters.length; index++) {
			int delimiterCodePoint = Character.codePointAt(delimiters, index);
			if (delimiterCodePoint == codePoint) {
				return true;
			}
		}
		return false;
	}

	/**
	 * convert a String to escaped String
	 *
	 * @param rep
	 * @param inStr
	 * @return
	 */
	private static String escape(String[][] rep, String inStr) {
		String ret = inStr;
		for (String[] r : rep) {
			ret = ret.replace(r[0], r[1]);
		}
		return (ret);
	}

	/**
	 * escape UTF8 String
	 *
	 * @param in
	 * @return
	 */
	private static String escapeUTF8(String in) {
		StringBuilder b = new StringBuilder();
		for (char c : in.toCharArray()) {
			if (c >= 0x80 && c < 0xff) {
				b.append("\\u00").append(Integer.toHexString(c));
			} else if (c >= 0x80 && c < 0xff) {
				b.append("\\u").append(Integer.toHexString(c));
			} else {
				b.append(c);
			}
		}
		return b.toString();
	}

	/**
	 * escape txt String
	 *
	 * @param text
	 * @return
	 */
	public static String escapeTxt(String text) {
		return text == null ? null : Normalizer.normalize(text, Normalizer.Form.NFD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
				.replace("'", "");
	}

	/**
	 * escape XML String
	 *
	 * @param text
	 * @return
	 */
	public static String escapeXml(String text) {
		return text == null ? null : Normalizer.normalize(text, Normalizer.Form.NFD)
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&apos;");
	}

	/**
	 * escape Java String
	 *
	 * @param inStr
	 * @return
	 */
	public static String escapeJava(final String inStr) {
		return (escape(repJava, inStr));
	}

	/**
	 * escape HTML String
	 *
	 * @param inStr
	 * @return
	 */
	public static String escapeHtml(String inStr) {
		return (escape(repHtml, inStr));
	}

	/**
	 * unescape a String
	 *
	 * @param rep
	 * @param inStr
	 * @return
	 */
	private static String unescape(String[][] rep, String inStr) {
		String ret = inStr;
		for (String[] r : rep) {
			ret = ret.replace(r[1], r[0]);
		}
		return (ret);
	}

	/**
	 * unescape Java String
	 *
	 * @param inStr
	 * @return
	 */
	public static String unescapeJava(final String inStr) {
		return (unescape(repJava, inStr));
	}

	/**
	 * unescape HTML String
	 *
	 * @param inStr
	 * @return
	 */
	public static String unescapeHtml(String inStr) {
		return (unescape(repHtml, inStr));
	}

	/**
	 * get a sub text from the given String
	 *
	 * @param text
	 * @param start
	 * @param end
	 * @return
	 */
	public static String subText(String text, String start, String end) {
		if (text == null || text.isEmpty()) {
			return "";
		}
		if (text.contains(start)) {
			String rtext = text.substring(text.indexOf(start) + start.length());
			if (rtext.contains(end)) {
				rtext = rtext.substring(0, rtext.indexOf(end));
			}
			return rtext;
		}
		return "";
	}

	/**
	 * add a repeated character into the given String
	 *
	 * @param str
	 * @param repeat
	 * @return
	 */
	public static String repeat(String str, int repeat) {
		if (str == null) {
			return null;
		}
		if (repeat <= 0) {
			return EMPTY;
		}
		String ret = EMPTY;
		for (int i = 0; i < repeat; i++) {
			ret += str;
		}
		return (ret);
	}

	/**
	 * convert to a Roman number
	 */
	private static final String[] RCODE = {"M", "CM", "D", "CD", "C", "XC",
		"L", "XL", "X", "IX", "V", "IV", "I"};
	private static final int[] BVAL = {1000, 900, 500, 400, 100, 90, 50, 40,
		10, 9, 5, 4, 1};

	/**
	 * convert into roman
	 *
	 * @param number
	 * @return
	 */
	public static String intToRoman(int number) {
		if (number <= 0 || number >= 4000) {
			return "";
		}
		String roman = "";
		for (int i = 0; i < RCODE.length; i++) {
			while (number >= BVAL[i]) {
				number -= BVAL[i];
				roman += RCODE[i];
			}
		}
		return roman;
	}

	/**
	 * check if String contains only digits
	 *
	 * @param string
	 * @return
	 */
	public static boolean isNumeric(String string) {
		if (string == null || string.length() == 0) {
			return false;
		}
		int l = string.length();
		int ni = 0;
		if (string.startsWith("-") || string.startsWith("+")) {
			ni = 1;
		}
		for (int i = ni; i < l; i++) {
			if (!Character.isDigit(string.codePointAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * check if the given String is a time value
	 *
	 * @param time : as "DD/MM/AAAA hh:mm:ss"<br> where DD/MM/AAAA and hh:mm:jj are optional but one
	 * of them is mandatory
	 *
	 * @return true if the value is OK
	 */
	public static boolean checkTime(String time) {
		String str[] = time.split(" ");
		if (str.length > 0) {
			int idx = 0;
			if (str[idx].contains("/")) {
				// DD and MM and AAAA are mandatory
				String d[] = str[0].split("/");
				if (d.length < 3) {
					return false;
				}
				idx++;
			}
			if (str[idx].contains(":")) {
				//hh and mm and ss are mandatory
				String d[] = str[0].split(":");
				if (d.length < 3) {
					return false;
				}
				idx++;
			}
		}
		return true;
	}

	/**
	 * convert the given unicode String
	 *
	 * @param instr
	 * @return
	 */
	public static String fromUnicode(String instr) {
		if (!instr.contains("\\u")) {
			return instr;
		}
		String str = "";
		int position = instr.indexOf("\\u");
		while (position != -1) {
			if (position != 0) {
				str += instr.substring(0, position);
			}
			String token = instr.substring(position + 2, position + 6);
			instr = instr.substring(position + 6);
			str += (char) Integer.parseInt(token, 16);
			position = instr.indexOf("\\u");
		}
		str += instr;
		return str;
	}

	/**
	 * Decode a text that is encoded as a Java string literal. The Java properties file format and
	 * Java source code format is supported.
	 *
	 * @param s the encoded string
	 * @return the string
	 */
	public static String javaDecode(String s) {
		int length = s.length();
		StringBuilder buff = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			if (c == '\\') {
				if (i + 1 >= s.length()) {
					LOG.err(TT + "javaDecode(s=\"" + s + "\") case '\\\\' format exception i=" + i);
					return "";
				}
				c = s.charAt(++i);
				if (c == '\\') {
					buff.append("\\");
					continue;
				}
				switch (c) {
					case 't':
						buff.append("\t");
						break;
					case 'r':
						buff.append("\r");
						break;
					case 'n':
						buff.append("\n");
						break;
					case 'b':
						buff.append("\b");
						break;
					case 'f':
						buff.append("\f");
						break;
					case '#':
						// for properties files
						buff.append("#");
						break;
					case '=':
						// for properties files
						buff.append("=");
						break;
					case ':':
						// for properties files
						buff.append(":");
						break;
					case '"':
						buff.append("\"");
						break;
					case '\\':
						buff.append("\\");
						break;
					case 'u': {
						if (i + 4 >= length) {
							LOG.err(TT + "javaDecode(s=\"" + s + "\") case 'u' format exception i=" + i);
							return "";
						}
						try {
							c = (char) Integer.parseInt(s.substring(i + 1, i + 5), 16);
						} catch (NumberFormatException e) {
							LOG.err(TT + "javaDecode(s=\"" + s + "\") case 'u' number format exception i=" + i);
							return "";
						}
						i += 4;
						buff.append(c);
						break;
					}
					default:
						if (c >= '0' && c <= '9' && i + 2 < length) {
							try {
								c = (char) Integer.parseInt(s.substring(i, i + 3), 8);
							} catch (NumberFormatException e) {
								LOG.err(TT + "javaDecode(s=\"" + s + "\") case default number format exception i=" + i);
								return "";
							}
							i += 2;
							buff.append(c);
						} else {
							LOG.err(TT + "javaDecode(s=\"" + s + "\") case default number format exception i=" + i);
							return "";
						}
				}
			} else {
				buff.append(c);
			}
		}
		return buff.toString();
	}

	private static final char[] HEX = "0123456789abcdef".toCharArray();

	/**
	 * Convert a string to a Java literal using the correct escape sequences. The literal is not
	 * enclosed in double quotes. The result can be used in properties files or in Java source code.
	 *
	 * @param s the text to convert
	 * @param buff the Java representation to return
	 * @param forSQL true if we embedding this inside a STRINGDECODE SQL command
	 */
	public static void javaEncode(String s, StringBuilder buff, boolean forSQL) {
		int length = s.length();
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			switch (c) {
				case '\t':
					// HT horizontal tab
					buff.append("\\t");
					break;
				case '\n':
					// LF linefeed
					buff.append("\\n");
					break;
				case '\f':
					// FF form feed
					buff.append("\\f");
					break;
				case '\r':
					// CR carriage return
					buff.append("\\r");
					break;
				case '"':
					// double quote
					buff.append("\\\"");
					break;
				case '\'':
					// quote:
					if (forSQL) {
						buff.append('\'');
					}
					buff.append('\'');
					break;
				case '\\':
					// backslash
					buff.append("\\\\");
					break;
				default:
					if (c >= ' ' && (c < 0x80)) {
						buff.append(c);
					} else {
						buff.append("\\u")
								.append(HEX[c >>> 12])
								.append(HEX[c >>> 8 & 0xf])
								.append(HEX[c >>> 4 & 0xf])
								.append(HEX[c & 0xf]);
					}
			}
		}
	}

	/**
	 * get Integer value from the given String
	 *
	 * @param text
	 * @return
	 */
	public static Integer getInteger(String text) {
		if (text == null || text.isEmpty()) {
			return 0;
		}
		if (isNumeric(text)) {
			return Integer.valueOf(text);
		}
		return 0;
	}

	/**
	 * Constant unity
	 */
	private static final long[] CST_SIZE = {1024, 1024 * 1024, 1024 * 1024 * 1024};
	private static final String[] CST_UNITS = {"Ko", "Mo", "Go"};

	/**
	 * Show the file size in a formatted String
	 *
	 * @param value
	 * @return string
	 */
	public static String format(final long value) {
		String result = null;
		long size;
		for (int i = 0; i < 3; i++) {
			size = value / CST_SIZE[i];
			if (size <= 1024) {
				result = mergeUnit(size, CST_UNITS[i]);
				break;
			}
		}
		return result;
	}

	/**
	 * Arround and add unity
	 *
	 * @param size
	 * @param unit
	 * @return
	 */
	public static String mergeUnit(long size, String unit) {
		return new DecimalFormat("#,##0.#").format(size) + " " + unit;
	}

	public static String ELLIPSIS = "\u2026";

	/**
	 * truncate the String to 200 characters and add an ellipsis character
	 *
	 * @param text
	 * @return
	 */
	public static String ellipsize(String text) {
		return ellipsize(text, 200);
	}

	/**
	 * truncate the String to max length and add an ellipsis character
	 *
	 * @param str
	 * @param max
	 * @return
	 */
	public static String ellipsize(String str, int max) {
		if (str == null || str.isEmpty()) {
			return "";
		}
		if (max < 1) {
			if (str.contains(" ")) {
				return str.substring(0, str.indexOf(" ")) + ELLIPSIS;
			}
		} else if (str.length() > max) {
			return str.substring(0, max) + ELLIPSIS;
		}
		return str;
	}

	/**
	 * count the number of words in a text
	 *
	 * @param text
	 * @return
	 */
	public static int countWords(String text) {
		if (text == null) {
			return 0;
		}
		String txt = Html.htmlToText(text, true).replaceAll("[^ a-z A-Z 0-9 ]+", " ");
		while (txt.contains("  ")) {
			txt = txt.replace("  ", " ");
		}
		if (text.trim().isEmpty()) {
			return 0;
		}
		String[] words = txt.trim().split("\\s+");
		int count = words.length;
		for (String word : words) {
			String w = word.trim();
			if (w.length() == 0) {
				count--;
			}
		}
		/*String punc = "#`’~!#$%^，";
		for (String word : words) {
			String w = word.trim();
			if (w.length() == 0 || punc.contains(w)) {
				count--;
			}
		}
		if (count > 0) {
			count--;
		}*/
		return count;
	}

	/**
	 * count the number of characters in text
	 *
	 * @param text
	 * @return
	 */
	public static int countChars(String text) {
		if (text == null || text.isEmpty()) {
			return (0);
		}
		if (text.contains("<") && text.contains(">")) {
			return Html.htmlToText(text, true).trim().length();
		}
		return text.trim().length();
	}

}
