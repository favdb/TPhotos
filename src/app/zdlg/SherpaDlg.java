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
import java.awt.Window;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author favdb
 */
public class SherpaDlg extends JDialog {

	private static final String TT = "SherpaDialog.";
	private String text;
	private SHERPA editor;
	private boolean validate = false;

	public SherpaDlg(JFrame frame, String text, String... title) {
		this(SwingUtilities.getWindowAncestor(frame), text, title);
	}

	public SherpaDlg(Window parent, String text, String... title) {
		super(parent, ModalityType.APPLICATION_MODAL);
		this.text = text;
		if (title != null && title.length > 0) {
			this.setTitle(I18N.getMsg(title[0]));
		} else {
			this.setTitle(I18N.getMsg("print.text_edit"));
		}
		initialize();
	}

	/**
	 * initialize
	 */
	private void initialize() {
		this.setFont(App.fontGet());
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP1)));
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

	/**
	 * set HTMLcontent
	 *
	 * @param text
	 */
	public void setHtmlContent(String text) {
		//LOG.trace(TT + "setHtmlContent(text=" + text + ")");
		editor.htmlContentSet(text);
	}

	/**
	 * get HTML content
	 *
	 * @return
	 */
	public String getHtmlContent() {
		//LOG.trace(TT + "getHtmlContent()");
		return editor.htmlContentGet();
	}

	/**
	 * do validation
	 */
	private void doOK() {
		validate = true;
		dispose();
	}

	/**
	 * check if validated
	 *
	 * @return
	 */
	public boolean isOK() {
		//LOG.trace(TT + "isOK()");
		return validate;
	}

}
