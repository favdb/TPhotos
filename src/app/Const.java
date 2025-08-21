/*
 * Copyright (C) 2024 favdb
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
package app;

import i18n.I18N;

/**
 *
 * @author favdb
 */
public class Const {

	static String getVersion() {
		return TPhotos.VERSION.toString();
	}

	public static String getFullName() {
		return TPhotos.NAME.toString() + " " + getVersion();
	}

	public static String getName() {
		return TPhotos.NAME.toString();
	}

	public enum TPhotos {
		NAME("TPhotos"),
		VERSION_MAJOR("2"),
		VERSION_MINOR("03"),
		VERSION(VERSION_MAJOR + "." + VERSION_MINOR);
		final private String text;

		private TPhotos(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public static final Integer FONT_SIZE = 12;

	/**
	 * Look and Feel values
	 */
	public enum LookAndFeel {
		LIGHT, ADVANCED, DARK, DRACULA;

		public String getI18N() {
			return I18N.getMsg("preferences.laf." + name().toLowerCase());
		}
	}

}
