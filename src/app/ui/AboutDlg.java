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
package app.ui;

import api.mig.swing.MigLayout;
import app.App;
import i18n.I18N;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import resources.MainResources;
import resources.icons.ICONS;
import tools.Html;
import tools.LOG;
import api.mig.MIG;
import tools.Ui;
import tools.file.FileUtil;

/**
 * tha about JDialog
 *
 * @author favdb
 */
public class AboutDlg extends JDialog {

	private static final String TT = "AboutDlg.";

	public AboutDlg(JFrame parent) {
		super(parent, true);
		initialize();
	}

	/**
	 * initialize the dialog
	 */
	private void initialize() {
		this.setTitle(I18N.getMsg("about"));
		setLayout(new MigLayout(MIG.FLOWY, "[center]"));
		setPreferredSize(new Dimension(680, 650));
		JTabbedPane tb = new JTabbedPane();
		JPanel pInfo = initInfos();
		tb.addTab(I18N.getMsg("about.infos"), pInfo);
		JPanel pVersions = initVersions();
		tb.addTab(I18N.getMsg("about.versions"), pVersions);
		add(tb, MIG.get(MIG.SPAN, MIG.GROW));
		add(Ui.initButton("ask.close", ICONS.K.OK, e -> dispose()), MIG.get(MIG.SPAN, MIG.CENTER));
		pack();
		this.setLocationRelativeTo(getParent());
	}

	/**
	 * initialize informations from resources
	 *
	 * @return
	 */
	private JPanel initInfos() {
		JPanel p = new JPanel(new MigLayout());
		JTextPane tx = new JTextPane();
		tx.setContentType("text/html");
		tx.setEditable(false);
		String lang = "_" + Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry();
		if (!App.getLang().isEmpty()) {
			lang = "_" + App.getLang();
		}
		boolean b = true;
		while (b) {
			if (MainResources.exists("html/About" + lang + ".html", MainResources.class)) {
				break;
			}
			if (lang.contains("_")) {
				lang = lang.substring(0, lang.lastIndexOf("_"));
			}
			if (lang.isEmpty()) {
				b = false;
			}
		}
		String str = FileUtil.resourceRead("html/About" + lang + ".html", MainResources.class);
		tx.setText(Html.intoHtml(str));
		tx.addHyperlinkListener(evt -> openBrowser(evt));
		tx.setPreferredSize(new Dimension(680, 650));
		JScrollPane scroll = new JScrollPane(tx);
		p.add(scroll, MIG.GROW);
		return p;
	}

	/**
	 * initalize versions informations from resources
	 *
	 * @return
	 */
	private JPanel initVersions() {
		JPanel p = new JPanel(new MigLayout(MIG.FILL));
		JTextPane tx = new JTextPane();
		tx.setContentType("text/html");
		tx.setEditable(false);
		String str = FileUtil.resourceRead("Versions.TXT", MainResources.class).replace("\n", "<br>");
		tx.setText(Html.intoHtml(str));
		tx.setPreferredSize(new Dimension(680, 650));
		JScrollPane scroll = new JScrollPane(tx);
		p.add(scroll, MIG.GROW);
		return p;
	}

	/**
	 * open a URL link in the default browser
	 *
	 * @param evt
	 */
	public static void openBrowser(HyperlinkEvent evt) {
		//LOG.trace(TT + "openUrl(evt description=\"" + evt.getDescription() + "\")");
		if (evt.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
			return;
		}
		String url = evt.getDescription();
		try {
			Desktop.getDesktop().browse(new URI(url));
		} catch (URISyntaxException | IOException e) {
			LOG.err(TT + "openBrowser(" + url + ")", e);
		}
	}

}
