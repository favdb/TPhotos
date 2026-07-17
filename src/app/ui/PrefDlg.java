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
package app.ui;

import api.mig.swing.MigLayout;
import app.App;
import app.Pref;
import i18n.I18N;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import resources.icons.ICONS;
import resources.icons.IconButton;
import tools.LaF;
import api.mig.MIG;
import tools.Ui;

/**
 *
 * @author favdb
 */
public class PrefDlg extends JDialog {

	private static final String TT = "ZoomDlg.";

	private FontPanel fntDefault;
	private JRadioButton rbNormal;
	private JRadioButton rbDark;
	private int original;

	public PrefDlg(MainFrame mainFrame) {
		super(mainFrame);
		initialize();
	}

	private void initialize() {
		setLayout(new MigLayout());
		setTitle(I18N.getMsg("pref.zoom"));
		original = App.preferences.toString().hashCode();

		if (App.isDev()) {
			add(initTheme(), MIG.SPAN);
		}

		JPanel zoom = new JPanel(new MigLayout());
		zoom.add(new JLabel(I18N.getColonMsg("pref.zoom.font")), MIG.SPAN);
		zoom.add(fntDefault = new FontPanel(this, "default", App.fontGet()));
		IconButton fontPlus = new IconButton("fontPlus", ICONS.K.PLUS, e -> increaseFont(1));
		zoom.add(fontPlus, MIG.get(MIG.SPAN, MIG.SPLIT2));
		IconButton fontMinus = new IconButton("fontPlus", ICONS.K.MINUS, e -> increaseFont(-1));
		zoom.add(fontMinus);
		add(zoom, MIG.SPAN);

		JPanel pok = new JPanel(new MigLayout());
		pok.add(Ui.initButton("ask.ok", ICONS.K.OK, e -> doOK()));
		pok.add(Ui.initButton("ask.cancel", ICONS.K.CANCEL, e -> {
			dispose();
		}));
		add(pok, MIG.get(MIG.SPAN, MIG.RIGHT));
		pack();
		this.setLocationRelativeTo(getParent());
		if (App.isDev()) {
			refreshAll();
		}
	}

	private JPanel initTheme() {
		JPanel theme = new JPanel(new MigLayout());
		theme.add(new JLabel(I18N.getColonMsg("pref.theme")), MIG.SPAN);
		rbNormal = new JRadioButton(I18N.getMsg("pref.theme.normal"));
		//rbNormal.setEnabled(false);
		rbDark = new JRadioButton(I18N.getMsg("pref.theme.dark"));
		//rbDark.setEnabled(false);
		rbDark.addChangeListener(e -> refreshAll());
		ButtonGroup bg = new ButtonGroup();
		bg.add(rbNormal);
		bg.add(rbDark);
		theme.add(rbNormal);
		theme.add(rbDark);
		rbNormal.setSelected(!App.preferences.darkGet());
		rbDark.setSelected(App.preferences.darkGet());
		return theme;
	}

	private void refreshAll() {
		//LOG.trace(TT + "refreshAll()");
		Color fg = LaF.THEME.text.get(!rbDark.isSelected());
		Color bk = LaF.THEME.control.get(rbDark.isSelected());
		for (Component comp : this.getComponents()) {
			if (((Container) comp).getComponentCount() > 0) {
				setColor(comp, fg, bk);
			} else {
				comp.setForeground(fg);
				comp.setBackground(bk);
			}
		}
		validate();
		repaint();
	}

	private void setColor(Component comp, Color fg, Color bk) {
		if (((Container) comp).getComponentCount() > 0) {
			Component[] children = ((Container) comp).getComponents();
			for (Component child : children) {
				if (((Container) child).getComponentCount() > 0) {
					setColor(child, fg, bk);
				}
				((JComponent) child).setForeground(fg);
				((JComponent) child).setBackground(bk);
			}
		}
		((JComponent) comp).setForeground(fg);
		((JComponent) comp).setBackground(bk);
	}

	private void doOK() {
		Pref pref = App.preferences;
		App.fontSet(fntDefault.getFont());
		if (App.isDev()) {
			pref.darkSet(rbDark.isSelected());
		}
		JOptionPane.showMessageDialog(this,
				I18N.getMsg("pref.zoom.restart"),
				I18N.getMsg("pref.zoom"),
				JOptionPane.INFORMATION_MESSAGE);
		dispose();
		if (original != pref.toString().hashCode()) {
			if (App.isDev()) {
				LaF.setColors();
			}
			LaF.setFont();
			LaF.update();
		}
	}

	private void increaseFont(int inc) {
		fntDefault.increase(inc);
		pack();
	}

	private static class FontPanel extends JPanel {

		private final JLabel show;

		public FontPanel(JDialog caller, String name, Font font) {
			setLayout(new MigLayout(MIG.get(MIG.INS1, MIG.GAP + " 2")));
			setName("font." + name);
			setFont(font);
			show = new JLabel(getString(font));
			show.setName("show");
			show.setBorder(BorderFactory.createEtchedBorder());
			show.setFont(font);
			add(show);
		}

		private void increase(int sz) {
			String str = show.getText();
			String ps[] = str.split(",");
			int nsz = 12;
			if (ps.length > 2) {
				nsz = Integer.parseInt(ps[2].trim());
			}
			nsz += sz;
			show.setText(ps[0] + ", " + ps[1] + ", " + nsz);
			Font fnt = getFont(show.getText());
			show.setFont(fnt);
			setFont(fnt);
		}

		public static Font getFont(String str) {
			//LOG.trace(TT+".getFont(str="+str+")");
			String s[] = str.split(",");
			if (s.length < 3) {
				return (App.fontGet());
			}
			int style;
			switch (s[1].trim()) {
				case "1":
					style = Font.BOLD;
					break;
				case "2":
					style = Font.ITALIC;
					break;
				default:
					style = Font.PLAIN;
					break;
			}
			int sz = Integer.parseInt(s[2].trim());
			if (sz == 0) {
				sz = 12;
			}
			return new Font(s[0], style, sz);
		}

	}

	public static String getString(Font font) {
		StringBuilder buf = new StringBuilder();
		buf.append(font.getName());
		buf.append(", ");
		switch (font.getStyle()) {
			case Font.BOLD:
				buf.append("bold");
				break;
			case Font.ITALIC:
				buf.append("italic");
				break;
			default:
				buf.append("plain");
				break;
		}
		buf.append(", ").append(font.getSize());
		return buf.toString();
	}

}
