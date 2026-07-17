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

import api.mig.MIG;
import api.mig.swing.MigLayout;
import app.App;
import app.Const;
import app.Organise;
import app.album.Album;
import app.diapo.DiapoParam;
import app.diapo.DiapoPreview;
import app.export.Export;
import app.print.Print;
import i18n.I18N;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import tools.LOG;

/**
 * main JFrame class
 *
 * @author favdb
 */
public class MainFrame extends JFrame {

	private static final String TT = "MainFrame.";

	public MainMenu appMenu;
	public JPanel panel;
	private Organise organiser;
	private Album album;
	private File file;
	private Print print;

	public MainFrame() {
		super();
		initialize();
	}

	/**
	 * initialize
	 */
	private void initialize() {
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				App.exit();
			}
		});
		setIconImage(IconUtil.getIconImage(ICONS.K.TPHOTOS.toString()));
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.GAP0, MIG.INS0, MIG.WRAP1, MIG.HIDEMODE3)));
		Dimension sz = Toolkit.getDefaultToolkit().getScreenSize();
		this.setMaximumSize(sz);
		appMenu = new MainMenu();
		add(appMenu.getToolBar(), MIG.get(MIG.GROWX, MIG.TOP));
		panel = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.GAP0, MIG.INS0)));
		panel.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		add(panel, MIG.get(MIG.GROW));
		organiser = new Organise();
		album = new Album();
		album.loadTable();
		if (album.getTable().xml.isOpened()) {
			album.loadParam();
		}
		if (appMenu.btDiapo != null) {
			appMenu.btDiapo.setVisible(album.getTable().getRowCount() > 0);
			appMenu.btExport.setVisible(album.getTable().getRowCount() > 0);
		}
		setPreferredSize(new Dimension(1024, 768));
		pack();
		setLocationRelativeTo(null);
		doSorter();
	}

	/**
	 * exit the app
	 */
	private void doExit() {
		//LOG.trace(TT + "doExit()");
		if (album.getTable().isModified()) {
			album.save();
		}
		dispose();
		App.exit();
	}

	/**
	 * do the diaporama
	 */
	public void doDiapo() {
		//LOG.trace(TT + "doDiapo()");
		if (appMenu.btSorter != null) {
			appMenu.btSorter.setSelected(false);
			appMenu.btExport.setSelected(false);
		}
		panel.removeAll();
		panel.add(album.getContentPane(), MIG.GROW);
		titleUpdate();
	}

	/**
	 * do the sort
	 */
	public void doSorter() {
		//LOG.trace(TT + "doSorter()");
		if (appMenu.btAlbum != null) {
			appMenu.btAlbum.setSelected(false);
			appMenu.btExport.setSelected(false);
		}
		panel.removeAll();
		panel.add(organiser.getContentPane(), MIG.GROWX);
		titleUpdate();
	}

	/**
	 * update the title
	 */
	public void titleUpdate() {
		StringBuilder b = new StringBuilder();
		String modif = " ";//album.getTable().isModified() ? "*" : " ";
		b.append(modif);
		b.append(Const.getFullName()).append(" ");
		if (appMenu.btSorter != null) {
			if (appMenu.btSorter.isSelected()) {
				b.append("(").append(App.preferences.photosDirGet()).append(")");
			} else if (appMenu.btAlbum.isSelected()) {
				b.append("(").append(album.diapoNameGet()).append(")");
			} else if (appMenu.btExport.isSelected()) {
				b.append(" ").append(I18N.getMsg("export"));
			} else {
				b.append("nettoyer");
			}
		}

		b.append(modif);
		setTitle(b.toString());
		this.invalidate();
		try {
			this.validate();
		} catch (Exception ex) {
		}
		this.repaint();
	}

	/**
	 * get the diaporama parameters
	 *
	 * @return
	 */
	public DiapoParam diapoParamGet() {
		if (album.diapoParamGet() == null) {
			LOG.trace(TT + "albumParamGet() albumParam is null, create new");
			album.diapoParamCreate();
		}
		return album.diapoParamGet();
	}

	/**
	 * get the album title
	 *
	 * @return
	 */
	public String diapoTitleGet() {
		return album.diapoTitleGet();
	}

	/**
	 * do the diaporama
	 */
	public void doDiaporama() {
		if (album.getTable().getRowCount() < 1) {
			return;
		}
		//DiapoParamDlg dlg = new DiapoParamDlg(this);
		//dlg.setVisible(true);
		DiapoPreview diaporama = new DiapoPreview(this);
		doDiapo();
		this.appMenu.btAlbum.setSelected(true);
	}

	/**
	 * set the current file
	 *
	 * @param file
	 */
	public void fileSet(File file) {
		this.file = file;
		album.fileSet(file);
	}

	/**
	 * get the current file
	 *
	 * @return
	 */
	public File fileGet() {
		return file;
	}

	/**
	 * set the current album root directory
	 *
	 * @param file
	 */
	public void photosDirSet(File file) {
		album.setPhotosDir(file);
	}

	/**
	 * do the export
	 */
	public void doExport() {
		appMenu.btSorter.setSelected(false);
		appMenu.btAlbum.setSelected(false);
		Export export = new Export(this);
		panel.removeAll();
		panel.add(export.getContentPane(), MIG.GROW);
		titleUpdate();
	}

	/**
	 * get the current album frame
	 *
	 * @return
	 */
	public Album albumGet() {
		return album;
	}

	/**
	 * refresh the album frame
	 */
	public void albumRefresh() {
		album.refreshAll();
	}

	public void printDo() {
		print = new Print(this);
		panel.setVisible(false);
		add(print);
	}

	public void printHide() {
		panel.setVisible(true);
		this.remove(print);
	}

}
