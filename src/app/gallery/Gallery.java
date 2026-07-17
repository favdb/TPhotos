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
package app.gallery;

import api.mig.MIG;
import api.mig.swing.MigLayout;
import app.App;
import app.album.Album;
import app.album.AlbumTable;
import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
 * gallery panel with asynchroneus load
 *
 * @author favdb
 */
public class Gallery extends JPanel {

	private static final String TT = "Gallery.";

	private static String T_ALBUM = "album", T_TABLE = "table";
	private String type = T_ALBUM;
	private File rootdir = null;
	private Album album;
	private AlbumTable table = null;
	private JPanel pGallery;
	private JScrollPane scroller;
	private List<ImageLabel> imgList = new ArrayList<>();
	private java.awt.event.ComponentAdapter resizeListener = null;
	private SwingWorker<Void, Integer> currentWorker = null;

	public Gallery() {
		super();
	}

	public Gallery(Album album, File rootdir) {
		super();
		this.album = album;
		this.rootdir = rootdir;
		this.type = T_ALBUM;
		initialize();
	}

	public Gallery(AlbumTable table) {
		super();
		this.table = table;
		this.type = T_TABLE;
		initialize();
	}

	private void initialize() {
		//LOG.trace(TT+"initialize()");
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.INS0, MIG.GAP1)));
		int nbcols = computeNB_COLS();

		if (pGallery == null) {
			pGallery = new JPanel();
		}
		pGallery.removeAll();
		pGallery.setLayout(new MigLayout(MIG.get("al left top", MIG.INS0, MIG.GAP + " 6", MIG.WRAP + " " + nbcols)));
		if (scroller == null) {
			scroller = new JScrollPane(pGallery);
			scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scroller.getVerticalScrollBar().setUnitIncrement(16);
			scroller.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
			scroller.setBorder(BorderFactory.createEmptyBorder());
		} else {
			this.remove(scroller);
		}
		this.add(scroller, MIG.get(MIG.GROW, MIG.PUSH, "top, left"));
		imagesLoad();
		if (resizeListener != null) {
			this.removeComponentListener(resizeListener);
		}
		resizeListener = new java.awt.event.ComponentAdapter() {
			@Override
			public void componentResized(java.awt.event.ComponentEvent e) {
				updateLayout();
			}
		};
		this.addComponentListener(resizeListener);
	}

	private int computeNB_COLS() {
		int textwidth = Ui.getTextWidth(" 99/99/9999 ", this.getFont());
		int currentWidth = this.getWidth();
		if (currentWidth <= 0) {
			currentWidth = 800;
		}
		return Math.max(1, currentWidth / (textwidth + 10));
	}

	private void updateLayout() {
		if (pGallery == null || scroller == null) {
			return;
		}
		initialize();
	}

	private void imagesLoad() {
		if (rootdir != null && rootdir.exists()) {
			ImageUtil.cleanCache(rootdir);
		}
		if (currentWorker != null && !currentWorker.isDone()) {
			currentWorker.cancel(true);
		}
		pGallery.removeAll();
		imgList.clear();
		List<File> filesToLoad = new ArrayList<>();
		if (type.equals(T_TABLE)) {
			for (int row = 0; row < table.getRowCount(); row++) {
				File f = (File) table.getModel().getValueAt(row, 1);
				if (f != null && f.exists()) {
					filesToLoad.add(f);
				}
			}
		} else if (rootdir != null && rootdir.exists()) {
			File[] fls = rootdir.listFiles();
			if (fls != null) {
				Arrays.sort(fls, (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));
				for (File f : fls) {
					if (App.jpegIs(f)) {
						filesToLoad.add(f);
					}
				}
			}
		}
		for (File f : filesToLoad) {
			ImageLabel il = new ImageLabel(this, f, "", table == null);
			imgList.add(il);
			pGallery.add(il);
		}
		pGallery.revalidate();
		pGallery.repaint();
		currentWorker = new SwingWorker<Void, Integer>() {
			@Override
			protected Void doInBackground() throws Exception {
				for (int i = 0; i < imgList.size(); i++) {
					if (isCancelled()) {
						return null;
					}

					imgList.get(i).loadThumbnail();
					publish(i);
				}
				return null;
			}

			@Override
			protected void process(List<Integer> chunks) {
				for (Integer index : chunks) {
					imgList.get(index).repaint();
				}
				imagesDiapoSet();
			}
		};
		currentWorker.execute();
	}

	/**
	 * change root directory
	 *
	 * @param rootdir
	 */
	public void setRootdir(File rootdir) {
		this.rootdir = rootdir;
		this.table = null;
		initialize();
	}

	/**
	 * change album table
	 *
	 * @param table
	 */
	public void setTable(AlbumTable table) {
		this.table = table;
		this.rootdir = null;
		initialize();
	}

	/**
	 * set the ImageLabel to SEL_ALBUM when file is in diapo
	 */
	public void imagesDiapoSet() {
		//LOG.trace(TT + "imagesDiapoSet()");
		if (rootdir != null && rootdir.isDirectory()) {
			List<File> falbum = new ArrayList<>();
			TableModel model = album.getTable().getModel();
			for (int row = 0; row < model.getRowCount(); row++) {
				File file = (File) model.getValueAt(row, 1);
				falbum.add(file);
			}
			for (File f : falbum) {
				for (ImageLabel lb : imgList) {
					if (f.getName().equals(lb.fileGet().getName())) {
						lb.setSel(ImageLabel.SEL_ALBUM);
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
		updateBtAdd();
	}

	/**
	 * update the add button
	 */
	public void updateBtAdd() {
		album.updateBtAdd(false);
		for (ImageLabel il : imgList) {
			if (il.getSel() == ImageLabel.SEL) {
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
	public void showPopup(MouseEvent e, ImageLabel il) {
		//LOG.trace(TT + "showPopup(il=" + il.toString() + ")");
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
			if (il.getSel() != ImageLabel.SEL_ALBUM) {
				popupMenu.add(Ui.initMenuItem(ICONS.K.PLUS, "album.add",
						act -> album.photoAdd(il)));
				popupMenu.add(new JSeparator());
				popupMenu.add(Ui.initMenuItem(ICONS.K.CALENDAR, "date.change",
						act -> album.changeDate(il.fileGet())));
				popupMenu.add(Ui.initMenuItem(ICONS.K.CANCEL, "action.delete",
						al -> {
							LOG.trace("delete: " + il.fileGet().getAbsolutePath());
							il.fileGet().delete();
							album.refreshAll();
						}));
			} else {
				popupMenu.add(Ui.initMenuItem(ICONS.K.MINUS, "album.remove",
						act -> album.photoRemove(il)));
			}
		}
		popupMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	/**
	 * get the images list
	 *
	 * @return
	 */
	public List<ImageLabel> getImgList() {
		return imgList;
	}

	/**
	 * add an image label
	 *
	 * @param lb
	 */
	public void imageAdd(ImageLabel lb) {
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
	public void imageRemove(ImageLabel lb) {
		if (table != null) {
			return;
		}
		album.photoRemove(lb);
	}

}
