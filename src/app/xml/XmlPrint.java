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
import app.tools.LOG;
import app.xml.XmlLibs.XmlLib;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Gère exclusivement les configurations de la mise en page print.
 */
public class XmlPrint {

	private static final String TT = "XmlPrint.";

	private final Xml xml;
	private String format = "A4", orientation = PORTRAIT;
	private final List<XmlPrintPage> pages = new ArrayList<>();
	private final List<XmlPrintCell> cells = new ArrayList<>();
	private boolean numpage;
	private String gridSize = "3,5";
	private int grid[] = {3, 5};
	private int margins[] = {0, 0, 0, 0, 0, 0};
	private boolean marginPhoto = false;//photo margin
	private boolean binding = false;//center binding margin

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public XmlPrint(Xml xml) {
		this.xml = xml;
		load();
	}

	public void traceCells() {
		LOG.trace(TT + "traceCells()");
		for (XmlPrintCell c : cells) {
			LOG.trace(c.toString());
		}
	}

	public void load() {
		//LOG.trace(TT + "load()");
		NodeList node = xml.getDocument().getElementsByTagName("print");
		if (node.getLength() > 0) {
			//get paper format, orientation, numpage
			Element item0 = (Element) node.item(0);
			format = xml.attributeGet(item0, "format");
			orientation = xml.attributeGet(item0, "orient");
			numpage = xml.attributeGet(item0, "numpage").equalsIgnoreCase("true");
			String str = xml.attributeGet(item0, "margins");
			marginsSet(str);
			gridSet(xml.attributeGet(item0, "size"));

			// 1. Initialize global list of XmlPrintCell
			loadCells();

			// 2. load pages structures (without dupplicated internal data)
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
					String ref = XmlUtil.stringGet(el, "ref");
					int page = XmlUtil.integerGet(el, "page");
					String pos = XmlUtil.stringGet(el, "pos");
					String rot = XmlUtil.stringGet(el, "rot");
					String dec = XmlUtil.stringGet(el, "dec");

					// 3. Search real existing cell
					for (XmlPrintCell target : cells) {
						String cId = target.isPhoto() ? target.photoIdGet() : target.textIdGet();
						if ((target.typeGet().equals(type)
								|| target.typeGet().startsWith(type)) && cId.equals(ref)) {
							target.pageSet(pageId);
							target.posSet(pos);
							break;
						}
					}
				}
			}
		}
	}

	public int[] gridGet() {
		String z[] = gridSize.split(",");
		try {
			grid[0] = Integer.parseInt(z[0]);
			grid[1] = Integer.parseInt(z[1]);
		} catch (NumberFormatException x) {

		}
		return grid;
	}

	public String grid2String() {
		return String.format("%d,%d", grid[0], grid[1]);
	}

	public void gridSet(int cx[]) {
		grid[0] = Math.max(1, cx[0]);
		grid[1] = Math.max(1, cx[1]);
	}

	public void gridSet(String val) {
		String cx[] = val.split(",");
		try {
			grid[0] = Integer.parseInt(cx[0]);
			grid[1] = Integer.parseInt(cx[1]);
		} catch (NumberFormatException ex) {
		}
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
		//LOG.trace(TT + "orientationSet(value=" + value + ")");
		this.orientation = (value.equalsIgnoreCase(LANDSCAPE) ? LANDSCAPE : PORTRAIT);
	}

	public void orientationSet(int value) {
		//LOG.trace(TT + "orientationSet(value=" + value + ")");
		this.orientation = (value == 1 ? LANDSCAPE : PORTRAIT);
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
		//LOG.trace(TT + "loadCells()");
		cells.clear();
		List<XmlAlbumItem> xphotos = xml.albumGet().itemsGet();
		for (XmlAlbumItem x : xphotos) {
			int cellid = Integer.parseInt(x.idGet());
			cells.add(new XmlPrintCell(cellid, x.idGet(), x.photoGet(),
					x.commentGet(), 0));
		}
		int nid = 1;
		for (XmlLib x : xml.libsGet().getAll()) {
			XmlPrintCell cell = new XmlPrintCell(nid++, x.getId(), x.getText(), 0);
			cells.add(cell);
		}
	}

	public XmlPrintCell photoCellGet(String id) {
		XmlPrintCell pc = new XmlPrintCell();
		XmlAlbum album = xml.albumGet();
		NodeList nodes = xml.rootGet().getElementsByTagName("item");
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				Element child = (Element) nodes.item(i);
				if (xml.attributeGet(child, "id").equals(id)) {
					pc.cellIdSet(XmlUtil.integerGet(child, "id"));
					pc.photoNameSet(XmlUtil.stringGet(child, "file"));
					pc.commentSet(XmlUtil.stringGet(child, "comment"));
					return pc;
				}
			}
		}
		return null;
	}

	public void updateCell(XmlPrintCell target, int page, String pos) {
		/*LOG.trace(TT + "updateCell(target=" + target.typeGet() + "." + target.idGet()
				+ ", page=" + page
				+ ", pos=" + pos + ")");*/
		String targetType = target.typeGet();
		int targetId = target.idGet();
		for (XmlPrintCell c : cells) {
			if (c.typeGet().equals(targetType) && c.idGet() == targetId) {
				c.pageSet(page);
				c.posSet(pos);
				return;
			}
		}
	}

	public String commonGet() {
		StringBuilder b = new StringBuilder();
		b.append(XmlUtil.indent(1)).append("<print ")
				.append(XmlUtil.attributXml("format", format))
				.append(XmlUtil.attributXml("orient", orientation))
				.append(XmlUtil.attributXml("size", grid2String()))
				.append(XmlUtil.attributXml("numpage", numpage))
				.append(XmlUtil.attributXml("margins", margins2String()))
				.append(">\n");
		return b.toString();
	}

	public String toXml() {
		StringBuilder b = new StringBuilder(commonGet());
		XmlPrintCell.sortByPage(cells);
		b.append(XmlUtil.indent(2)).append("<pages>\n");
		int page = 0;
		for (XmlPrintCell c : cells) {
			if (c.pageGet() == 0) {
				continue;
			}
			if (c.pageGet() != page) {
				if (page > 0) {
					b.append(XmlUtil.indent(3)).append("</page>\n");
				}
				page = c.pageGet();
				b.append(XmlUtil.indent(3)).append("<page id=\"").append(page).append("\">\n");
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

	public List<XmlPrintCell> getCells() {
		return cells;
	}

	/**
	 * add the given cell
	 *
	 * @param cell
	 */
	public void cellAdd(XmlPrintCell cell) {
		//LOG.trace(TT+"cellAdd(cell="+cell.toString()+")");
		cells.add(cell);
	}

	/**
	 * remove the given cell
	 *
	 * @param cell
	 */
	public void cellDelete(XmlPrintCell cell) {
		LOG.trace(TT + "cellDelete(cell=" + cell.toString() + ")");
		cells.remove(cell);
	}

	/**
	 * reload all cells and pages
	 */
	public void reloadAll() {
		//LOG.trace(TT + "reloadAll()");
		cells.clear();
		pages.clear();
		loadCells();
	}

	/**
	 * clear all pages and XmlPrintCell
	 */
	public void pagesClear() {
		cells.clear();
		pages.clear();
	}

	public void pageClear(int page) {
		List<XmlPrintCell> ls = new ArrayList<>();
		for (XmlPrintCell p : cells) {
			if (p.pageGet() == page) {
				ls.add(p);
			}
		}
		for (XmlPrintCell p : ls) {
			cells.remove(p);
		}
	}

	/**
	 * set boolean value for page number to print
	 *
	 * @param selected
	 */
	public void numpageSet(boolean value) {
		this.numpage = value;
	}

	/**
	 * set boolean value for page number to print
	 *
	 * @param selected
	 */
	public boolean numpageGet() {
		return this.numpage;
	}

	/**
	 * get the margins as array of int
	 *
	 * @return
	 */
	public int[] marginsIntGet() {
		return margins;
		/*int[] m = {0, 0, 0, 0, 0, 0};
		try {
			if (marginsString != null && !marginsString.isEmpty()) {
				String p[] = marginsString.split(",");
				m[0] = Integer.parseInt(p[0]);
				m[1] = Integer.parseInt(p[1]);
				m[2] = Integer.parseInt(p[2]);
				m[3] = Integer.parseInt(p[3]);
				if (p.length > 4) {
					m[4] = Integer.parseInt(p[4]);
				}
				if (p.length > 5) {
					m[5] = Integer.parseInt(p[5]);
				}
			}
		} catch (NumberFormatException ex) {

		}
		return m;*/
	}

	public String margins2String() {
		return String.format("%d,%d,%d,%d,%d,%d",
				margins[0], margins[1], margins[2], margins[3],//0-3=sheet margins
				margins[4], margins[5]);//4=central binding, 5=photo margin
	}

	/**
	 * set the margins 0-3=sheet margins, 4=central binding, 5=photo margin
	 *
	 * @param margins
	 */
	public void marginsSet(int v[]) {
		try {
			margins[0] = v[0];
			margins[1] = v[1];
			margins[2] = v[2];
			margins[3] = v[3];
			margins[4] = v[4];
			margins[5] = v[5];
		} catch (Exception ex) {

		}
	}

	/**
	 * set the margins 0-3=sheet margins, 4=central binding, 5=photo margin from the given
	 * String
	 *
	 * @param margins
	 */
	public void marginsSet(String vx) {
		margins = new int[]{0, 0, 0, 0, 0, 0};
		try {
			String v[] = vx.split(",");
			margins[0] = Integer.parseInt(v[0]);
			margins[1] = Integer.parseInt(v[1]);
			margins[2] = Integer.parseInt(v[2]);
			margins[3] = Integer.parseInt(v[3]);
			margins[4] = Integer.parseInt(v[4]);
			margins[5] = Integer.parseInt(v[5]);
		} catch (Exception ex) {

		}
	}

	public boolean centralGet() {
		try {
			return margins[4] > 0;
		} catch (Exception ex) {
			return false;
		}
	}

	public boolean gapGet() {
		try {
			return margins[5] > 0;
		} catch (Exception ex) {
			return false;
		}
	}

}
