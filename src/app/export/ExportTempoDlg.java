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

import app.MainFrame;
import app.resources.icons.ICONS;
import app.tools.GBC;
import app.tools.Ui;
import java.awt.GridBagLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
		setLayout(new GridBagLayout());
		
		JPanel p = new JPanel(new GridBagLayout());
		p.add(Ui.initButton("minus", ICONS.K.MINUS, e -> addTempo(-1)), new GBC("0, 0, ins 2"));
		
		tfTempo = new JTextField();
		tfTempo.setColumns(3);
		p.add(tfTempo, new GBC("0, 1, ins 2"));
		
		p.add(Ui.initButton("plus", ICONS.K.PLUS, e -> addTempo(1)), new GBC("0, 2, ins 2"));

		add(p, new GBC("0, 0, gw 2, center, ins 5"));
		
		add(Ui.initButton("ask.ok", ICONS.K.OK, e -> dispose()), new GBC("1, 0, ins 2"));
		add(Ui.initButton("ask.cancel", ICONS.K.CANCEL, e -> {
			tempo = -1;
			dispose();
		}), new GBC("1, 1, ins 2"));

		pack();
		setLocationRelativeTo(mainFrame);
		addTempo(0);
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