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

import static app.print.Print.*;
import app.print.PrintCell;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tools.LOG;

/**
 * Gère exclusivement les configurations de la mise en page print.
 */
public class XmlPrint {

	private static final String TT = "XmlPrint.";

	private final Xml xml;
	private String format = "A4", orientation = PORTRAIT;
	private final List<XmlPrintPage> pages = new ArrayList<>();
	private final List<PrintCell> cells = new ArrayList<>();

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public XmlPrint(Xml xml) {
		this.xml = xml;
		load();
	}

	public void traceCells() {
		LOG.trace(TT + "traceCells()");
		for (PrintCell c : cells) {
			LOG.trace(c.toString());
		}
	}

	public void load() {
		//LOG.trace(TT + "load()");
		NodeList node = xml.getDocument().getElementsByTagName("print");
		format = xml.attributeGet((Element) node.item(0), "format");
		orientation = xml.attributeGet((Element) node.item(0), "orient");

		// 1. Initialise la liste globale unique de référence avec TOUTES les instances possibles
		loadCells();

		// 2. Recréation des structures de pages (sans données internes dupliquées)
		pages.clear();
		NodeList pagesnode = xml.getDocument().getElementsByTagName("page");

		for (int i = 0; i < pagesnode.getLength(); i++) {
			Node pageNode = pagesnode.item(i);
			int pageId = XmlUtil.integerGet(pageNode, "id");
			pages.add(new XmlPrintPage(xml, XmlUtil.stringGet(pageNode, "id")));

			NodeList cellnodes = ((Element) pageNode).getElementsByTagName("cell");
			for (int ii = 0; ii < cellnodes.getLength(); ii++) {
				Element el = (Element) cellnodes.item(ii);

				String type = XmlUtil.stringGet(el, "type");
				int ref = XmlUtil.integerGet(el, "ref");
				int page = XmlUtil.integerGet(el, "page");
				String pos = XmlUtil.stringGet(el, "pos");

				// 3. Recherche de la VRAIE cellule de référence déjà existante
				for (PrintCell target : cells) {
					int cId = target.isPhoto() ? target.photoIdGet() : target.textIdGet();

					// Si le type et l'ID métier correspondent, on lui injecte ses coordonnées
					if ((target.typeGet().equals(type) || target.typeGet().startsWith(type)) && cId == ref) {
						target.pageSet(pageId);
						target.posSet(pos);
						break;
					}
				}
			}
		}
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
	 * get the fpaper ormat (may be A4 or A3)
	 *
	 * @return
	 */
	public String formatGet() {
		return format;
	}

	/**
	 * set the paper format
	 *
	 * @param format (may be A4 or A3)
	 */
	public void formatSet(String format) {
		this.format = (format.equals("A3") ? "A3" : "A4");
	}

	/**
	 * get paper orientation
	 *
	 * @return
	 */
	public String orientationGet() {
		//LOG.trace(TT + "orientationGet() = " + orientation);
		return orientation;
	}

	/**
	 * set paper orientation (may be portrait or landscape)
	 *
	 * @param value
	 */
	public void orientationSet(String value) {
		this.orientation = (value.equalsIgnoreCase(LANDSCAPE) ? LANDSCAPE : PORTRAIT);
	}

	/**
	 * check if orientation is PORTRAIT
	 *
	 * @return
	 */
	public boolean isPortrait() {
		return orientation.equals(PORTRAIT);
	}

	/**
	 * Find a photo file name in the photos node by the given ID
	 *
	 * @param id
	 * @return
	 */
	public String photoFind(int id) {
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
	 * load all cell from album and print
	 *
	 */
	public void loadCells() {
		LOG.trace(TT + "loadCells()");
		cells.clear();
		List<XmlAlbumPhoto> xphotos = xml.albumGet().photosAllGet();
		for (XmlAlbumPhoto x : xphotos) {
			int cellid = Integer.parseInt(x.idGet());
			cells.add(new PrintCell(cellid, cellid, x.fileGet(), x.commentGet(), 0));
		}
		int nid = 1;
		for (XmlLib x : xml.libsGet().getAll()) {
			PrintCell cell = new PrintCell(nid++, x.getText(), 0);
			cells.add(cell);
		}
	}

	public PrintCell photoCellGet(String id) {
		PrintCell pc = new PrintCell();
		XmlAlbum album = xml.albumGet();
		NodeList nodes = xml.rootGet().getElementsByTagName("item");
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				Element child = (Element) nodes.item(i);
				if (xml.attributeGet(child, "id").equals(id)) {
					pc.cellIdSet(XmlUtil.integerGet(child, "id"));
					pc.photoFileSet(XmlUtil.stringGet(child, "file"));
					pc.commentSet(XmlUtil.stringGet(child, "comment"));
					return pc;
				}
			}
		}
		return null;
	}

	public void updateCell(PrintCell target, int page, String pos) {
		/*LOG.trace(TT + "updateCell(target=" + target.typeGet() + "." + target.idGet()
				+ ", page=" + page
				+ ", pos=" + pos + ")");*/
		String targetType = target.typeGet();
		int targetId = target.idGet();
		for (PrintCell c : cells) {
			if (c.typeGet().equals(targetType) && c.idGet() == targetId) {
				c.pageSet(page);
				c.posSet(pos);
				return;
			}
		}
	}

	public String toXml() {
		StringBuilder b = new StringBuilder();
		//open print tag
		b.append(XmlUtil.indent(1)).append("<print ")
				.append(XmlUtil.attributXml("format", format))
				.append(XmlUtil.attributXml("orient", orientation))
				.append(XmlUtil.attributXml("size", sizeGet()))
				.append(">\n");
		//save all pages
		b.append(XmlUtil.indent(2)).append("<pages>\n");
		int page = 0;
		for (PrintCell c : cells) {
			if (c.pageGet() == 0) {
				continue;
			}
			if (c.pageGet() != page) {
				if (page > 0) {
					b.append(XmlUtil.indent(3)).append("</page>\n");
				}
				page = c.pageGet();
				b.append(XmlUtil.indent(3)).append("<page id=\"" + page + "\">\n");
			}
			b.append(c.toXml());
		}
		if (page > 0) {
			b.append(XmlUtil.indent(3)).append("</page>\n");
		}
		b.append(XmlUtil.indent(2)).append("</pages>\n");
		//close print tag
		b.append(XmlUtil.indent(1)).append("</print>\n");
		return b.toString();
	}

	public List<XmlPrintPage> getPages() {
		return pages;
	}

	public List<PrintCell> getCells() {
		return cells;
	}

	public void addCell(PrintCell cell) {
		cells.add(cell);
	}

}
