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
package app.album;

import api.mig.MIG;
import api.mig.swing.MigLayout;
import app.App;
import static app.album.AlbumTree.getSubdir;
import app.dialog.ChangeDateDlg;
import app.gallery.Gallery;
import app.gallery.ImageLabel;
import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import resources.icons.ICONS;
import resources.icons.IconButton;
import tools.LOG;
import tools.Ui;
import tools.file.FileUtil;
import tools.xml.Xml;

/**
 *
 * @author favdb
 */
public class Album extends JFrame {

	private static final String TT = "Album.";
	private static final int IMG_SIZE = 200;

	private AlbumParam param;
	// tree and table
	private AlbumTree tree;
	private Gallery gallery;
	private AlbumTable table;
	private JPanel pTree, pGallery, pTable;
	// other components
	private String albumName = "Album";
	private Xml xml;
	private JTextField title;
	private boolean imageAllowed;
	private Color originBK, originFG;
	private JSplitPane sp;
	private File curImg;
	private String curTxt;
	private Dimension curSz;
	private IconButton btAdd;

	public Album() {
		super();
		initialize();
	}

	/**
	 * set the Album name
	 *
	 * @param value
	 */
	public void albumNameSet(String value) {
		this.albumName = value;
	}

	/**
	 * get the Album name
	 *
	 * @return
	 */
	public String albumNameGet() {
		return table.getXml().getFile().getName();
	}

