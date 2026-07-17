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
package app.diapo;

import app.xml.Xml;
import app.xml.XmlUtil;
import i18n.I18N;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import tools.file.FileUtil;

/**
 *
 * @author favdb
 */
public class DiapoParam {

	private String name = "Album", comment = "{JJ/MM/AAAA}";
	private Integer mode = 0, tempo = 0;

	public DiapoParam() {

	}

	public DiapoParam(Xml xml) {
		Node n = xml.getNode("pref");
		if (n != null) {
			mode = XmlUtil.getInteger(n, "mode");
			tempo = XmlUtil.getInteger(n, "tempo");
			comment = XmlUtil.getString(n, "comment");
		} else {
			xml.childCreate(xml.getRoot(), "pref");
		}
	}

	public void setMode(int value) {
		this.mode = value;
	}

	public Integer getMode() {
		if (mode < 0) {
			return 0;
		}
		return mode;
	}

	public void setTempo(int value) {
		this.tempo = value;
	}

	public Integer getTempo() {
		if (tempo < 0) {
			return 0;
		}
		return tempo;
	}

	public void setComment(String value) {
		this.comment = value;
	}

	public String getComment() {
		return comment;
	}

	public String getComment(File file) {
		String dt = FileUtil.removeExtension(file.getName());
		String str = comment;
		if (comment.contains("{") && comment.contains("}")) {
			try {
				SimpleDateFormat fm1 = new SimpleDateFormat("yyyyMMdd_HHmmss");
				Date date = fm1.parse(dt);
				Map<String, String> df = new HashMap<>();
				df.put("album.param.comment.day", "dd/MM/yyyy");
				df.put("album.param.comment.month", "MM/yyyy");
				df.put("album.param.comment.year", "yyyy");
				df.put("album.param.comment.month_long", "MMM yyyy");
				df.put("album.param.comment.full", "dd MMM yyyy");
				df.put("album.param.comment.full_hour", "dd MMM yyyy HH:mm:ss");
				for (Map.Entry<String, String> entry : df.entrySet()) {
					String key = entry.getKey();
					String motif = "{" + I18N.getMsg(key) + "}";
					if (str.contains(motif)) {
						SimpleDateFormat fm2 = new SimpleDateFormat(entry.getValue());
						str = str.replace(motif, fm2.format(date));
						break;
					}
				}
			} catch (ParseException ex) {
				return "";
			}
		}
		return str;
	}

	public void updateXml(Xml xml) {
		Element n = (Element) xml.getNode("pref");
		if (n == null) {
			n = xml.getDocument().createElement("pref");
			xml.getRoot().appendChild(n);
		}
		mode = Math.max(mode, 0);
		tempo = Math.max(tempo, 0);
		n.setAttribute("mode", mode.toString());
		n.setAttribute("tempo", tempo.toString());
		n.setAttribute("comment", comment);
	}

	public String toXml() {
		StringBuilder b = new StringBuilder("   <pref ");
		b.append("comment=\"").append(comment).append("\" ");
		b.append("mode=\"").append(mode.toString()).append("\" ");
		b.append("tempo=\"").append(tempo.toString()).append("\" ");
		b.append("/>\n");
		return b.toString();
	}

}
