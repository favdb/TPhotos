/*
 * Copyright (C) 2023 favdb
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
package tools.shortcut;

import api.mig.MIG;
import api.mig.swing.MigLayout;
import app.App;
import i18n.I18N;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import resources.icons.ICONS;
import tools.FontUtil;
import tools.LaF;
import tools.Ui;

/**
 *
 * @author favdb
 */
public class ShortcutsDlg extends JDialog {

	private static final String TT = "Shortcut";

	private List<ShortcutsKey> list = new ArrayList<>();
	private List<ShortcutsKey> listShef = new ArrayList<>();
	private List<ShortcutsKey> listOsbk = new ArrayList<>();
	private String sort = "msg";
	private JButton btSort;
	private JPanel panelList;
	private boolean modified;
	private JButton btOK;

	public static void show(JComponent frame) {
		ShortcutsDlg dlg = new ShortcutsDlg((JFrame) null);
		dlg.setVisible(true);
	}

	public static void show(JFrame frame) {
		ShortcutsDlg dlg = new ShortcutsDlg(frame);
		dlg.setVisible(true);
	}

	public static void show(JDialog dialog) {
		ShortcutsDlg dlg = new ShortcutsDlg(dialog);
		dlg.setVisible(true);
	}

	public static void show(JPanel panel) {
		Container parent = panel.getParent();
		while (parent != null) {
			if (parent instanceof JFrame) {
				show((JFrame) parent);
				break;
			}
			if (parent instanceof JDialog) {
				show((JDialog) parent);
				break;
			}
			parent = parent.getParent();
		}
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public ShortcutsDlg(JFrame mainFrame) {
		super(mainFrame);
		initialize();
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public ShortcutsDlg(JDialog mainFrame) {
		super(mainFrame);
		initialize();
	}

	/**
	 * initialize the user interface
	 */
	public void initialize() {
		this.setLayout(new MigLayout(MIG.get(MIG.FILLX, MIG.WRAP), "[grow][]"));
		this.setTitle(I18N.getMsg("help.shortcut"));
		listShef = listLoad(Shortcuts.SHEF);
		listSort(listShef);
		listOsbk = listLoad(Shortcuts.OSBK);
		listSort(listOsbk);
		JTextArea ta = new JTextArea(I18N.getMsg("shortcut.m.advice"));
		ta.setFont(FontUtil.getItalic(ta.getFont()));
		ta.setRows(4);
		ta.setEditable(false);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		add(ta, MIG.get(MIG.SPAN, MIG.GROW));
		panelList = new JPanel(new MigLayout(MIG.get(MIG.FILLX, MIG.WRAP, MIG.INS0, MIG.GAP0),
				"[center]" + (FontUtil.getWidth() * 2) + "px" + "[80%]"));
		refresh();

		// add the scroller
		JScrollPane scroller = new JScrollPane(panelList);
		scroller.getVerticalScrollBar().setUnitIncrement((int) (FontUtil.getHeight() * 1.25));
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setPreferredSize(new Dimension(10, 780));
		add(scroller, MIG.get(MIG.SPAN, MIG.GROW));

		////// footer part //////
		// number of keys if dev mode
		if (App.isDev()) {
			add(new JLabel("<html><i>nb of keys=" + getNbKeys() + "</i></html>"),
					MIG.get(MIG.SPAN, MIG.RIGHT));
		}

		// sort button
		btSort = Ui.initButton("btSort", "shortcut.m.sort_key", ICONS.K.SHEF_SORT, "", e -> listSort());
		btSort.setMinimumSize(new Dimension(FontUtil.getWidth() * 18, 5));
		add(btSort);

		//ok cancel buttons
		JPanel end = new JPanel(new MigLayout());
		JButton btCancel = Ui.initButton("btCancel", "cancel", ICONS.K.CANCEL, "", e -> applyNot());
		end.add(btCancel);
		btOK = Ui.initButton("btOk", "ok", ICONS.K.OK, "", e -> apply());
		btOK.setEnabled(false);
		end.add(btOK);
		add(end, MIG.get(MIG.SPAN, MIG.RIGHT));

		this.setPreferredSize(new Dimension(700, 800));
		this.pack();
		this.setLocationRelativeTo(this.getParent());
		this.setModal(true);
		SwingUtilities.invokeLater(() -> listSort());
	}

	/**
	 * get a formatted subtitle
	 *
	 * @param title
	 * @return
	 */
	private String getSubtitle(String title) {
		return "<html><p><b>" + I18N.getColonMsg(title) + "</b></p></html>";
	}

	/**
	 * load a list of ShortcutKeys
	 *
	 * @param s
	 * @return
	 */
	private List<ShortcutsKey> listLoad(String s[]) {
		list = new ArrayList<>();
		for (String sl : s) {
			String msg = I18N.getMsg("shortcut.i." + sl);
			if (msg.startsWith("!")) {
				msg = I18N.getMsg(sl);
			}
			if (!msg.startsWith("!")) {
				if (sl.equals("heading")) {
					for (int ij = 1; ij < 7; ij++) {
						list.add(new ShortcutsKey("heading" + ij));
					}
				} else {
					list.add(new ShortcutsKey(sl));
				}
			}
		}
		return list;
	}

	/**
	 * sort the lists for the given order, with changing sort parameter
	 */
	private void listSort() {
		//LOG.trace(TT + ".setSort() prior='" + this.sort + "'");
		btSort.setText(I18N.getMsg("shortcut.m.sort_" + (!sort.equals("key") ? "msg" : "key")));
		this.sort = (sort.equals("key") ? "msg" : "key");
		listSort(listShef);
		listSort(listOsbk);
		refresh();
	}

	private void listSort(List<ShortcutsKey> list) {
		Collections.sort(list, (Object o1, Object o2) -> {
			ShortcutsKey p1 = (ShortcutsKey) o1;
			ShortcutsKey p2 = (ShortcutsKey) o2;
			return p1.getSort().compareTo(p2.getSort());
		});
	}

	/**
	 * refresh the given panel for the given list
	 *
	 * @param panel
	 * @param list
	 */
	private void refresh() {
		panelList.removeAll();
		// for SHEF shorcuts
		panelList.add(new JLabel(getSubtitle("editor")), MIG.get(MIG.SPAN, MIG.LEFT));
		for (ShortcutsKey k : listShef) {
			shortcutKeyInit("shef", k);
		}
		// for OSBK shortcuts
		panelList.add(new JLabel(getSubtitle("other")), MIG.get(MIG.SPAN, MIG.LEFT));
		for (ShortcutsKey k : listOsbk) {
			shortcutKeyInit("osbk", k);
		}
		this.revalidate();
	}

	/**
	 * set the UI of the given type for the given ShortcutKey
	 *
	 * @param type
	 * @param k
	 */
	private void shortcutKeyInit(String type, ShortcutsKey k) {
		//LOG.trace(TT + ".shortcutKeyInit(type=" + type + ", k=" + k.toString() + ")");
		JButton bt = Ui.initButton("bt" + k.getId(), "", null, "",
				e -> shortcutChange(type, k));
		bt.setText(k.getKey().replace(" ", "+")
				.replace("shift", I18N.getMsg("shortcut.0.shift"))
				.replace("alt", I18N.getMsg("shortcut.0.alt"))
				.replace("ctrl", I18N.getMsg("shortcut.0.ctrl")));
		bt.setIcon(null);
		bt.setMinimumSize(new Dimension(FontUtil.getWidth() * 20, (int) (FontUtil.getHeight() * 1.5)));
		if (k.getError()) {
			bt.setBackground(Color.red);
			bt.setForeground(Color.white);
		} else if (isNotAllowed(k)) {
			bt.setBackground(Color.LIGHT_GRAY);
			if (LaF.isDark()) {
				bt.setForeground(Color.BLACK);
			}
		}
		panelList.add(bt);
		panelList.add(new JLabel(k.getMsg()));
	}

	private boolean isNotAllowed(ShortcutsKey k) {
		return (k.getKey().contains("[") && k.getKey().contains("]"));
	}

	/**
	 * change a ShortcutKey
	 *
	 * @param type
	 * @param k
	 */
	private void shortcutChange(String type, ShortcutsKey k) {
		if (isNotAllowed(k)) {
			return;
		}
		String r = ShortcutsKeyDlg.show(this, k).trim();
		if (!r.isEmpty() && !r.equals(k.getKey())) {
			listUpdate(type, k.getId(), r);
			shortcutsCheck();
			refresh();
			modified = true;
			btOK.setEnabled(true);
		}
	}

	/**
	 * update a list of the given type for a given id and key
	 *
	 * @param type
	 * @param id
	 * @param key
	 */
	private void listUpdate(String type, String id, String key) {
		if (type.equals("shef")) {
			for (ShortcutsKey k : listShef) {
				if (k.getId().equals(id)) {
					k.setKey(key);
					return;
				}
			}
		} else {
			for (ShortcutsKey k : listOsbk) {
				if (k.getId().equals(id)) {
					k.setKey(key);
					return;
				}
			}
		}
	}

	/**
	 * check if there is no double key
	 *
	 * @return true if ther is a double
	 */
	private boolean shortcutsCheck() {
		// sort the list by key
		boolean rc;
		sort = "msg";
		// check the lists
		rc = shortcutsCheckList(listShef);
		rc = rc | shortcutsCheckList(listOsbk);
		// restore the sort list
		if (rc) {
			refresh();
		}
		return rc;
	}

	/**
	 * check the given list for doubles
	 *
	 * @param list
	 * @return true if there is a double
	 */
	private boolean shortcutsCheckList(List<ShortcutsKey> list) {
		boolean rc = false;
		for (int i = 0; i < list.size(); i++) {
			ShortcutsKey k = list.get(i);
			k.resetError();
		}
		for (int i = 0; i < list.size() - 1; i++) {
			ShortcutsKey ki = list.get(i);
			for (int j = i + 1; j < list.size(); j++) {
				ShortcutsKey kj = list.get(j);
				if (ki.getKey().equals(kj.getKey())) {
					ki.setError();
					kj.setError();
					rc = true;
				}
			}
		}
		return rc;
	}

	/**
	 * get the number of Shortcuts
	 *
	 * @return
	 */
	private int getNbKeys() {
		return Shortcuts.OSBK.length + Shortcuts.SHEF.length;
	}

	/**
	 * exit with no apply
	 */
	private void applyNot() {
		dispose();
	}

	/**
	 * exit applying modifications
	 */
	private void apply() {
		if (modified) {
			if (shortcutsCheck()) {
				JOptionPane.showMessageDialog(this,
						I18N.getMsg("shortcut.m.error"),
						I18N.getMsg("shortcut"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			listSave();
		}
		dispose();
	}

	/**
	 * save the lists
	 */
	private void listSave() {
		Collections.sort(listShef, (Object o1, Object o2) -> {
			ShortcutsKey p1 = (ShortcutsKey) o1;
			ShortcutsKey p2 = (ShortcutsKey) o2;
			return p1.getId().compareToIgnoreCase(p2.getId());
		});
		for (ShortcutsKey k : listShef) {
			if (!k.getId().contains("heading")) {
				Shortcuts.setKey(k.getId(), k.getKey());
			}
		}
		Collections.sort(listOsbk, (Object o1, Object o2) -> {
			ShortcutsKey p1 = (ShortcutsKey) o1;
			ShortcutsKey p2 = (ShortcutsKey) o2;
			return p1.getId().compareToIgnoreCase(p2.getId());
		});
		for (ShortcutsKey k : listOsbk) {
			if (!k.getId().contains("heading")) {
				Shortcuts.setKey(k.getId(), k.getKey());
			}
		}
		Shortcuts.saveKeys();
	}

}
