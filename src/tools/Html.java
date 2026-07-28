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
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

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
	public static String htmlToText(String html, boolean preserveNewLines) {
		//LOG.trace(TT + "htmlToText(src, preserve=" + (preserveNewLines ? "true" : "false")+")");
		if (html == null || html.trim().isEmpty()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();

		HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {
			private boolean inBlockTag = false;

			@Override
			public void handleText(char[] data, int pos) {
				sb.append(data);
				inBlockTag = false;
			}

			@Override
			public void handleSimpleTag(HTML.Tag t, javax.swing.text.MutableAttributeSet a, int pos) {
				if (preserveNewLines && t == HTML.Tag.BR) {
					sb.append("\n");
					inBlockTag = true;
				}
			}

			@Override
			public void handleStartTag(HTML.Tag t, javax.swing.text.MutableAttributeSet a, int pos) {
				if (preserveNewLines && isBlockTag(t)) {
					// Évite de multiplier les saut de lignes inutiles si plusieurs balises bloc s'enchaînent
					if (sb.length() > 0 && !inBlockTag && sb.charAt(sb.length() - 1) != '\n') {
						sb.append("\n");
					}
					inBlockTag = true;
				}
			}

			private boolean isBlockTag(HTML.Tag t) {
				return t == HTML.Tag.P || t == HTML.Tag.DIV || t == HTML.Tag.H1
						|| t == HTML.Tag.H2 || t == HTML.Tag.H3 || t == HTML.Tag.H4
						|| t == HTML.Tag.H5 || t == HTML.Tag.H6 || t == HTML.Tag.LI
						|| t == HTML.Tag.TR;
			}
		};

		Reader reader = new StringReader(html);
		try {
			new ParserDelegator().parse(reader, callback, true);
		} catch (IOException ex) {
			sb.append("");
		}
		return sb.toString().trim();
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

}
