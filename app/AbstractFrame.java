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
package app;

import i18n.I18N;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import tools.Html;

/**
 *
 * @author favdb
 */
public abstract class AbstractFrame extends JFrame {

	private static final String TT = "AbstractFrame.";

	public JTextPane taInfos;

	public AbstractFrame() {
	}

	public abstract void initialize();

	public abstract void doCopyBegin();

	public abstract void doCopyEnd();

	/**
	 * set infos text
	 *
	 * @param txt
	 */
	public void taInfosAdd(String txt) {
		//LOG.trace(TT + "setInfos(txt=" + txt + ")");
		taInfos.setText(Html.intoHtml(taInfosGet() + txt));
		taInfos.setCaretPosition(taInfos.getDocument().getLength());
		taInfos.repaint();
	}

	/**
	 * get only body part of infos
	 *
	 * @return
	 */
	public String taInfosGet() {
		//LOG.trace(TT+"getInfos()");
		return Html.getBody(taInfos.getText());
	}

	public void taInfosInit(String msg) {
		taInfos = new JTextPane();
		taInfos.setContentType("text/html");
		taInfoSet(I18N.getMsg(msg));
	}

	/**
	 * set infos text
	 *
	 * @param txt
	 */
	public void taInfoSet(String txt) {
		//LOG.trace(TT + "setInfos(txt=" + txt + ")");
		taInfos.setText(Html.intoHtml(txt));
		taInfos.setCaretPosition(taInfos.getDocument().getLength());
		taInfos.repaint();
	}

}
