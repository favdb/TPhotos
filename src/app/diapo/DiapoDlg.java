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
package app.diapo;

import app.MainFrame;
import app.i18n.I18N;
import app.resources.icons.ICONS;
import app.tools.GBC;
import app.tools.Ui;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * dialog for Diaporama options, used only for testing
 *
 * @author favdb
 */
public class DiapoDlg extends JDialog {

	private static final String TT = "DiapoParamDlg.";

	private final MainFrame mainFrame;
	private final DiapoParam param;
	private JComboBox cbMode;
	private final String[] modes = {
		I18N.getMsg("album.param.mode_none"),
		I18N.getMsg("album.param.mode_dissolve"),
		I18N.getMsg("album.param.mode_fade")
	};
	private JTextField tfTempo;

	public DiapoDlg(MainFrame mainFrame) {
		super(mainFrame, true);
		this.mainFrame = mainFrame;
		param = mainFrame.diapoParamGet();
		initialize();
	}

	@SuppressWarnings("unchecked")
	private void initialize() {
		setLayout(new GridBagLayout());
		setTitle(I18N.getMsg("album.param.diapo_tips"));

		// Umurongo wa 0: Mode
		add(new JLabel(I18N.getColonMsg("album.param.mode")), new GBC("0, 0, right, ins 2"));
		cbMode = new JComboBox(modes);
		cbMode.setSelectedIndex(param.getMode());
		cbMode.addItemListener(e -> tfTempo.setEnabled(cbMode.getSelectedIndex() > 0));
		add(cbMode, new GBC("0, 1, left, ins 2"));

		// Umurongo wa 1: Tempo
		add(new JLabel(I18N.getColonMsg("album.param.mode_tempo")), new GBC("1, 0, right, ins 2"));
		tfTempo = new JTextField(param.getTempo().toString());
		tfTempo.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent eve) {
				String AllowedData = "0123456789.";
				char enter = eve.getKeyChar();
				if (!AllowedData.contains(String.valueOf(enter))) {
					eve.consume();
				}
			}
		});
		tfTempo.setColumns(5);
		add(tfTempo, new GBC("1, 1, left, ins 2"));
		tfTempo.setEnabled(cbMode.getSelectedIndex() > 0);

		// Umurongo wa 2: Inshoberamahanga / Tips
		add(new JLabel(I18N.getMsg("album.param.mode_tempo_tips")), new GBC("2, 1, left, ins 2"));

		// Umurongo wa 3: Ingofero z'ibutoni Cancel / OK
		JPanel p = new JPanel(new GridBagLayout());
		p.add(Ui.initButton("ask.cancel", ICONS.K.CANCEL, e -> dispose()), new GBC("0, 0, ins 2"));
		JButton btOK;
		p.add(btOK = Ui.initButton("ask.ok", ICONS.K.OK, e -> doOK()), new GBC("0, 1, ins 2"));
		
		add(p, new GBC("3, 0, gw 2, right, ins 5"));

		pack();
		this.setLocationRelativeTo(mainFrame);
		this.getRootPane().setDefaultButton(btOK);
	}

	private void doOK() {
		// set param mode and tempo
		param.setMode(cbMode.getSelectedIndex());
		dispose();
	}

}