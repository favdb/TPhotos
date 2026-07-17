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

import app.App;
import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.JLabel;

/**
 *
 * @author favdb
 */
public class FontUtil {

	public static Font getItalic(Font def) {
		return new Font(def.getName(), Font.ITALIC, def.getSize());
	}

	public static Font getBold(Font def) {
		return new Font(def.getName(), Font.BOLD, def.getSize());
	}

	public static int getHeight() {
		return getHeight(new JLabel("W"));
	}

	public static int getHeight(JLabel x) {
		FontMetrics fnt = x.getFontMetrics(x.getFont());
		return fnt.getHeight();
	}

	public static int getWidth() {
		return getWidth(new JLabel("W"));
	}

	public static int getWidth(JLabel x) {
		FontMetrics fnt = x.getFontMetrics(x.getFont());
		int n = 0, r = 0;
		for (int i = '0'; i < '9'; i++) {
			n += fnt.charWidth(i);
			r++;
		}
		for (int i = 'A'; i < 'Z'; i++) {
			n += fnt.charWidth(i);
			r++;
		}
		for (int i = 'a'; i < 'z'; i++) {
			n += fnt.charWidth(i);
			r++;
		}
		if (r == 0) {
			r = 1;
		}
		return n / r;
	}

	public static Font getSmall() {
		//LOG.trace(TT+".getSmallFont()");
		int sz = App.fontGet().getSize();
		sz = (sz / 4) * 3;
		return new Font("Default", 0, sz);
	}

}
