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

/**
 *
 * @author favdb
 */
public class XmlLib {

	private String id = "", text = "";

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

	public void setFile(String value) {
		this.text = value;
	}

	public String toString() {
		return id + "," + text;
	}

	public String toXml() {
		StringBuilder b = new StringBuilder("<lib ");
		b.append("id=\"").append(id).append("\"> ")
				.append("<![CDATA[").append(text).append("]]>")
				.append("</lib>\n");
		return b.toString();
	}

}
