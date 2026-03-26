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
import i18n.I18N;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingWorker;
import javax.swing.table.TableModel;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import tools.ImageUtil;
import tools.LOG;
import tools.Ui;

/**
 * gallery panel avec chargement asynchrone
 *
 * @author favdb
 */
public class Gallery extends JPanel {

	private static final String TT = "Gallery.";

	private int IMG_SZ = 128;
	private File rootdir = null;
	private Album album;
	private AlbumTable table = null;
	private JPanel pGallery;
	private JScrollPane scroller;
	private List<ImageLabel> imgList = new ArrayList<>();
	private java.awt.event.ComponentAdapter resizeListener = null;

	public Gallery() {
		super();
	}

	public Gallery(Album album, File rootdir) {
		super();
		this.album = album;
		this.rootdir = rootdir;
		initialize();
	}

	public Gallery(AlbumTable table) {
		super();
		this.table = table;
		initialize();
	}

	private void initialize() {
		//LOG.trace(TT+"initialize()");
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.INS0, MIG.GAP1)));
		int nbcols = computeNB_COLS();

		if (pGallery == null) {
			pGallery = new JPanel();
		}
		// cleanup the gallery panel before defining layout
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
		loadImages();
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
		IMG_SZ = textwidth - 4;
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

	private SwingWorker<Void, Integer> currentWorker = null;

	private void loadImages() {
		if (rootdir != null && rootdir.exists()) {
			ImageUtil.cleanCache(rootdir);
		}
		if (currentWorker != null && !currentWorker.isDone()) {
			currentWorker.cancel(true);
		}

		pGallery.removeAll();
		imgList.clear();

		final List<File> filesToLoad = new ArrayList<>();
		if (table != null) {
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
				setImagesAlbum();
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
	 * set the ImageLabel to SEL_ALBUM when file is in album
	 */
	public void setImagesAlbum() {
		//LOG.trace(TT + "setImagesAlbum()");
		if (rootdir != null && rootdir.isDirectory()) {
			List<File> falbum = new ArrayList<>();
			TableModel model = album.getTable().getModel();
			for (int row = 0; row < model.getRowCount(); row++) {
				File file = (File) model.getValueAt(row, 1);
				falbum.add(file);
			}
			for (File f : falbum) {
				for (ImageLabel lb : imgList) {
					if (f.getName().equals(lb.getFile().getName())) {
						lb.setSel(ImageLabel.SEL_ALBUM);
					}
				}
			}
		}
	}

	public void refresh() {
		initialize();
		updateBtAdd();
	}

	public void updateBtAdd() {
		album.updateBtAdd(false);
		for (ImageLabel il : imgList) {
			if (il.getSel() == ImageLabel.SEL) {
				album.updateBtAdd(true);
				break;
			}
		}
	}

	public void showPopup(MouseEvent e, ImageLabel il) {
		//LOG.trace(TT + "showPopup(il=" + il.toString() + ")");
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem item1 = new JMenuItem(I18N.getMsg("date.change"));
		item1.addActionListener(act -> album.changeDate(il.getFile()));
		if (il.getSel() == ImageLabel.SEL_ALBUM) {
			item1 = new JMenuItem(I18N.getMsg("album.remove"));
			item1.addActionListener(act -> album.photoRemove(il));
		}
		popupMenu.add(item1);
		if (il.getSel() != ImageLabel.SEL_ALBUM) {
			JMenuItem itemDelete = new JMenuItem(I18N.getMsg("action.delete"));
			itemDelete.setIcon(IconUtil.getIconSmall(ICONS.K.CANCEL));
			itemDelete.addActionListener(al -> {
				LOG.trace("delete: " + il.getFile().getAbsolutePath());
				il.getFile().delete();
				album.refreshAll();
			});
			popupMenu.add(itemDelete);
		}
		popupMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	public List<ImageLabel> getImgList() {
		return imgList;
	}

	public void albumAdd(ImageLabel lb) {
		album.photoAdd(lb);
	}

	public void albumRemove(ImageLabel lb) {
		album.photoRemove(lb);
	}

}
