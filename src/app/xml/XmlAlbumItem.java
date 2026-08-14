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

import app.App;
import java.io.File;

/**
 *
 * @author favdb
 */
public class XmlAlbumItem {

	private static final String TT = "XmlAlbumItem.";

	/**
	 * data: <ul>
	 * <li>id: identity</li>
	 * <li>photo: file name (without photo directory)</li>
	 * <li>comment: text comment of the photo</li>
	 * </ul>
	 */
	private String id = "", photo = "", comment = "";

	public XmlAlbumItem(String id, String file, String comment) {
		this.id = id;
		this.photo = file;
		this.comment = comment;
	}

	public String idGet() {
		return id;
	}

	public void idSet(String id) {
		this.id = id;
	}

	/**
	 * get file name of the photo
	 *
	 * @return
	 */
	public String photoGet() {
		return photo;
	}

	/**
	 * set file name of the photo
	 *
	 * @param file
	 */
	public void photoSet(String file) {
		this.photo = file;
	}

	/**
	 * get the File for the photo (prefix with photosDir if needed)
	 *
	 * @return
	 */
	public File fileGet() {
		File f = new File(photo);
		if (!f.exists()) {
			f = new File(App.pref.photosDirGet(), photo);
		}
		return f;
	}

	/**
	 * get the comment
	 *
	 * @return
	 */
	public String commentGet() {
		return comment;
	}

	/**
	 * set the comment
	 *
	 * @param comment
	 */
	public void commentSet(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		return id + "," + photo + "," + comment;
	}

	public String toXml() {
		StringBuilder b = new StringBuilder();
		String photosDir = App.pref.photosDirGet();
		String relativePath = photo;
		if (photosDir != null && !photosDir.isEmpty()) {
			File photoFile = new File(photo);
			File baseDir = new File(photosDir);
			if (photoFile.isAbsolute() || photo.startsWith(photosDir)) {
				String absPhoto = photoFile.getAbsolutePath();
				String absBase = baseDir.getAbsolutePath();
				if (absPhoto.startsWith(absBase)) {
					relativePath = absPhoto.substring(absBase.length());
					if (relativePath.startsWith(File.separator)) {
						relativePath = relativePath.substring(File.separator.length());
					}
				} else {
					relativePath = photoFile.getName();
				}
			}
		} else {
			relativePath = new File(photo).getName();
		}
		b.append(XmlUtil.INDENT).append(XmlUtil.INDENT).append("<item ");
		b.append("id=\"").append(id).append("\" ")
				.append("file=\"").append(relativePath).append("\" ")
				.append("comment=\"").append(XmlUtil.escapeXml(comment)).append("\" ")
				.append("/>\n");
		return b.toString();
	}

	public File photoFile() {
		File f = new File(photo);
		if (!f.exists()) {
			f = new File(App.pref.photosDirGet());
		}
		return f;
	}
}
