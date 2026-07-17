/*
 * Copyright (C) 2021 favdb
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
package resources.icons;

import javax.swing.Icon;

/**
 * utility class for Icons
 *
 * @author favdb
 */
public class ICONS {

	private ICONS() {
		// empty
	}

	public enum K {
		AR_RIGHT,
		CALENDAR,
		CANCEL,
		CLEAR,
		COGS,
		COPYRIGHT,
		ERROR,
		EXIT,
		F_EXPORT, F_NEW, F_OPEN, F_PRINT,
		FILE,
		FOLDER,
		FOLDER_OPEN,
		HELP,
		MINUS, PLUS,
		NAV_NEXT, NAV_PREV,
		OK,
		OPTIONS,
		PHOTO,
		PIC,
		PREVIEW,
		REFRESH,
		TPHOTOS,
		UNKNOWN,
		NONE, EMPTY,
		SHEF_SORT,
		SHEF_EDIT_UNDO,
		SHEF_EDIT_REDO,
		SHEF_EDIT_COPY,
		SHEF_EDIT_CUT,
		SHEF_EDIT_PASTE,
		SHEF_FIND,
		SHEF_COLOR,
		SHEF_PENCIL,
		SHEF_CHAR_ENDNOTE,
		SHEF_FONTSIZE,
		SHEF_LINK,
		SHEF_IMAGE,
		SHEF_CATEGORIES,
		SHEF_TABLE,
		SHEF_CHAR_UNICODE,
		SHEF_AL_RIGHT,
		SHEF_AL_LEFT,
		SHEF_AL_CENTER,
		SHEF_AL_JUSTIFY,
		SHEF_LIST_ORDERED,
		SHEF_LIST_UNORDERED,
		SHEF_HTML_HR,
		SHEF_HIGHLIGHTER,
		SHEF_TX_BOLD,
		SHEF_TX_ITALIC,
		SHEF_TX_UNDERLINE,
		SHEF_TX_STRIKE,
		SHEF_TX_SUBSCRIPT,
		SHEF_TX_SUPERSCRIPT,
		SHEF_HTML_BR,
		SHEF_SOURCE,
		SHEF_HTML,
		SHEF_ZOOM_IN, SHEF_ZOOM_OUT;

		@Override
		public String toString() {
			return name().toLowerCase();
		}

		public Icon getIcon() {
			return IconUtil.getIconSmall(name());
		}
	}

	public static K getIconKey(String str) {
		for (K key : K.values()) {
			if (key.toString().equalsIgnoreCase(str)) {
				return key;
			}
		}
		return K.UNKNOWN;
	}

}
