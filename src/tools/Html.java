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
package tools;

import app.App;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * utilities clas for HTML
 *
 * @author favdb
 */
public class Html {

	private static final String BR = "<br>";

	/**
	 * get the body content as String
	 *
	 * @param html
	 * @return empty String if there is no body tag
	 */
	public static String getBody(String html) {
		String bodyPattern = "(?s)<body.*?>(.*?)</body>";
		Pattern pattern = Pattern.compile(bodyPattern, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(html);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			LOG.err("Html.getBody(html=\n'" + html + "\n' no body part");
			return "";
		}
	}

	/**
	 * add all HTML common tags
	 *
	 * @param html
	 * @return
	 */
	public static String intoHtml(String html) {
		return "<html>"
				+ "<head>"
				+ "</head>"
				+ "<body style=\""
				+ "font-size: " + App.fontGet().getSize() + ";"
				+ "font-family: sans-serif;"
				+ "\">"
				+ html
				+ "</body></html>";
	}

	/**
	 * enclose the given String to get a color text in blue
	 *
	 * @param msg
	 * @return
	 */
	public static String intoBlue(String msg) {
		return "<font color=\"blue\">" + msg + "</font>";
	}

	/**
	 * enclose the given String to get a color text in green
	 *
	 * @param msg
	 * @return
	 */
	public static String intoGreen(String msg) {
		return "<font color=\"green\">" + msg + "</font>";
	}

	/**
	 * enclose the given String to get a color text in red
	 *
	 * @param msg
	 * @return
	 */
	public static String intoRed(String msg) {
		return "<font color=\"red\">" + msg + "</font>";
	}

	/**
	 * convert plain text to Html, inserting tags like P or BR
	 *
	 * @param text
	 * @return
	 */
	public static String textToHTML(String text) {
		if (text == null) {
			return "";
		}
		int length = text.length();
		boolean prevSlashR = false;
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < length; i++) {
			char ch = text.charAt(i);
			switch (ch) {
				case '\r':
					if (prevSlashR) {
						out.append(BR);
					}
					prevSlashR = true;
					break;
				case '\n':
					prevSlashR = false;
					out.append(BR);
					break;
				case '"':
					if (prevSlashR) {
						out.append(BR);
						prevSlashR = false;
					}
					out.append("&quot;");
					break;
				case '<':
					if (prevSlashR) {
						out.append(BR);
						prevSlashR = false;
					}
					out.append("&lt;");
					break;
				case '>':
					if (prevSlashR) {
						out.append(BR);
						prevSlashR = false;
					}
					out.append("&gt;");
					break;
				case '&':
					if (prevSlashR) {
						out.append(BR);
						prevSlashR = false;
					}
					out.append("&amp;");
					break;
				default:
					if (prevSlashR) {
						out.append(BR);
						prevSlashR = false;
					}
					out.append(ch);
					break;
			}
		}
		return (out.toString()
				.replace(BR + "-- ", BR + "&emsp;&emsp;◦ ")
				.replace(BR + "- ", BR + "&emsp;• "));
	}

	/**
	 * enclose the given String into P tag
	 *
	 * @param string
	 * @return
	 */
	public static String intoP(String string) {
		return "<p>" + string + "</p>";
	}

}
