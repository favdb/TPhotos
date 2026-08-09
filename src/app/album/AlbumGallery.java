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
package app.album;

import api.mig.MIG;
import api.mig.swing.MigLayout;
import app.App;
import app.ui.MainFrame;
import java.awt.Desktop;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JViewport;
import javax.swing.SwingWorker;
import javax.swing.table.TableModel;
import resources.icons.ICONS;
import tools.ImageUtil;
import tools.LOG;
import tools.Ui;

/**
 * gallery panel with asynchronous load
 *
 * @author favdb
 */
public class AlbumGallery extends JPanel {

	private static final String TT = "Gallery.";

	private static String T_ALBUM = "album", T_TABLE = "table";
	private String type = T_ALBUM;
	private File rootdir = null;
	private Album album;
	private AlbumTable table = null;
	private JPanel pGallery;
	private JScrollPane scroller;
	private final List<AlbumGalleryCell> galleryCells = new ArrayList<>();
	private ComponentAdapter resizeListener = null;
	private SwingWorker<Void, Integer> currentWorker = null;

	public AlbumGallery() {
		super();
	}

	public AlbumGallery(Album album, File rootdir) {
		super();
		this.album = album;
		this.table = null;
		this.rootdir = rootdir;
		this.type = T_ALBUM;
		initialize();
	}

	public AlbumGallery(AlbumTable table) {
		super();
		this.album = null;
		this.table = table;
		this.type = T_TABLE;
		initialize();
	}

	public MainFrame mainFrameGet() {
		return App.mainFrame;
	}

