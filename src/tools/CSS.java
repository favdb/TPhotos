package tools;


import app.App;

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
/**
 *
 * @author favdb
 */
public class CSS {

	public static String forEditor() {
		StringBuilder b = new StringBuilder();
		b.append("body {\n")
				.append("font-family: ")
				.append(App.fontGet().getFamily())
				.append(";\n")
				.append("font-size: ")
				.append(App.fontGet().getSize())
				.append(";\n")
				.append("}\n");
		b.append("em {\n")
				.append("background: yellow;")
				.append("font-style: normal;")
				.append("}\n");
		return b.toString();
	}

}
