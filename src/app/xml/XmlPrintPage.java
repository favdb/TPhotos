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
import java.util.List;

/**
 * manage the page
 *
 * @author favdb
 */
public class XmlPrintPage {

	private String id;
	private List<PrintItem> cells = new ArrayList<>();

	public XmlPrintPage(String idPage) {
		this.id = idPage;
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
	public List<PrintItem> cellsGet() {
		return cells;
	}

	/**
	 * add a cell to the page
	 *
	 * @param cell
	 */
	public void callAdd(PrintItem cell) {
		this.cells.add(cell);
	}

	@Override
	public String toString() {
		return String.format("id=%s nb_cells=%d", id, cells.size());
	}

}
