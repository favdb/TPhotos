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
package app.album;

import api.mig.swing.MigLayout;
import app.MainFrame;
import i18n.I18N;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import resources.icons.ICONS;
import api.mig.MIG;
import tools.Ui;

/**
 * dialog for Diaporama options
 *
 * @author favdb
 */
public class DiaporamaDlg extends JDialog {

	private static final String TT = "DiapoParamDlg.";
	private final MainFrame mainFrame;
	private final AlbumParam param;
	private JComboBox cbMode;
	private String[] modes = {
		I18N.getMsg("album.param.mode_none"),
		I18N.getMsg("album.param.mode_dissolve"),
		I18N.getMsg("album.param.mode_fade")
	};
	private JTextField tfTempo;

	public DiaporamaDlg(MainFrame mainFrame) {
		super(mainFrame, true);
		this.mainFrame = mainFrame;
		param = mainFrame.albumParamGet();
		initialize();
	}

	@SuppressWarnings("unchecked")
	private void initialize() {
		setLayout(new MigLayout(MIG.WRAP + " 2"));
		setTitle(I18N.getMsg("album.param.diapo_tips"));
		add(new JLabel(I18N.getColonMsg("album.param.mode")), MIG.RIGHT);
		cbMode = new JComboBox(modes);
		cbMode.setSelectedIndex(param.getMode());
		cbMode.addItemListener(e -> tfTempo.setEnabled(cbMode.getSelectedIndex() > 0));
		add(cbMode);
		add(new JLabel(I18N.getColonMsg("album.param.mode_tempo")), MIG.RIGHT);
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
		add(tfTempo);
		tfTempo.setEnabled(cbMode.getSelectedIndex() > 0);
		add(new JLabel(I18N.getMsg("album.param.mode_tempo_tips")), MIG.SKIP + " 1");
		JPanel p = new JPanel(new MigLayout());
		p.add(Ui.initButton("ask.cancel", ICONS.K.CANCEL, e -> dispose()));
		JButton btOK;
		p.add(btOK = Ui.initButton("ask.ok", ICONS.K.OK, e -> doOK()));
		add(p, MIG.get(MIG.SPAN, MIG.RIGHT));
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
