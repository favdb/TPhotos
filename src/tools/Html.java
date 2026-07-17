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
import i18n.I18N;
import java.awt.Color;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * utilities clas for HTML
 *
 * @author favdb
 */
public class Html {

	public static final String TYPE = "text/html",
			NL = "\n", // new ligne
			//DOCTYPE = "",//empty DOCTYPE
			DOCTYPE = "<!DOCTYPE html>" + NL,
			// alignment
			AL_CENTER = "text-align: center;",
			AL_LEFT = "text-align: left;",
			AL_RIGHT = "text-align: right;",
			// bold
			B_BEG = "<b>", B_END = "</b>",
			// html structures
			HTML_B = "<html>" + NL,
			HTML_B_LANG = "<html lang=\"" + Locale.getDefault().getLanguage() + "\">" + NL,
			HTML_E = "</html>" + NL,
			BODY_B = "<body>" + NL, BODY_E = "</body>" + NL,
			BR = "<br>" + NL,
			STYLE_B = "<style type=\"text/css\">\n" + NL, STYLE_E = "</style>" + NL,
			// color
			COLOR = "color",
			COLOR_BG = "background-color",
			DIV_B = "<div>", DIV_E = "</div>",
			EXT = ".html",
			// fonts
			FONT_FAMILY = "font-family",
			FONT_SIZE = "font-size",
			FONT_SIZE_DEFAULT = "html {font-size: "
			+ App.fontGet().getSize() + ";}",
			FONT_STYLE = "font-style",
			FONT_WEIGHT = "font-weight",
			// head tags
			HEAD_B = "<head>" + NL, HEAD_E = "</head>" + NL,
			HR = "<hr>" + NL,
			I_B = "<i>", I_E = "</i>",
			LI_B = "<li>", LI_E = "</li>" + NL,
			//margins
			MARGIN = "margin",
			MARGIN_BOTTOM = "margin-bottom",
			MARGIN_LEFT = "margin-left",
			MARGIN_RIGHT = "margin-right",
			MARGIN_TOP = "margin-top",
			// meta
			META_CONTENT = "<meta name=\"keywords\" content=\"HTML\">" + NL,
			META_UTF8 = "<meta charset=\"utf-8\">" + NL,
			//paddings
			PADDING = "padding",
			PADDING_LEFT = "padding-left",
			PADDING_RIGHT = "padding-right",
			// paragraph
			P_B = "<p>",
			P_CENTER = "<p style=\"" + AL_CENTER + "\">",
			P_EMPTY = "<p></p>" + NL,
			P_E = "</p>" + NL,
			TABLE_B = "<table>" + NL, TABLE_E = "</table>" + NL,
			TABLE_STYLE = STYLE_B
			+ "table,td,th {"
			+ "border: 1px solid black;"
			+ "border-collapse: separate;"
			+ "border-spacing: 0px;"
			+ "}"
			+ STYLE_E,
			TD_B = "<td>", TD_E = "</td>" + NL,
			TEXT_ALIGN = "text-align",
			TR_B = "<tr>" + NL, TR_E = "</tr>" + NL,
			U_B = "<u>", U_E = "</u>",
			UL_B = "<ul>" + NL, UL_E = "</ul>" + NL;

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
	 * convert HTML to plain text
	 *
	 * @param src String to convert
	 * @param preserveNewLines true for preserving new lines char
	 * @return
	 */
	public static String htmlToText(String src, boolean preserveNewLines) {
		//LOG.trace(TT + "htmlToText(src, preserve=" + (preserveNewLines ? "true" : "false")+")");
		if (src == null) {
			return ("");
		}
		String html = src;
		if (!preserveNewLines) {
			html = Jsoup.parse(html).text();
		} else {
			html = Jsoup.parse(html).wholeText();
		}
		html = html.replace("\n\n", NL);
		while (html.startsWith(NL)) {
			html = html.substring(1);
		}
		html = html.trim();
		while (html.endsWith(NL)) {
			html = html.substring(0, html.length() - 1);
		}
		return html;
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

	/**
	 * get a clean HTML, with empty HEAD tag
	 *
	 * @param text
	 * @return
	 */
	public static String toCleanHtml(String text) {
		//LOG.trace(TT + ".toCleanHtml(text=\"" + text + "\")");
		StringBuilder b = new StringBuilder(HTML_B);
		b.append(HEAD_B).append(HEAD_E);
		b.append(BODY_B).append(text).append(BODY_E);
		b.append(HTML_E);
		return b.toString();
	}

	public static String htmlToText(String html) {
		return htmlToText(html, false);
	}

	/**
	 * convert a HTML String into truncated plain text
	 *
	 * @param html
	 * @param len
	 * @return
	 */
	public static String htmlToText(String html, int len) {
		if (html == null) {
			return "";
		}
		String txt = html.replaceAll("<[/]?img[^>]*>", "[" + I18N.getMsg("image") + "]");
		return StringUtil.ellipsize(htmlToText(txt, true), len);
	}

	public static String getHtmlColor(Color color) {
		return "#" + getHexName(color);
	}

	/**
	 * Return the hex name of a specified color.
	 *
	 * @param color Color to get hex name of.
	 * @return Hex name of color: "rrggbb".
	 */
	public static String getHexName(Color color) {
		if (color == null) {
			return "ffffff";
		}
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		String rHex = Integer.toString(r, 16);
		String gHex = Integer.toString(g, 16);
		String bHex = Integer.toString(b, 16);
		return (rHex.length() == 2 ? "" + rHex : "0" + rHex)
				+ (gHex.length() == 2 ? "" + gHex : "0" + gHex)
				+ (bHex.length() == 2 ? "" + bHex : "0" + bHex);
	}

	public static String removeTag(String html, String tag) {
		Document doc = Jsoup.parse(html);
		for (Element element : doc.select(tag)) {
			element.remove();
		}
		String t = doc.body().outerHtml()
				.replace("<body>", "")
				.replace("</body>", "");
		if (t.startsWith(NL)) {
			t = t.substring(1);
		}
		if (t.endsWith(NL)) {
			t = t.substring(0, t.length() - 1);
		}
		return t.trim();
	}

}