	private void initialize() {
		//LOG.trace(TT+"initialize()");
		setLayout(new MigLayout(MIG.get(/*MIG.FILL, */MIG.INS0, MIG.GAP1)));
		int nbcols = nbColsGet();

		if (pGallery == null) {
			pGallery = new JPanel();
		}
		pGallery.removeAll();
		pGallery.setLayout(new MigLayout(MIG.get("al left top", /*MIG.FILL,*/
				MIG.INS0, MIG.GAP + " 6", MIG.WRAP + " " + nbcols)));
		if (scroller == null) {
			scroller = new JScrollPane(pGallery);
			scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scroller.getVerticalScrollBar().setUnitIncrement(16);
			scroller.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
			scroller.setBorder(BorderFactory.createEmptyBorder());
		} else {
			remove(scroller);
		}
		add(scroller, MIG.get(MIG.GROW, MIG.PUSH, "top, left"));
		photosLoad();
		if (resizeListener != null) {
			removeComponentListener(resizeListener);
		}
		resizeListener = new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				layoutUpdate();
			}
		};
		addComponentListener(resizeListener);
	}

	/**
	 * get size of columns
	 *
	 * @return
	 */
	private int nbColsGet() {
		int textwidth = Ui.getTextWidth(" 99/99/9999 ", this.getFont());
		int currentWidth = this.getWidth();
		if (currentWidth <= 0) {
			currentWidth = 800;
		}
		return Math.max(1, currentWidth / (textwidth + 10));
	}

	/**
	 * update layout
	 */
	private void layoutUpdate() {
		if (pGallery == null || scroller == null) {
			return;
		}
		initialize();
	}

	/**
	 * load photos
	 */
	private void photosLoad() {
		if (rootdir != null && rootdir.exists()) {
			ImageUtil.cleanCache(rootdir);
		}
		if (currentWorker != null && !currentWorker.isDone()) {
			currentWorker.cancel(true);
		}
		pGallery.removeAll();
		galleryCells.clear();
		if (rootdir == null && !type.equals(T_TABLE)) {
			pGallery.revalidate();
			pGallery.repaint();
			return;
		}
		List<File> filesToLoad = new ArrayList<>();
		if (type.equals(T_TABLE)) {
			for (int row = 0; row < table.getRowCount(); row++) {
				File f = (File) table.getModel().getValueAt(row, 1);
				if (f != null && f.exists()) {
					filesToLoad.add(f);
				}
			}
		} else if (rootdir != null && rootdir.exists()) {
			// On charge le dossier sélectionné et l'ensemble de son arborescence
			collectPhotos(rootdir, filesToLoad);
			Collections.sort(filesToLoad, (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));
		}
		for (File f : filesToLoad) {
			AlbumGalleryCell il = new AlbumGalleryCell(this, f, "", table == null);
			galleryCells.add(il);
			pGallery.add(il);
		}
		pGallery.revalidate();
		pGallery.repaint();
		currentWorker = new SwingWorker<Void, Integer>() {
			@Override
			protected Void doInBackground() throws Exception {
				for (int i = 0; i < galleryCells.size(); i++) {
					if (isCancelled()) {
						return null;
					}
					galleryCells.get(i).loadThumbnail();
					publish(i);
				}
				return null;
			}

			@Override
			protected void process(List<Integer> chunks) {
				try {
					for (Integer index : chunks) {
						galleryCells.get(index).repaint();
					}
					inDiapoSet();
				} catch (Exception ex) {
				}
			}
		};
		currentWorker.execute();
	}

	/**
	 * Parcours récursif pour collecter tous les fichiers JPEG sous le dossier sélectionné
	 *
	 * @param dir dossier à explorer
	 * @param result liste cible des fichiers images trouvés
	 */
	private void collectPhotos(File dir, List<File> result) {
		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}
		for (File f : files) {
			if (f.isDirectory()) {
				// Explore tous les sous-dossiers (normés ou non) situés sous le dossier sélectionné
				collectPhotos(f, result);
			} else if (App.jpegIs(f)) {
				result.add(f);
			}
		}
	}

	/**
	 * parse to find JPEG files
	 *
	 * @param dir folder to parse
	 * @param result target List
	 * @param isRoot true si c'est le dossier sélectionné dans l'arbre
	 */
	private void collectPhotos(File dir, List<File> result, boolean isRoot) {
		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}
		for (File f : files) {
			if (f.isDirectory()) {
				// Si c'est un sous-dossier hors-norme, on explore toujours.
				// Si c'est un dossier normé (ex: jour), on explore seulement si on est en train
				// d'explorer sous le dossier racine sélectionné (isRoot = true).
				if (!isNormedDir(f) || isRoot) {
					collectPhotos(f, result, false);
				}
			} else if (App.jpegIs(f)) {
				result.add(f);
			}
		}
	}

	/**
	 * Vérifie si le dossier suit la norme numérique (Année / Mois / Jour)
	 */
	private boolean isNormedDir(File dir) {
		String name = dir.getName();
		// Vérifie si le nom du dossier est purement numérique (ex: 2026, 08, 25)
		return name.matches("\\d+");
	}

	/**
	 * change root directory
	 *
	 * @param rootdir
	 */
	public void rootdirSet(File rootdir) {
		this.rootdir = rootdir;
		this.table = null;
		initialize();
	}

	/**
	 * change album table
	 *
	 * @param table
	 */
	public void tableSet(AlbumTable table) {
		this.table = table;
		this.rootdir = null;
		initialize();
	}

	/**
	 * set the AlbumGalleryCell to SEL_ALBUM when file is in diapo
	 */
	public void inDiapoSet() {
		//LOG.trace(TT + "inDiapoSet()");
		if (rootdir != null && rootdir.isDirectory()) {
			List<File> falbum = new ArrayList<>();
			TableModel model = album.getTable().getModel();
			for (int row = 0; row < model.getRowCount(); row++) {
				File file = (File) model.getValueAt(row, 1);
				falbum.add(file);
			}
			for (File f : falbum) {
				for (AlbumGalleryCell lb : galleryCells) {
					if (f.getName().equals(lb.fileGet().getName())) {
						lb.setSel(AlbumGalleryCell.SEL_ALBUM);
					}
				}
			}
		}
	}

	/**
	 * refresh
	 */
	public void refresh() {
		initialize();
		btAddUpdate();
	}

	/**
	 * update the add button
	 */
	public void btAddUpdate() {
		album.updateBtAdd(false);
		//album.updateBtAdd(album.xmlGet().albumGet().photosAllGet().size() > 0);
		for (AlbumGalleryCell il : galleryCells) {
			if (il.getSel() == AlbumGalleryCell.SEL) {
				album.updateBtAdd(true);
				break;
			}
		}
	}

	/**
	 * show popup menu
	 *
	 * @param e
	 * @param il
	 */
	public void popupShow(MouseEvent e, AlbumGalleryCell il) {
		//LOG.trace(TT + "popupShow(il=" + il.toString() + ")");
		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.add(Ui.initMenuItem(ICONS.K.PHOTO, "menu.file_album_open",
				act -> {
					try {
						Desktop.getDesktop().open(il.fileGet());
					} catch (IOException ex) {
						LOG.err("unable to open file", ex);
					}
				}));
		if (table == null) {
			if (il.getSel() != AlbumGalleryCell.SEL_ALBUM) {
				popupMenu.add(Ui.initMenuItem(ICONS.K.PLUS, "album.add",
						act -> album.photoAdd(il)));
				popupMenu.add(new JSeparator());
				popupMenu.add(Ui.initMenuItem(ICONS.K.CALENDAR, "date.change",
						act -> album.changeDate(il.fileGet())));
				popupMenu.add(Ui.initMenuItem(ICONS.K.CANCEL, "action.delete",
						act -> {
							il.fileGet().delete();
							refresh();
						}));
			} else {
				popupMenu.add(Ui.initMenuItem(ICONS.K.MINUS, "album.remove",
						act -> album.photoRemove(il)));
			}
		}
		popupMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	/**
	 * get the AlbumGalleryCell list
	 *
	 * @return
	 */
	public List<AlbumGalleryCell> cellListGet() {
		return galleryCells;
	}

	/**
	 * add an image label
	 *
	 * @param lb
	 */
	public void cellAdd(AlbumGalleryCell lb) {
		if (table != null) {
			return;
		}
		album.photoAdd(lb);
	}

	/**
	 * remove an image label
	 *
	 * @param lb
	 */
	public void cellRemove(AlbumGalleryCell lb) {
		if (table != null) {
			return;
		}
		album.photoRemove(lb);
	}

}
