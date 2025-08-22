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
		NAV_PREV,
		NAV_LEFT_GRAYED,
		NAV_NEXT,
		NAV_RIGHT_GRAYED,
		CALENDAR,
		CANCEL,
		CLEAR,
		COGS,
		COPYRIGHT,
		ERROR,
		EXIT,
		F_EXPORT, F_NEW, F_OPEN,
		FILE,
		FOLDER,
		FOLDER_OPEN,
		TPHOTOS,
		HELP,
		MINUS, PLUS,
		OK,
		OPTIONS,
		PHOTO,
		PIC,
		UNKNOWN,
		NONE;

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