	/**
	 * initialize the panel
	 */
	private void initialize() {
		//LOG.trace(TT + "initialize()");
		originBK = this.getBackground();
		originFG = this.getForeground();
		String xmlAlbum = App.preferences.albumLastGet();
		if (xmlAlbum.isEmpty()) {
			xmlAlbum = "Album.xml";
		}
		xml = new Xml(App.preferences.photosDirGet() + File.separator + xmlAlbum);
		param = new AlbumParam(xml);
		JPanel ptree = initTree();
		initGallery();
		JPanel ptable = initTable();

		JPanel pgallery = new JPanel(new MigLayout(MIG.get(MIG.FILLX, MIG.INS0, MIG.WRAP1)));
		pgallery.setPreferredSize(new Dimension(800, 800));
		JToolBar tb = new JToolBar();
		tb.setFloatable(false);
		btAdd = new IconButton("", ICONS.K.PLUS, e -> btAddPhotos());
		btAdd.setEnabled(false);
		tb.add(btAdd, MIG.RIGHT);
		pgallery.add(tb, MIG.GROWX);
		pgallery.add(gallery, MIG.GROW);

		JSplitPane spRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pgallery, ptable);
		spRight.setResizeWeight(1.0);
		sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, ptree, spRight);
		sp.setResizeWeight(0.15);
		sp.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());

		setLayout(new MigLayout(MIG.FILLX));
		add(sp, MIG.GROW);

		table.getSelectionModel().addListSelectionListener((ListSelectionEvent event) -> {
			imageAllowed = false;
			if (table.getRowCount() == 0) {
				return;
			}
			int[] rows = table.getSelectedRows();
			if (rows == null || rows.length != 1) {
				return;
			}
		});
	}

	/**
	 * initialize the tree panel
	 *
	 * @return
	 */
	private JPanel initTree() {
		//LOG.trace(TT + "initTree()");
		pTree = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.INS0)));
		tree = new AlbumTree(this);
		tree.addTreeSelectionListener(e -> treeChanged());
		JScrollPane scroll = new JScrollPane(tree);
		scroll.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		int minWidth = Ui.getTextWidth(" 9999|99|99 ", tree.getFont());
		pTree.setMinimumSize(new Dimension(minWidth, 100));
		scroll.setMinimumSize(new Dimension(minWidth, 100));
		pTree.add(scroll, MIG.GROW);
		return pTree;
	}

	/**
	 * initalize the gallery panel
	 *
	 * @return
	 */
	private void initGallery() {
		gallery = new Gallery(this, new File(App.preferences.photosDirGet()));
		gallery.setPreferredSize(new Dimension(800, 800));
	}

	/**
	 * initialize the view
	 *
	 * @return
	 */
	private JPanel initTable() {
		//LOG.trace(TT + "initTable()");
		pTable = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.INS0, MIG.GAP + " 5"), "[grow]", "[][grow]"));
		JPanel ptitle = new JPanel(new MigLayout(MIG.FLOWX));
		ptitle.add(new JLabel(I18N.getColonMsg("album.title")), MIG.SPLIT2);
		ptitle.add(title = new JTextField(), MIG.get(MIG.SPAN, MIG.GROWX));
		title.setText(xml.getTitle());
		title.setColumns(32);
		title.addCaretListener(e -> titleChange());
		pTable.add(ptitle, MIG.get(MIG.GROWX));
		IconButton bt = new IconButton("", ICONS.K.PHOTO, "album.preview", e -> {
			JDialog dialog = new JDialog();
			dialog.setLayout(new MigLayout(MIG.FILL));
			dialog.setTitle("album.preview");
			dialog.setSize(1024, 600);
			dialog.setLocationRelativeTo(this.getParent());
			dialog.setModal(true);
			dialog.add(new Gallery(table), MIG.GROW);
			dialog.setVisible(true);
		});
		pTable.add(bt, MIG.SPAN);
		table = new AlbumTable(this);
		table.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		JScrollPane scroll = new JScrollPane(table);
		scroll.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		pTable.add(scroll, MIG.get(MIG.SPAN, MIG.GROW));
		pTable.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		int minWidth = Ui.getTextWidth("WW | Photo | Commentaire ", table.getFont());
		table.setMinimumSize(new Dimension(minWidth, 100));
		scroll.setMinimumSize(new Dimension(minWidth, 100));
		return pTable;
	}

	/**
	 * get the Album table
	 *
	 * @return
	 */
	public AlbumTable getTable() {
		return table;
	}

	/**
	 * action when tree selection changed
	 */
	private void treeChanged() {
		//LOG.trace(TT + "treeChanged()");
		String txt = "";
		File img = null;
		TreePath[] paths = tree.getSelectionPaths();
		if (paths != null && paths.length > 1) {
			txt = I18N.getMsg("photo.selected", paths.length);
		} else {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if (node == null) {
				return;
			}
			File imgFile = (File) node.getUserObject();
			if (imgFile.isDirectory()) {
				gallery.setRootdir(imgFile);
			}
			if (node.isLeaf() && imgFile.isFile()) {
				if (App.jpegIs(imgFile)) {
					img = imgFile;
				} else {
					LOG.trace("image not JPEG");
					return;
				}
			} else {
				int nb = App.jpegCount(imgFile);
				txt = String.format("%d %s", nb, I18N.getMsg(nb > 1 ? "photos" : "photo"));
			}
		}
		imageAllowed = true;
	}

	/**
	 * load the Album Table from current file
	 */
	public void loadTable() {
		table.load(xml);
		loadParam();
	}

	/**
	 * load the Album Table form in file
	 *
	 * @param file
	 */
	public void loadTable(File file) {
		xml = new Xml(file.getAbsolutePath());
		loadTable();
	}

	/**
	 * save the Table content
	 */
	public void save() {
		table.save(title.getText());
	}

	/**
	 * save preferences
	 */
	public void savePref() {
		param.updateXml(xml);
	}

	/**
	 * get the Table into XML format
	 *
	 * @return
	 */
	public Xml getXml() {
		return table.getXml();
	}

	/**
	 * get Album parameters
	 *
	 * @return the param
	 */
	public AlbumParam getAlbumParam() {
		return param;
	}

	/**
	 * load Album parameters
	 */
	public void loadParam() {
		param = new AlbumParam(table.getXml());
	}

	/**
	 * create Album parameters
	 */
	public void albumParamCreate() {
		if (table != null && table.xml.isOpened()) {
			loadParam();
		}
	}

	public void setAlbumFile(File file) {
		if (table.isModified()) {
			table.save(title.getText());
		}
		xml = new Xml(file);
		table.load(xml);
		loadParam();
		title.setText(xml.getTitle());
		tree.reload();
	}

	/**
	 * set the Photos directory
	 *
	 * @param file
	 */
	public void setPhotosDir(File file) {
		setAlbumFile(new File(App.preferences.photosDirGet() + File.separator + "Album.xml"));
	}

	/**
	 * refresh the panel
	 */
	public void refreshAll() {
		tree.reload();
		gallery.refresh();
		setAlbumFile(new File(App.preferences.photosDirGet() + File.separator + "Album.xml"));
	}

	/**
	 * add selected photos to the current album
	 */
	public void btAddPhotos() {
		List<ImageLabel> imgList = gallery.getImgList();
		for (ImageLabel il : imgList) {
			File f = il.getFile();
			if (il.getSel() == ImageLabel.SEL) {
				table.addRow(new AlbumItem(table.getRowCount() + 1, param.getComment(f), f));
				il.setSel(ImageLabel.SEL_ALBUM);
			}
		}
		refreshAll();
	}

	/**
	 * action for button to add folder photos to the current album
	 */
	public void btAddAction() {
		//LOG.trace(TT + "btAddAction()");
		if (!AlbumParamDlg.showing(this, true)) {
			return;
		}
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (node == null) {
			return;
		}
		List<AlbumItem> treeItems = new ArrayList<>();
		TreePath[] paths = tree.getSelectionPaths();
		for (TreePath path : tree.getSelectionPaths()) {
			String nf = path.getLastPathComponent().toString();
			File fl = new File(nf);
			if (fl.isDirectory()) {
				addDir(fl, treeItems);
			} else if (App.jpegIs(fl)) {
				addFile(fl, treeItems);
			}
		}
		if (!treeItems.isEmpty()) {
			Collections.sort(treeItems, (AlbumItem f1, AlbumItem f2)
					-> f1.file.getAbsolutePath().compareTo(f2.file.getAbsolutePath()));
			int n = table.getRowCount() + 1;
			for (AlbumItem item : treeItems) {
				item.id = n++;
				table.addRow(item);
			}
			refreshAll();
		}
	}

	/**
	 * action to change a comment
	 */
	public void changeComments() {
		//LOG.trace(TT + "changeComments()");
		//show comment dialog to get new comment template
		AlbumParamDlg dlg = new AlbumParamDlg(this, false);
		dlg.setVisible(true);
		if (dlg.isCanceled()) {
			return;
		}
		String newComment = dlg.getComment();
		AlbumTable tb = getTable();
		int[] rows = tb.getSelectedRows();
		LOG.log("change comment for " + rows.length + " rows, comment=\"" + newComment + "\"");
		for (int i = 0; i < rows.length; i++) {
			tb.updateComment(rows[i], newComment);
		}
	}

	/**
	 * action to add a directory
	 *
	 * @param dir
	 * @param treeItems
	 */
	private void addDir(File dir, List<AlbumItem> treeItems) {
		//LOG.trace(TT + "addDir(dir=" + dir.getName() + ")");
		File[] files = dir.listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				addDir(f, treeItems);
			}
			if (App.jpegIs(f)) {
				addFile(f, treeItems);
			}
		}
	}

	/**
	 * add a file into the tree
	 *
	 * @param file
	 * @param treeItems
	 */
	private void addFile(File file, List<AlbumItem> treeItems) {
		//LOG.trace(TT + "addFile(file=" + file.getName() + ")");
		if (App.jpegIs(file)) {
			treeItems.add(new AlbumItem(treeItems.size() + 1, param.getComment(file), file));
		}
	}

	/**
	 * replace the given image file by the new given file
	 *
	 * @param oldFile
	 * @param newFile
	 */
	public void replace(File oldFile, File newFile) {
		for (int i = 0; i < table.getRowCount(); i++) {
			if (((File) table.getValueAt(i, 1)).equals(oldFile)) {
				table.setValueAt(newFile, i, 1);
			}
		}
	}

	/**
	 * showing the popup menu
	 *
	 * @param e
	 */
	public void showPopup(MouseEvent e, DefaultMutableTreeNode node) {
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem item1 = new JMenuItem(I18N.getMsg("album.add"));
		item1.addActionListener(act -> btAddAction());
		popupMenu.add(item1);
		if (node.isLeaf()) {
			JMenuItem item2 = new JMenuItem(I18N.getMsg("date.change"));
			item2.addActionListener(act -> changeDate());
			popupMenu.add(item2);
		}
		popupMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	public void updateBtAdd(boolean b) {
		btAdd.setEnabled(b);
	}

	public void changeDate(File file) {
		if (file != null && App.jpegIs(file)) {
			ChangeDateDlg dlg = new ChangeDateDlg(this, file);
			dlg.setVisible(true);
			if (!dlg.isCancel()) {
				String origin = FileUtil.removeExtension(file.getName());
				String date = dlg.getDate();
				if (!date.equals(origin)) try {
					int mode = dlg.getMode();
					String subdir = getSubdir(date, mode);
					File out = new File(App.preferences.photosDirGet() + File.separator
							+ subdir + File.separator + date + ".jpg");
					out.mkdirs();
					Files.move(file.toPath(), out.toPath(), REPLACE_EXISTING);
					gallery.refresh();
				} catch (IOException ex) {
					LOG.err(TT + "changeDate() move error", ex);
				}
			}
		}
	}

	/**
	 * change the date-time of the selected image
	 */
	public void changeDate() {
		//LOG.trace(TT + "changeDate()");
		// vérification qu'on est bien sur une image*
		TreePath[] paths = tree.getSelectionPaths();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (node == null) {
			return;
		}
		if (node.isLeaf()) {
			File imgFile = (File) node.getUserObject();
			if (App.jpegIs(imgFile)) {
				ChangeDateDlg dlg = new ChangeDateDlg(this, imgFile);
				dlg.setVisible(true);
				if (!dlg.isCancel()) {
					String origin = FileUtil.removeExtension(imgFile.getName());
					String date = dlg.getDate();
					if (!date.equals(origin)) try {
						int mode = dlg.getMode();
						String subdir = getSubdir(date, mode);
						File out = new File(App.preferences.photosDirGet() + File.separator
								+ subdir + File.separator + date + ".jpg");
						out.mkdirs();
						Files.move(imgFile.toPath(), out.toPath(), REPLACE_EXISTING);
						tree.reload();
						tree.select(out);
					} catch (IOException ex) {
						LOG.err(TT + "changeDate() move error", ex);
					}
				}
			}
		}
	}

	/**
	 * change Album title
	 */
	private void titleChange() {
		if (!title.getText().equals(xml.getTitle())) {
			table.setModified();
		}
	}

	/**
	 * get the Album title
	 *
	 * @return
	 */
	public String albumTitleGet() {
		return xml.getTitle();
	}

	public Gallery getGallery() {
		return gallery;
	}

	/**
	 * add the given photo to the current album
	 *
	 * @param lb
	 */
	public void photoAdd(ImageLabel il) {
		if (!AlbumParamDlg.showing(this, true)) {
			return;
		}
		File f = il.getFile();
		table.addRow(new AlbumItem(table.getRowCount() + 1, param.getComment(f), f));
		save();
		gallery.refresh();
	}

	/**
	 * remove the given photo from the current album
	 *
	 * @param lb
	 */
	public void photoRemove(ImageLabel lb) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		for (int row = 0; row < table.getRowCount(); row++) {
			File f = (File) model.getValueAt(row, 1);
			if (f.getAbsolutePath().equals(lb.getFile().getAbsolutePath())) {
				model.removeRow(row);
				save();
				gallery.refresh();
				break;
			}
		}
	}

}
