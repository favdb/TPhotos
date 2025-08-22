/*
 * Copyright (C) 2022 favdb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tools.file;

import i18n.I18N;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import tools.ListUtil;

/**
 * class for a new File filter
 *
 * @author favdb
 */
public class FileFilter extends javax.swing.filechooser.FileFilter {

	private String desc;

	public void setDescription(String desc) {
		this.desc = desc;
	}

	public enum TYPE {
		BOOK, DOC, DOCX, ODT,
		EPUB, IMG, PNG, JPEG, PROP, PROPERTIES,
		BACKUP, XML, OSBK, DB;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	private final static String[][] DESC = {
		{"book", "docx, odt, html, txt, epub", I18N.getMsg("file.type.book")},
		{"doc", "docx, odt, html, txt", I18N.getMsg("file.type.doc")},
		{"odt", "odt", I18N.getMsg("file.type.odt")},
		{"docx", "docx", I18N.getMsg("file.type.docx")},
		{"html", "html", I18N.getMsg("file.type.html")},
		{"txt", "txt", I18N.getMsg("file.type.txt")},
		{"tex", "tex", I18N.getMsg("file.type.tex")},
		{"epub", "epub", I18N.getMsg("file.type.epub")},
		{"img", "png, jpg, jpeg, bmp, img", I18N.getMsg("file.type.img")},
		{"png", "png", I18N.getMsg("file.type.png")},
		{"jpeg", "jpg, jpeg", I18N.getMsg("file.type.jpeg")},
		{"prop", "properties", I18N.getMsg("file.type.prop")},
		{".properties", "properties", I18N.getMsg("file.type.prop")},
		{"backup", "backup", I18N.getMsg("file.type.backup")},
		{"xml", "xml", I18N.getMsg("file.type.xml")},
		{"osbk", "osbk,osbz", I18N.getMsg("file.type.osbk")},
		{"db", "h2.db, mv.db, osbk", I18N.getMsg("file.type.osbk")}
	};

	private static String getEXT(String t) {
		for (String[] s : DESC) {
			if (s[0].equalsIgnoreCase(t)) {
				List<String> sx = new ArrayList<>();
				String[] ee = s[1].split(",");
				for (String see : ee) {
					sx.add("*." + see.trim());
				}
				return ListUtil.join(sx, ", ");
			}
		}
		return "???";
	}

	private final String type;

	public FileFilter(TYPE ext) {
		type = ext.toString().trim();
	}

	public FileFilter(String ext) {
		type = ext.trim();
	}

	@Override
	public boolean accept(File file) {
		if (file.isDirectory()) {
			return true;
		}
		for (String[] x : DESC) {
			if (!x[0].equalsIgnoreCase(type)) {
				continue;
			}
			String[] z = x[1].split(",");
			for (String sz : z) {
				if (file.getName().endsWith("." + sz.trim())) {
					return (true);
				}
			}
		}
		return false;
	}

	@Override
	public String getDescription() {
		if (desc != null && !desc.isEmpty()) {
			return desc;
		}
		for (String[] s : DESC) {
			if (s[0].equalsIgnoreCase(type)) {
				return s[2] + " (" + getEXT(type) + ")";
			}
		}
		return "???";
	}

	public String getType() {
		return type;
	}

}
