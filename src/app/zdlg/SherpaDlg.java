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
package app.zdlg;

import api.mig.MIG;
import api.mig.swing.MigLayout;
import api.sherpa.SHERPA;
import app.App;
import app.i18n.I18N;
import app.resources.icons.ICONS;
import app.tools.Ui;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author favdb
 */
public class SherpaDlg extends JDialog {

	private static final String TT = "SherpaDialog.";
	private String text;
	private SHERPA editor;
	private boolean validate = false;
	private final String[] stitle;

	public SherpaDlg(JFrame parentFrame, String text, String... title) {
		super(parentFrame, true);
		this.text = text;
		this.stitle = title;
		initialize();
	}

	public SherpaDlg(java.awt.Window parent, String text, String... title) {
		super(parent, ModalityType.APPLICATION_MODAL);
		this.text = text;
		this.stitle = title;
		initialize();
	}

	private void initialize() {
		this.setFont(App.fontGet());
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP1)));
		this.setTitle(I18N.getMsg("print.text_edit"));
		if (stitle != null && stitle.length > 0) {
			this.setTitle(I18N.getMsg(stitle[0]));
		}
		this.setPreferredSize(new Dimension(940, 480));
		add(editor = new SHERPA(), MIG.GROW);
		editor.setPreferredSize(new Dimension(1024, 480));
		editor.setFont(App.fontGet());
		editor.htmlContentSet(text);
		JPanel pok = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.INS0)));
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
		this.setVisible(true);
	}

	public void setHtmlContent(String text) {
		//LOG.trace(TT + "setHtmlContent(text=" + text + ")");
		editor.htmlContentSet(text);
	}

	public boolean isValidate() {
		//LOG.trace(TT + "isValidate()");
		return validate;
	}

	public String getHtmlContent() {
		//LOG.trace(TT + "getHtmlContent()");
		return editor.htmlContentGet();
	}

	private void doOK() {
		validate = true;
		dispose();
	}

}
