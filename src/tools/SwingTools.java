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
package tools;

import java.awt.Dimension;
import javax.swing.JComponent;

/**
 *
 * @author favdb
 */
public class SwingTools {

	/**
	 * set fixed size for the given JComponent
	 *
	 * @param comp: the component
	 * @param dim : the new size
	 */
	public static void setFixedSize(JComponent comp, Dimension dim) {
		comp.setSize(dim);
		comp.setPreferredSize(dim);
		comp.setMaximumSize(dim);
		comp.setMinimumSize(dim);
	}

	/**
	 * set fixed size for the given JComponent
	 *
	 * @param comp: the component
	 * @param width: the new width
	 * @param height: the new height
	 */
	public static void setFixedSize(JComponent comp, int width, int height) {
		Dimension dim = new Dimension(width, height);
		setFixedSize(comp, dim);
	}

}
