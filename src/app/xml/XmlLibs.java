/*
 * Copyright (C) 2026 favdb
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
package app.xml;

import app.tools.LOG;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author favdb
 */
public class XmlLibs {

	private static final String TT = "XmlLibs.";

	private final Xml xml;
	List<XmlLib> libs = new ArrayList<>();

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public XmlLibs(Xml xml) {
		this.xml = xml;
		load();
	}

	public List<XmlLib> getAll() {
		return libs;
	}

	/**
	 * load the library of text Key: ID of the text, Value: HTML content (in a CDATA)
	 */
	private Map<Integer, String> loadLibs() {
		Map<Integer, String> libMap = new HashMap<>();
		NodeList libsNodes = xml.getDocument().getElementsByTagName("lib");
		for (int i = 0; i < libsNodes.getLength(); i++) {
			Element el = (Element) libsNodes.item(i);
			String idStr = xml.attributeGet(el, "id").trim();
			if (!idStr.isEmpty()) {
				libMap.put(Integer.valueOf(idStr), el.getTextContent().trim());
			}
		}
		return libMap;
	}

	/**
	 * load all text as List of XmlLib
	 *
	 * @return
	 */
	public void load() {
		libs.clear();
		NodeList nodes = xml.rootGet().getElementsByTagName("lib");
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				Element child = (Element) nodes.item(i);
				String text = child.getTextContent();
				XmlLib p = new XmlLib(child.getAttribute("id"), text);
				libs.add(p);
			}
		}
	}

	/**
	 * get the XmlLib for the given index
	 *
	 * @param i
	 * @return
	 */
	public XmlLib libGet(int i) {
		if (i < libs.size()) {
			return libs.get(i);
		}
		return null;
	}

	/**
	 * Update the text content of the XmlLib given index .
	 *
	 * @param id index to modify
	 * @param value new text to set
	 */
	public void libUpdate(int id, String value) {
		for (XmlLib l : libs) {
			if (l.getId().equals("" + id)) {
				l.setText(value);
				break;
			}
		}
	}

	public String toXml() {
		StringBuilder b = new StringBuilder();
		b.append(XmlUtil.indent(1)).append("<libs>\n");
		for (XmlLib p : libs) {
			b.append(p.toXml());
		}
		b.append(XmlUtil.indent(1)).append("</libs>\n");
		return b.toString();
	}

	/**
	 * add the given text in libs
	 *
	 * @param value
	 * @return
	 */
	public int libAdd(String value) {
		int n = 0;
		for (XmlLib l : libs) {
			int id = Integer.parseInt(l.getId());
			n = Math.max(n, id);
		}
		int id = n + 1;
		libs.add(new XmlLib(id, value));
		return id;
	}

	/**
	 * remove the given text id from libs
	 *
	 * @param value
	 */
	public void libDelete(int value) {
		LOG.trace(TT + "libDelete(" + "id=" + value + ")");
		for (XmlLib l : libs) {
			int id = Integer.parseInt(l.getId());
			if (id == value) {
				libs.remove(l);
				break;
			}
		}
	}

	public class XmlLib {

		private String id = "", text = "";

		public XmlLib(int id, String text) {
			this("" + id, text);
		}

		public XmlLib(String id, String text) {
			this.id = id;
			this.text = text;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getText() {
			return text;
		}

		public void setText(String value) {
			this.text = value;
		}

		public String toString() {
			return id + "," + text;
		}

		public String toXml() {
			StringBuilder b = new StringBuilder(XmlUtil.indent(2));
			b.append("<lib ");
			b.append("id=\"").append(id).append("\"> ")
					.append("<![CDATA[").append(text).append("]]>")
					.append("</lib>\n");
			return b.toString();
		}

	}

}
