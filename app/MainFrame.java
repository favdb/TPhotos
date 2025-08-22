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

import api.mig.swing.MigLayout;
import app.album.Album;
import app.album.AlbumParam;
import app.album.Diaporama;
import app.export.Export;
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
import tools.MIG;

/**
 *
 * @author favdb
 */
public class MainFrame extends JFrame {

	private static final String TT = "MainFrame.";

	public MainMenu appMenu;
	public JPanel panel;
	private Organise organiser;
	private Album album;

	public MainFrame() {
		super();
		initialize();
	}

	private void initialize() {
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				App.exit();
			}
		});
		setIconImage(IconUtil.getIconImage(ICONS.K.TPHOTOS.toString()));
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.GAP0, MIG.INS0, MIG.WRAP1)));
		Dimension sz = Toolkit.getDefaultToolkit().getScreenSize();
		this.setMaximumSize(sz);
		appMenu = new MainMenu();
		add(appMenu.getToolBar(), MIG.get(MIG.GROWX));
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

	private void doExit() {
		//LOG.trace(TT + "doExit()");
		if (album.getTable().isModified()) {
			album.save();
		}
		dispose();
		App.exit();
	}

	public void doAlbum() {
		//LOG.trace(TT + "doAlbum()");
		if (appMenu.btSorter != null) {
			appMenu.btSorter.setSelected(false);
			appMenu.btExport.setSelected(false);
		}
		panel.removeAll();
		panel.add(album.getContentPane(), MIG.GROW);
		updateTitle();
	}

	public Album getAlbumPanel() {
		return album;
	}

	public void doSorter() {
		//LOG.trace(TT + "doSorter()");
		if (appMenu.btAlbum != null) {
			appMenu.btAlbum.setSelected(false);
			appMenu.btExport.setSelected(false);
		}
		panel.removeAll();
		panel.add(organiser.getContentPane(), MIG.GROWX);
		updateTitle();
	}

	public void updateTitle() {
		StringBuilder b = new StringBuilder();
		String modif = album.getTable().isModified() ? "*" : " ";
		b.append(modif);
		b.append(Const.getFullName()).append(" ");
		if (appMenu.btSorter != null) {
			if (appMenu.btSorter.isSelected()) {
				b.append("(").append(App.preferences.photosDirGet()).append(")");
			} else if (appMenu.btAlbum.isSelected()) {
				b.append("(").append(album.albumNameGet()).append(")");
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

	public AlbumParam albumParamGet() {
		if (album.getAlbumParam() == null) {
			LOG.trace(TT + "albumParamGet() albumParam is null, create new");
			album.albumParamCreate();
		}
		return album.getAlbumParam();
	}

	/**
	 * get the album title
	 *
	 * @return
	 */
	public String albumTitleGet() {
		return album.albumTitleGet();
	}

	public void doDiaporama() {
		if (album.getTable().getRowCount() < 1) {
			return;
		}
		//DiapoParamDlg dlg = new DiapoParamDlg(this);
		//dlg.setVisible(true);
		Diaporama diaporama = new Diaporama(this);
		doAlbum();
		this.appMenu.btAlbum.setSelected(true);
	}

	public void setAlbumFile(File file) {
		album.setAlbumFile(file);
	}

	public void setPhotosDir(File file) {
		album.setPhotosDir(file);
	}

	public void exportDo() {
		appMenu.btSorter.setSelected(false);
		appMenu.btAlbum.setSelected(false);
		Export export = new Export(this);
		panel.removeAll();
		panel.add(export.getContentPane(), MIG.GROW);
		updateTitle();
	}

	void refreshAlbum() {
		album.refreshAll();
	}

}
