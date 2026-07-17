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
package tools;

import i18n.I18N;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import resources.icons.ICONS;
import resources.icons.IconUtil;

/**
 * user interface utilities
 *
 * @author favdb
 */
public class Ui {

	/**
	 * set a tooltype text to the given component, using the name
	 *
	 * @param comp
	 */
	private static void setToolTipText(JComponent comp) {
		String name = comp.getName();
		if (name == null) {
			return;
		}
		if (name.startsWith("!") || name.startsWith(" ")) {
			name = name.substring(1);
		}
		String str = I18N.getMsg(name + "_tips");
		if (!str.startsWith("!")) {
			comp.setToolTipText(str);
		}
	}

	/**
	 * initialize a JButton
	 *
	 * @param name
	 * @param icon
	 * @param action
	 * @return
	 */
	public static JButton initButton(String name, ICONS.K icon, ActionListener action) {
		JButton bt = new JButton();
		if (icon != ICONS.K.NONE) {
			bt.setIcon(icon.getIcon());
		}
		bt.setName(name);
		if (!name.startsWith("!") && !name.startsWith(" ")) {
			bt.setText(I18N.getMsg(name));
		}
		setToolTipText(bt);
		if (action != null) {
			bt.addActionListener(action);
		}
		bt.setMargin(new Insets(0, 0, 0, 0));
		return bt;
	}

	/**
	 * initialize an Icon Jbutton
	 *
	 * @param name
	 * @param icon
	 * @param action
	 * @return
	 */
	public static JButton initIconButton(String name, ICONS.K icon, ActionListener action) {
		JButton bt = new JButton();
		bt.setIcon(icon.getIcon());
		bt.setName(name);
		setToolTipText(bt);
		bt.setMaximumSize(new Dimension((int) (IconUtil.getDefSize() * 1.3), (int) (IconUtil.getDefSize() * 1.3)));
		bt.addActionListener(action);
		return bt;
	}

	public static JButton initIconButton(String name, ICONS.K icon, String tt, ActionListener action) {
		JButton bt = initIconButton(name, icon, action);
		bt.setToolTipText(I18N.getMsg(tt));
		return bt;
	}

	public static JButton initButton(String name, String text, ICONS.K icon,
			String tooltip, ActionListener... act) {
		//LOG.trace(TT+"initButton(name=" + name + ",
		//text=" + text + ", icon=" + icon.toString() + ")");
		JButton btn = new JButton();
		btn.setName(name);
		if (text != null && !text.isEmpty()) {
			if (text.equals("...") || text.trim().isEmpty()) {
				btn.setText(text);
			} else if (!text.isEmpty() && !text.equalsIgnoreCase("none")) {
				btn.setText(I18N.getMsg(text));
			}
		}
		if (icon != null && icon != ICONS.K.NONE && icon != ICONS.K.EMPTY) {
			btn.setIcon(IconUtil.getIconSmall(icon));
			if (text == null || text.isEmpty()) {
				btn.setMargin(new Insets(0, 0, 0, 0));
				btn.setMaximumSize(IconUtil.getDefDim());
			}

		}
		if (!tooltip.isEmpty()) {
			btn.setToolTipText(I18N.getMsg(tooltip));
		}
		if (act != null && act.length > 0) {
			btn.addActionListener(act[0]);
		}
		return btn;
	}

	/**
	 * initialize a JToggleButton
	 *
	 * @param text
	 * @param icon
	 * @param selected
	 * @param action
	 * @return
	 */
	public static JToggleButton initToggleButton(String text, ICONS.K icon, boolean selected, ActionListener action) {
		JToggleButton bt = new JToggleButton(I18N.getMsg(text));
		bt.setIcon(IconUtil.getIconSmall(icon));
		bt.setToolTipText(I18N.getMsg(text + "_tips"));
		bt.setSelected(selected);
		bt.addActionListener(action);
		return bt;
	}

	/**
	 * initialize a JToggleButton
	 *
	 * @param text
	 * @param selected
	 * @param action
	 * @return
	 */
	public static JToggleButton initToggleButton(String text, boolean selected, ActionListener action) {
		JToggleButton bt = new JToggleButton(I18N.getMsg(text));
		bt.setIcon(IconUtil.getIconSmall(text.replace(".", "_")));
		bt.setToolTipText(I18N.getMsg(text + "_tips"));
		bt.setSelected(selected);
		bt.addActionListener(action);
		return bt;
	}

	/**
	 * initialize a JMenu
	 *
	 * @param key
	 * @return
	 */
	public static JMenu initMenu(String key) {
		JMenu m = new JMenu(I18N.getMsg(key));
		setToolTipText(m);
		m.setMnemonic(I18N.getMnemonic(key));
		return m;
	}

	/**
	 * initialize a menu item
	 *
	 * @param icon : may be null or ICONS.K.EMPTY
	 * @param name
	 * @param action : may be null for no action
	 * @return
	 */
	public static JMenuItem initMenuItem(ICONS.K icon, String name, ActionListener action) {
		return initMenuItem(icon, name, "", ' ', action);
	}

	/**
	 * initialize a menu item
	 *
	 * @param icon : may be null or ICONS.K.EMPTY
	 * @param name
	 * @param key : keyboard shortcut
	 * @param mnemonic : may be space char for no mnemonic
	 * @param action : may be null for no action
	 * @return
	 */
	public static JMenuItem initMenuItem(ICONS.K icon, String name,
			String key, char mnemonic, ActionListener action) {
		JMenuItem m = new JMenuItem(I18N.getMsg(name));
		if (icon != null && !icon.toString().equals("empty")) {
			m.setIcon(icon.getIcon());
		}
		m.setName(name);
		// keyboard shortcut
		if (!key.isEmpty()) {
			KeyStroke k = KeyStroke.getKeyStroke(key);
			if (k != null) {
				m.setAccelerator(k);
			}
		}
		if (mnemonic != ' ') {
			m.setMnemonic(mnemonic);
		}
		setToolTipText(m);
		if (action != null) {
			m.addActionListener(action);
		}
		return m;
	}

	/**
	 * initalize a JCheckBoxMenuItem
	 *
	 * @param icon
	 * @param name
	 * @param action
	 * @return
	 */
	public static JCheckBoxMenuItem initCkMenuItem(ICONS.K icon, String name, ActionListener action) {
		JCheckBoxMenuItem m = new JCheckBoxMenuItem(I18N.getMsg(name));
		if (icon != null && !icon.toString().equals("empty")) {
			m.setIcon(icon.getIcon());
		}
		m.setName(name);
		setToolTipText(m);
		if (action != null) {
			m.addActionListener(action);
		}
		return m;
	}

	public static int getTextWidth(String str, Font font) {
		AffineTransform at = new AffineTransform();
		FontRenderContext frc = new FontRenderContext(at, true, true);
		return (int) font.getStringBounds(str, frc).getWidth();
	}

	public static JComboBox<String> initComboBox(String paperlist, String[] list, String sel) {
		JComboBox<String> cb = new JComboBox<String>(list);
		if (!sel.isEmpty()) {
			cb.setSelectedItem(sel);
		}
		return cb;
	}

}
