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
package app.export;

import api.mig.swing.MigLayout;
import app.ui.MainFrame;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import resources.icons.ICONS;
import api.mig.MIG;
import tools.Ui;

/**
 *
 * @author favdb
 */
public class ExportTempoDlg extends JDialog {

	private final MainFrame mainFrame;
	private int tempo = 5;
	private JTextField tfTempo;

	public ExportTempoDlg(MainFrame mainFrame) {
		super(mainFrame, true);
		this.mainFrame = mainFrame;
		initialize();
	}

	private void initialize() {
		setLayout(new MigLayout());
		JPanel p = new JPanel(new MigLayout(MIG.FILL));
		p.add(Ui.initButton("minus", ICONS.K.MINUS, e -> addTempo(-1)));
		p.add(tfTempo = new JTextField());
		tfTempo.setColumns(3);
		p.add(Ui.initButton("plus", ICONS.K.PLUS, e -> addTempo(1)));
		add(p, MIG.get(MIG.SPAN, MIG.CENTER));
		add(Ui.initButton("ask.ok", ICONS.K.OK, e -> dispose()));
		add(Ui.initButton("ask.cancel", ICONS.K.CANCEL, e -> {
			tempo = -1;
			dispose();
		}));
	}

	public int getTempo() {
		return tempo;
	}

	private void addTempo(int value) {
		tempo += value;
		if (tempo < 1) {
			tempo = 1;
		}
		tfTempo.setText("" + tempo);
	}

}
