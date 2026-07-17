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

import app.print.PrintItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import tools.LOG;

/**
 * Gère exclusivement les configurations de la mise en page print.
 */
public class XmlPrint {

	private static final String TT = "XmlPrint.";

	private final Xml xml;
	private String format = "A4";
	private String orientation = "portrait";
	private final List<XmlLib> libs = new ArrayList<>();

	public XmlPrint(Xml xml) {
		this.xml = xml;
	}

	public void load() {
		LOG.trace(TT + "load()");
		NodeList node = xml.getDocument().getElementsByTagName("print");
		format = xml.attributeGet((Element) node.item(0), "format");
		orientation = xml.attributeGet((Element) node.item(0), "orient");
	}

	/**
	 * get the size (rows, cols) depending on orientation
	 *
	 * @return
	 */
	public String sizeGet() {
		return (isPortrait() ? "5,3" : "3,5");
	}

	/**
	 * get the format (may be A4)
	 *
	 * @return
	 */
	public String formatGet() {
		return format;
	}

	/**
	 * get orientation
	 *
	 * @return
	 */
	public String orientationGet() {
		LOG.trace(TT + "orientationGet() = " + orientation);
		return orientation;
	}

	/**
	 * set orientation (may be portrait or landscape)
	 *
	 * @param value
	 */
	public void orientationSet(String value) {
		if (value.equalsIgnoreCase("landscape")) {
			this.orientation = value;
		} else {
			this.orientation = "portrait";
		}
	}

	/**
	 * check if orientation is portrait
	 *
	 * @return
	 */
	public boolean isPortrait() {
		return orientation.equals("portrait");
	}

	/**
	 * Find a photo file name in the photos node by the given ID
	 *
	 * @param id
	 * @return
	 */
	public String findPhoto(int id) {
		NodeList nodes = xml.getDocument().getElementsByTagName("item");
		for (int i = 0; i < nodes.getLength(); i++) {
			Element el = (Element) nodes.item(i);
			String sid = xml.attributeGet(el, "id");
			if (!sid.isEmpty() && Integer.parseInt(sid) == id) {
				return xml.attributeGet(el, "file");
			}
		}
		return null;
	}

	/**
	 * load the library of text Key: ID of the text, Value: HTML content (in a CDATA)
	 */
	private Map<Integer, String> loadTextLibrary() {
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
	 * Load all pages as List of XmlPage
	 *
	 * @return
	 */
	public List<XmlPrintPage> printPageGetAll() {
		List<XmlPrintPage> list = new ArrayList<>();
		Map<Integer, String> textLibrary = loadTextLibrary();
		NodeList nodes = xml.getDocument().getElementsByTagName("page");
		for (int i = 0; i < nodes.getLength(); i++) {
			Element elPage = (Element) nodes.item(i);
			String idPage = xml.attributeGet(elPage, "id");
			if (!idPage.isEmpty()) {
				XmlPrintPage page = new XmlPrintPage(idPage);
				NodeList cells = elPage.getElementsByTagName("cell");
				for (int j = 0; j < cells.getLength(); j++) {
					Element elCell = (Element) cells.item(j);
					PrintItem cell = new PrintItem();
					String type = xml.attributeGet(elCell, "type");
					cell.typeSet(type);
					String posRaw = xml.attributeGet(elCell, "pos");
					if (!posRaw.isEmpty()) {
						String[] tokens = posRaw.split(",");
						if (tokens.length == 3) {
							cell.cellIdSet(Integer.parseInt(tokens[0].trim()));
							cell.spanHorizontalSet(Integer.parseInt(tokens[1].trim()));
							cell.spanVerticalSet(Integer.parseInt(tokens[2].trim()));
						}
					}
					String refStr = xml.attributeGet(elCell, "ref");
					if (!refStr.isEmpty()) {
						int refId = Integer.parseInt(refStr);
						if ("p".equals(type)) {
							cell.photoIdSet(refId);
							cell.photoFileSet(findPhoto(refId));
						} else if ("t".equals(type)) {
							cell.textIdSet(refId);
							cell.textSet(textLibrary.get(refId));
						}
					}
					page.callAdd(cell);
				}
				list.add(page);
			}
		}
		return list;
	}

	/**
	 * load all text as List of XmlLib
	 *
	 * @return
	 */
	public List<XmlLib> loadLibs() {
		libs.clear();
		NodeList nodes = xml.getRoot().getElementsByTagName("lib");
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				Element child = (Element) nodes.item(i);
				String text = child.getTextContent();
				XmlLib p = new XmlLib(child.getAttribute("id"), text);
				libs.add(p);
			}
		}
		return libs;
	}

	/**
	 * get the XmlLib for the given index
	 *
	 * @param i
	 * @return
	 */
	public XmlLib getLib(int i) {
		if (i < libs.size()) {
			return libs.get(i);
		}
		return null;
	}

}
