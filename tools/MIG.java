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
package tools;

import java.awt.Point;

/**
 * utility class for MigLayout (getting MigLayout parameters from static strings)
 *
 * @author favdb
 */
public final class MIG {

	/**
	 * MIG is a utility class
	 */
	private MIG() {
		// empty
	}

	/**
	 * String contants
	 */
	public static final String BOTTOM = "bottom",
			CENTER = "center",
			FILL = "fill",
			FILLX = "fillx",
			FLOWY = "flowy",
			FLOWX = "flowx",
			GAP = "gap ",
			GAP0 = "gap 0 0",
			GAP1 = "gap 1 1",
			GROW = "grow",
			GROWX = "growx",
			GROWY = "growy",
			HIDEMODE2 = "hidemode 2",
			HIDEMODE3 = "hidemode 3",
			INS0 = "ins 0",
			INS1 = "ins 1",
			LEFT = "left",
			MIDDLE = "aly center",
			NEWLINE = "newline",
			PUSH = "push",
			PUSHX = "pushx",
			RIGHT = "right",
			SG = "sg",
			SHRINK = "shrink",
			SKIP = "skip",
			SPAN = "span",
			SPANX = "spanx",
			SPANX2 = "spanx 2",
			SPLIT = "split",
			SPLIT2 = "split 2",
			TOP = "top",
			WRAP = "wrap",
			WRAP1 = "wrap 1";

	/**
	 * get parameters for MigLayout
	 *
	 * @param mig
	 * @param migs
	 * @return
	 */
	public static String get(String mig, String... migs) {
		StringBuilder b = new StringBuilder(mig);
		if (migs != null) {
			for (String s : migs) {
				b.append(",").append(s);
			}
		}
		return b.toString();
	}

	/**
	 * get pos parameter from coordinates
	 *
	 * @param x
	 * @param y
	 * @return
	 */
	public static String getPos(int x, int y) {
		return "pos " + x + " " + y;
	}

	/**
	 * get pos parameter from a Point
	 *
	 * @param px
	 * @return
	 */
	public static String getPos(Point px) {
		return getPos(px.x, px.y);
	}

	/**
	 * get pos String from a Point
	 *
	 * @param x
	 * @param y
	 * @return
	 */
	public static String posToString(int x, int y) {
		return "pos " + x + " " + y;
	}

}
