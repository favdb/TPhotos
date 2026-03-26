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

import api.mig.MIG;
import api.mig.swing.MigLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import resources.icons.ICONS;
import resources.icons.ICONS.K;
import tools.Ui;

/**
 * main menu class
 *
 * @author favdb
 */
public class MainMenu {

	private JMenuBar menuBar;
	public JToggleButton btSorter, btAlbum, btExport, btShow;
	public JButton btDiapo, btPhotos, btAbout;

	public MainMenu() {
		initialize();
	}

	/**
	 * initialize
	 */
	private void initialize() {
	}

	/**
	 * get the toolbar
	 *
	 * @return
	 */
	public JPanel getToolBar() {
		JPanel p = new JPanel(new MigLayout(MIG.get("inset 0 0 0 10", MIG.FILL), "[grow][]"));
		p.setBorder(BorderFactory.createRaisedBevelBorder());
		JToolBar tb = new JToolBar();
		//tb.setLayout(new MigLayout(MIG.FILLX));
		tb.setFloatable(false);
		p.add(initActions());
		p.add(btAbout = Ui.initIconButton(" menu.help_about", ICONS.K.HELP, e -> App.aboutDo()), MIG.RIGHT);
		return p;
	}

	/**
	 * initialize JPanel and actions
	 *
	 * @return
	 */
	private JPanel initActions() {
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.FILLX)));
		p.setOpaque(false);
		String space = "   ";
		p.add(btPhotos = Ui.initIconButton(" menu.file", ICONS.K.OPTIONS, null));
		btPhotos.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				filePopup().show(e.getComponent(), e.getX(), e.getY());
			}
		});
		p.add(btSorter = Ui.initToggleButton("app.organiser", true, e -> App.sorterDo()));
		p.add(btAlbum = Ui.initToggleButton("app.album", false, e -> App.albumDo()));
		p.add(btDiapo = Ui.initButton("app.diapo", ICONS.K.PIC, e -> App.diapoDo()));
		btDiapo.setVisible(false);
		p.add(btExport = Ui.initToggleButton("export", ICONS.K.F_EXPORT, false, e -> App.exportDo()));
		btExport.setVisible(false);
		return p;
	}

	/**
	 * popup to choose the action
	 *
	 * @return
	 */
	private static JPopupMenu filePopup() {
		JPopupMenu filePopup = new JPopupMenu();
		//---------------------Photos directory
		JMenuItem openPhotoDir = Ui.initMenuItem(K.FOLDER, "menu.file_photo", evt -> App.photosDirSelect());
		filePopup.add(openPhotoDir);
		//---------------------Album file
		JMenu menuAlbum = Ui.initMenu("menu.file_album");
		JMenuItem newAlbum = Ui.initMenuItem(K.F_NEW, "menu.file_album_new", evt -> App.albumFileNew());
		menuAlbum.add(newAlbum);
		JMenuItem openAlbum = Ui.initMenuItem(K.F_OPEN, "menu.file_album_open", evt -> App.albumFileOpen());
		menuAlbum.add(openAlbum);
		filePopup.add(menuAlbum);
		//---------------------Zoom option
		JMenuItem zoom = Ui.initMenuItem(K.COGS, "pref.zoom", evt -> App.zoom());
		filePopup.add(zoom);
		return filePopup;
	}

	/**
	 * get the JMenuBar
	 *
	 * @return
	 */
	public JMenuBar getMenuBar() {
		return menuBar;
	}

}
