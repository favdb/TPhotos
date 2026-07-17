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
import i18n.I18N;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import resources.icons.ICONS;
import tools.Ui;

/**
 *
 * @author favdb
 */
public class ShortcutsKeyDlg extends JDialog {

	static String show(ShortcutsDlg p, ShortcutsKey k) {
		ShortcutsKeyDlg dlg = new ShortcutsKeyDlg(p, k);
		dlg.setVisible(true);
		return (dlg.isCanceled() ? "" : dlg.getResult());
	}

	private boolean canceled = false;
	private final ShortcutsKey key;
	private JCheckBox ckShift, ckCtrl, ckAlt;
	private JTextField tfKey;
	private static final String KEYMSG = "shortcut.0.";
	private String result = "";

	public ShortcutsKeyDlg(ShortcutsDlg p, ShortcutsKey key) {
		super(p);
		this.key = key;
		initialize();
	}

	private void initialize() {
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP1)));
		this.setTitle(I18N.getMsg("shortcut"));
		// init the shortcut message
		add(new JLabel(key.getMsg()), MIG.CENTER);
		// init the shortcut key
		tfKey = new JTextField(Shortcuts.i18n(key.getKey()).replace(" ", "+"));
		tfKey.setColumns(16);
		tfKey.addFocusListener(new TfKeyFocus());
		tfKey.addKeyListener(new TfKeyListener());
		add(tfKey);
		JPanel ckp = new JPanel(new MigLayout());
		ckShift = new JCheckBox(I18N.getMsg(KEYMSG + "shift"));
		ckShift.setSelected(key.getKey().contains("shift"));
		ckShift.setEnabled(false);
		ckp.add(ckShift);
		ckCtrl = new JCheckBox(I18N.getMsg(KEYMSG + "ctrl"));
		ckCtrl.setSelected(key.getKey().contains("ctrl"));
		ckCtrl.setEnabled(false);
		ckp.add(ckCtrl);
		ckAlt = new JCheckBox(I18N.getMsg(KEYMSG + "alt"));
		ckAlt.setSelected(key.getKey().contains("alt"));
		ckAlt.setEnabled(false);
		ckp.add(ckAlt);
		add(ckp, MIG.CENTER);
		JPanel p = new JPanel(new MigLayout());
		p.add(Ui.initButton("cancel", ICONS.K.CANCEL, e -> applyNot()),
				MIG.get(MIG.SPAN, MIG.SPLIT2, MIG.RIGHT));
		p.add(Ui.initButton("ok", ICONS.K.OK, e -> apply()),
				MIG.get(MIG.SPAN, MIG.SPLIT2, MIG.RIGHT));
		add(p, MIG.get(MIG.SPAN, MIG.RIGHT));

		this.pack();
		this.setLocationRelativeTo(getParent());
		this.setModal(true);
		tfKey.requestFocus();
	}

	private void applyNot() {
		canceled = true;
		dispose();
	}

	private void apply() {
		canceled = false;
		StringBuilder b = new StringBuilder();
		if (ckShift.isSelected()) {
			b.append("shift ");
		}
		if (ckCtrl.isSelected()) {
			b.append("ctrl ");
		}
		if (ckAlt.isSelected()) {
			b.append("alt ");
		}
		String z = tfKey.getText();
		z = z.substring(z.lastIndexOf("+") + 1);
		b.append(z);
		result = (b.toString());
		dispose();
	}

	public boolean isCanceled() {
		return canceled;
	}

	public String getKey() {
		return key.getKey();
	}

	public String getResult() {
		return result;
	}

	private class TfKeyListener implements KeyListener {

		public TfKeyListener() {
		}

		@Override
		public void keyTyped(KeyEvent e) {
			// empty
		}

		@Override
		public void keyPressed(KeyEvent e) {
			int c = e.getKeyCode();
			ckShift.setSelected(e.isShiftDown());
			ckCtrl.setSelected(e.isControlDown());
			ckAlt.setSelected(e.isAltDown());
			String modifier = KeyEvent.getKeyModifiersText(e.getModifiers());
			String chartext = "" + (c < 20 ? " " : (char) c);
			if (c >= KeyEvent.VK_F1 && c <= KeyEvent.VK_F12) {
				int n = c - KeyEvent.VK_F1 + 1;
				chartext = "[F" + n + "]";
			} else if (c >= KeyEvent.VK_NUMPAD1 && c <= KeyEvent.VK_NUMPAD9) {
				int n = c - KeyEvent.VK_NUMPAD1 + 1;
				chartext = "[" + n + "]";
			} else if (c == KeyEvent.VK_ADD) {
				chartext = "[+]";
			} else if (c == KeyEvent.VK_SUBTRACT) {
				chartext = "[-]";
			} else if (c == KeyEvent.VK_ENTER) {
				chartext = "[" + I18N.getMsg("shortcut.0.enter") + "]";
			} else if (c == KeyEvent.VK_SPACE) {
				chartext = "[" + I18N.getMsg("shortcut.0.space") + "]";
			}
			tfKey.setText(modifier + "+" + chartext);
			tfKey.selectAll();
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

	}

	private class TfKeyFocus implements FocusListener {

		public TfKeyFocus() {
		}

		@Override
		public void focusGained(FocusEvent e) {
			tfKey.selectAll();
		}

		@Override
		public void focusLost(FocusEvent e) {
			// empty
		}

	}

}
