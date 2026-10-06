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

import app.App;
import app.MainFrame;
import app.media.Media;
import app.resources.icons.ICONS;
import app.tools.GBC;
import app.tools.ImageUtil;
import app.tools.LOG;
import app.tools.Ui;
import app.xml.XmlAlbumItem;
import java.awt.Desktop;
import java.awt.FontMetrics;
import java.awt.GridBagLayout;
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

/**
 * class for the gallery panel with asynchronous load
 *
 * @author favdb
 */
public class AlbumGallery extends JPanel {

	private static final String TT = "AlbumGallery.";

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

	/**
	 * Calcule la largeur d'une cellule en fonction de la taille du texte de la date
	 * (JJ/MM/AAAA)
	 *
	 * @return largeur minimale calculée
	 */
	public int imgSzGet() {
		FontMetrics fm = getFontMetrics(getFont());
		int textWidth = fm != null ? fm.stringWidth("99/99/9999") : 100;
		// Ajout de marges et de la bordure (2px * 2) pour s'assurer que le texte ne soit pas tronqué
		return Math.max(120, textWidth + 16);
	}

	/**
	 * get the MainFrame
	 *
	 * @return
	 */
	public MainFrame mainFrameGet() {
		return App.mainFrame;
	}

	/**
	 * initialization
	 */
	private void initialize() {
		//LOG.trace(TT+"initialize()");
		setLayout(new GridBagLayout());
		if (pGallery == null) {
			pGallery = new JPanel();
		}
		pGallery.removeAll();
		pGallery.setLayout(new GridBagLayout());
		if (scroller == null) {
			scroller = new JScrollPane(pGallery);
			scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scroller.getVerticalScrollBar().setUnitIncrement(16);
			scroller.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
			scroller.setBorder(BorderFactory.createEmptyBorder());
		} else {
			remove(scroller);
		}
		add(scroller, new GBC("0, 0, fill b, wx 1.0, wy 1.0"));
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
	 * get number of columns
	 *
	 * @return
	 */
	private int nbColsGet() {
		int cellwidth = imgSzGet();
		int currentWidth = this.getWidth();
		if (currentWidth <= 0) {
			currentWidth = 800;
		}
		return Math.max(1, (currentWidth / (cellwidth + 4)));
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
			collectPhotos(rootdir, filesToLoad);
			Collections.sort(filesToLoad, (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));
		}
		int nbCols = nbColsGet();
		int row = 0;
		int col = 0;
		for (File f : filesToLoad) {
			AlbumGalleryCell il = new AlbumGalleryCell(this, f, "", table == null);
			if (!Media.jpegIs(f)) {
				il.selAllow(false);
			}
			galleryCells.add(il);
			pGallery.add(il, new GBC(row + ", " + col + ", anchor nw, ins 3 3 3 3"));
			col++;
			if (col >= nbCols) {
				col = 0;
				row++;
			}
		}
		pGallery.add(new JPanel(), new GBC(row + 1 + ", 0, width " + nbCols + ", wx 1.0, wy 1.0"));
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
					initDiapoSet();
				} catch (Exception ex) {
				}
			}
		};
		currentWorker.execute();
	}

	/**
	 * Recursive files collect in selected sub-directory for all JPEG
	 *
	 * @param dir sub-directorys to explore
	 * @param result target list for finded images
	 */
	private void collectPhotos(File dir, List<File> result) {
		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}
		for (File f : files) {
			if (f.isDirectory()) {
				collectPhotos(f, result);
			} else if (Media.jpegIs(f)) {
				result.add(f);
			} else if (Media.mp4Is(f)) {
				result.add(f);
			}
		}
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
	public void initDiapoSet() {
		//LOG.trace(TT + "initDiapoSet()");
		if (rootdir != null && rootdir.isDirectory()) {
			List<XmlAlbumItem> falbum = new ArrayList<>();
			TableModel model = album.tableGet().getModel();
			for (int row = 0; row < model.getRowCount(); row++) {
				XmlAlbumItem file = (XmlAlbumItem) model.getValueAt(row, 1);
				falbum.add(file);
			}
			for (XmlAlbumItem f : falbum) {
				for (AlbumGalleryCell lb : galleryCells) {
					if (f.fileGet().getName().equals(lb.fileGet().getName())) {
						lb.selSet(AlbumGalleryCell.SEL_ALBUM);
						lb.repaint();
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
		album.btAddUpdate(false);
		for (AlbumGalleryCell il : galleryCells) {
			if (il.selGet() == AlbumGalleryCell.SEL) {
				album.btAddUpdate(true);
				break;
			}
		}
	}

	/**
	 * show popup menu for the given AlbumGalleryCell
	 *
	 * @param e
	 * @param il
	 */
	public void showPopup(MouseEvent e, AlbumGalleryCell il) {
		//LOG.trace(TT + "showPopup(il=" + il.toString() + ")");
		List<AlbumGalleryCell> cx = new ArrayList<>();
		for (AlbumGalleryCell cell : galleryCells) {
			if (cell.selGet() == AlbumGalleryCell.SEL) {
				cx.add(cell);
			}
		}
		if (cx.size() > 1) {
			showPopupMulti(e, cx);
		} else {
			JPopupMenu popupMenu = new JPopupMenu();
			popupMenu.add(Ui.initMenuItem(ICONS.K.PHOTO, "menu.file_album_open",
					act -> {
						try {
							Desktop.getDesktop().open(il.fileGet());
						} catch (IOException ex) {
							LOG.err("unable to open file", ex);
						}
					}));
			if (!Media.mp4Is(il.fileGet())) {
				if (table == null) {
					if (il.selGet() != AlbumGalleryCell.SEL_ALBUM) {
						popupMenu.add(Ui.initMenuItem(ICONS.K.PLUS, "album.add",
								act -> album.photoAdd(il)));
						popupMenu.add(new JSeparator());
						popupMenu.add(Ui.initMenuItem(ICONS.K.CALENDAR, "date.change",
								act -> {
									album.dateChange(il.fileGet());
								}));
						popupMenu.add(Ui.initMenuItem(ICONS.K.CANCEL, "action.delete",
								act -> album.photoDelete(il)));
					} else {
						popupMenu.add(Ui.initMenuItem(ICONS.K.MINUS, "album.remove",
								act -> album.photoRemove(il)));
					}
				}
			}
			popupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/**
	 * show popup menu for multi selection
	 *
	 * @param e
	 * @param il
	 */
	public void showPopupMulti(MouseEvent e, List<AlbumGalleryCell> list) {
		LOG.trace(TT + "popupShow(list nb=" + list.size() + ")");
		JPopupMenu popupMenu = new JPopupMenu();
		if (album != null) {
			popupMenu.add(Ui.initMenuItem(ICONS.K.PLUS, "album.add",
					act -> album.photoAdd(list)));
			popupMenu.add(Ui.initMenuItem(ICONS.K.CALENDAR, "date.change",
					act -> album.dateChange(list)));
			popupMenu.add(Ui.initMenuItem(ICONS.K.CANCEL, "action.delete",
					act -> album.photoDelete(list)));
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
	 * add the given AlbumGalleryCellto the album
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
	 * remove the given AlbumGalleryCell from the album
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
