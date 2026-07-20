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

import app.print.PrintCell;
import java.util.ArrayList;
import java.util.List;

/**
 * manage the page
 *
 * @author favdb
 */
public class XmlPrintPage {

	private String id;
	private final Xml xml;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public XmlPrintPage(Xml xml, String idPage) {
		this.xml = xml;
		this.id = idPage;
		load();
	}

	public void load() {
		//empty all data loaded as PrintCell
	}

	/**
	 * get the ID of the page
	 *
	 * @return
	 */
	public String idGet() {
		return id;
	}

	/**
	 * set the ID of the page
	 *
	 * @param id
	 */
	public void idSet(String id) {
		this.id = id;
	}

	/**
	 * get all cells of the page
	 *
	 * @return
	 */
	public List<PrintCell> cellsGet() {
		List<PrintCell> l = new ArrayList<>();
		for (PrintCell c : xml.printGet().getCells()) {
			String sid = Integer.toString(c.pageGet());
			if (id.equals(sid)) {
				l.add(c);
			}
		}
		return l;
	}

	/**
	 * add a cell to the page
	 *
	 * @param cell
	 */
	public void cellAdd(PrintCell cell) {
		if (cell == null) {
			return;
		}

		// Assigner la cellule à cette page modifie sa propriété interne.
		// cellsGet() la détectera automatiquement au prochain rafraîchissement.
		cell.pageSet(Integer.parseInt(id));
	}

	@Override
	public String toString() {
		return String.format("page id=%s nb_cells=%d", id, cellsGet().size());
	}

	public String toXml() {
		StringBuilder b = new StringBuilder();
		b.append(XmlUtil.indent(3)).append("<page ")
				.append(XmlUtil.attributXml("id", id))
				.append(">\n");
		for (PrintCell p : cellsGet()) {
			b.append(p.toXml());
		}
		b.append(XmlUtil.indent(3)).append("</page>\n");
		return b.toString();
	}

}
