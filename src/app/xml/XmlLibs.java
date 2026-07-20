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

	private final Xml xml;
	List<XmlLib> libs = new ArrayList<>();

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
				libMap.put(Integer.parseInt(idStr), el.getTextContent().trim());
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
	 * Met à jour le contenu d'une bibliothèque de texte par son identifiant.
	 *
	 * * @param id l'identifiant du texte à modifier
	 * @param id
	 * @param newText le nouveau texte HTML provenant de SHEF
	 */
	public void libUpdate(int id, String newText) {
		//todo à réécrire
		NodeList libNodes = xml.getDocument().getElementsByTagName("lib");
		for (int i = 0; i < libNodes.getLength(); i++) {
			Element el = (Element) libNodes.item(i);
			String idStr = xml.attributeGet(el, "id");

			if (!idStr.isEmpty() && Integer.parseInt(idStr) == id) {
				// Remplacement du contenu par le nouveau texte épuré/HTML
				el.setTextContent(newText);
				return;
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

}
