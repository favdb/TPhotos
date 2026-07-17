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
package app.print;

import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.DefaultTreeCellRenderer;
import tools.ImageUtil;

/**
 * Renderer class for Pool
 */
public class PoolRenderer extends DefaultTreeCellRenderer {

	private static final String TT = "PoolRenderer.";

	private final Pool pool;
	private final int ins = 4;
	private int icon_sz = Pool.ROW_SZ - ins;

	// Définition des bordures de couleur (épaisseur 2 pixels, adaptable à votre besoin)
	private static final Border BORDER_RED = BorderFactory.createLineBorder(Color.RED, 2);
	private static final Border BORDER_WHITE = BorderFactory.createLineBorder(Color.WHITE, 2);
	private static final Border BORDER_GREEN = BorderFactory.createLineBorder(Color.GREEN, 2);

	public PoolRenderer(Pool pool) {
		this.pool = pool;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		Component comp = super.getTreeCellRendererComponent(tree, value, sel,
				expanded, leaf, row, hasFocus);
		if (value instanceof PoolCell) {
			PrintItem cell = ((PoolCell) value).getPrintCell();
			if (cell.isPhoto()) {
				setIcon(ImageUtil.getImage(cell.photoFileGet(), icon_sz - ins));
			} else {
				String txt = "<html><body style='padding:5px;'>" + cell.textGet() + "</body></html>";
				setIcon(ImageUtil.createTextImage(txt, icon_sz - ins));
			}
			setText("");
			Border colorBorder;
			if (cell.pageGet() > 0) {
				colorBorder = BORDER_GREEN;
			} else if (sel) {
				colorBorder = BORDER_RED;
			} else {
				colorBorder = BORDER_WHITE;
			}
			setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(ins, 0, ins, 0),
					colorBorder
			));
			return comp;
		} else {
			setBorder(null);
			return comp;
		}
	}
}
