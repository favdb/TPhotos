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

import java.io.File;

/**
 *
 * @author favdb
 */
public class XmlAlbumPhoto {

	private String id = "", file = "", comment = "";

	public XmlAlbumPhoto(String id, String file, String comment) {
		this.id = id;
		this.file = file;
		this.comment = comment;
	}

	public String idGet() {
		return id;
	}

	public void idSet(String id) {
		this.id = id;
	}

	public String fileGet() {
		return file;
	}

	public void fileSet(String file) {
		this.file = file;
	}

	public String commentGet() {
		return comment;
	}

	public void commentSet(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		return id + "," + file + "," + comment;
	}

	public String toXml() {
		StringBuilder b = new StringBuilder();
		File ff = new File(file);
		b.append(XmlUtil.INDENT).append(XmlUtil.INDENT).append("<item ");
		b.append("id=\"").append(id).append("\" ")
				.append("file=\"").append(ff.getName()).append("\" ")
				.append("comment=\"").append(comment).append("\" ")
				.append("/>\n");
		return b.toString();
	}
}
