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
package app.ui;

import api.mig.MIG;
import api.mig.swing.MigLayout;
import api.shef.SHEF;
import api.shef.editor.HTMLEditorPane;
import app.App;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JPanel;
import resources.icons.ICONS;
import tools.Ui;

/**
 *
 * @author favdb
 */
public class SHEFDialog extends JDialog {

	private static final String TT = "SHEFDialog.";
	private String text;
	private HTMLEditorPane editor;
	private boolean validate = false;

	public SHEFDialog(Frame parentFrame, boolean b, String text) {
		super(parentFrame, true);
		this.text = text;
		initialize();
	}

	private void initialize() {
		SHEF.scaleAuto();
		this.setFont(App.fontGet());
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP1)));
		this.setPreferredSize(new Dimension(1024, 480));
		add(editor = new HTMLEditorPane(), MIG.GROW);
		editor.setFont(App.fontGet());
		editor.setText(text);
		JPanel pok = new JPanel(new MigLayout());
		pok.add(Ui.initButton("ask.ok", ICONS.K.OK, e -> doOK()));
		pok.add(Ui.initButton("ask.cancel", ICONS.K.CANCEL, e -> {
			dispose();
		}));
		add(pok, MIG.get(MIG.SPAN, MIG.RIGHT));
		this.pack();
		for (Component c : editor.getComponents()) {
			c.setFont(App.fontGet());
		}
		this.setLocationRelativeTo(getParent());
	}

	public void setHtmlContent(String text) {
		//LOG.trace(TT + "setHtmlContent(text=" + text + ")");
		editor.setText(text);
	}

	public boolean isSaved() {
		//LOG.trace(TT + "isSaved()");
		return true;
	}

	public String getHtmlContent() {
		//LOG.trace(TT + "getHtmlContent()");
		return editor.getText();
	}

	private void doOK() {
		validate = true;
		dispose();
	}

}
