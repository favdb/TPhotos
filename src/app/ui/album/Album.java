/*
 * Copyright (C) 2024-2026 favdb
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
package app.ui.album;

import api.mig.MIG;
import api.mig.swing.MigLayout;
import app.App;
import app.Pref;
import static app.ui.album.AlbumTree.getSubdir;
import app.ui.diapo.DiapoParam;
import app.ui.dialog.ChangeDateDlg;
import app.ui.dialog.CommentParamDlg;
import app.ui.MainFrame;
import app.xml.Xml;
import app.xml.XmlAlbum;
import app.xml.XmlAlbumItem;
import app.i18n.I18N;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import app.resources.icons.ICONS;
import app.resources.icons.IconButton;
import app.tools.LOG;
import app.tools.Ui;
import app.tools.file.FileUtil;

/**
 *
 * @author favdb
 */
public class Album extends JFrame {

	private static final String TT = "Album.";
	private static final int IMG_SIZE = 200;

	public enum VIEW_MODE {
		YEAR(0, "organize.by_year"),
		MONTH(1, "organize.by_month"),
		DAY(2, "organize.by_day"),
		NONE(3, "organize.by_none");

		private final int level;
		private final String i18nKey;

		VIEW_MODE(int level, String i18nKey) {
			this.level = level;
			this.i18nKey = i18nKey;
		}

		public int getLevel() {
			return level;
		}

		public String getI18nKey() {
			return i18nKey;
		}
	}

	private DiapoParam param;
	// tree and table
	private AlbumTree tree;
	private AlbumGallery gallery;
	private AlbumTable table;
	private JPanel pTree, pGallery, pTable;
	private JComboBox<VIEW_MODE> cbViewMode;
	private VIEW_MODE currentViewMode = VIEW_MODE.MONTH;
	// other components
	private String albumName = "Album";
	private Xml xml;
	private JTextField title;
	private boolean imageAllowed;
	private Color originBK, originFG;
	private File curImg;
	private String curTxt;
	private Dimension curSz;
	private IconButton btAdd;
	private File file;

	public Album() {
		super();
		initialize();
	}

	/**
	 * set the Album name
	 *
	 * @param value
	 */
	public void diapoNameSet(String value) {
		this.albumName = value;
	}

	/**
	 * get the Album name
	 *
	 * @return
	 */
	public String diapoNameGet() {
		return table.xmlGet().fileGet().getName();
	}

	/**
	 * initialize the panel
	 */
	private void initialize() {
		setLayout(new MigLayout(MIG.FILL));
		currentViewMode = VIEW_MODE.values()[App.pref.albumViewLastGet()];
		// initialize original colors
		originBK = this.getBackground();
		originFG = this.getForeground();
		String xmlAlbum = App.pref.albumLastGet();
		if (xmlAlbum.isEmpty()) {
			xmlAlbum = "Album.xml";
		}
		file = new File(App.pref.photosDirGet() + File.separator + xmlAlbum);
		if (!file.exists()) {
			file = new File(App.pref.photosDirGet() + File.separator + "Album.xml");
		}
		xml = new Xml(file);
		param = new DiapoParam(xml);
		JPanel ptree = initTree();
		JPanel pgallery = initGallery();
		JPanel ptable = initTable();
		JSplitPane spRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pgallery, ptable);
		spRight.setResizeWeight(1.0);
		JSplitPane spAll = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, ptree, spRight);
		spAll.setResizeWeight(0.10);
		spAll.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
		add(spAll, MIG.GROW);
		String node = App.pref.getString(Pref.KEY.ALBUM_LASTNODE);
		if (!node.isEmpty()) {
			File sel = new File(App.pref.photosDirGet(), node);
			tree.select(sel);
			gallery.refresh();
		}
	}

	/**
	 * initialize the tree panel
	 *
	 * @return
	 */
	private JPanel initTree() {
		//LOG.trace(TT + "initTree()");
		pTree = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.INS0, MIG.WRAP1)));
		cbViewMode = new JComboBox<>(VIEW_MODE.values());
		cbViewMode.setSelectedItem(currentViewMode);
		cbViewMode.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> ls,
					Object val, int idx, boolean sel, boolean focus) {
				super.getListCellRendererComponent(ls, val, idx, sel, focus);
				if (val instanceof VIEW_MODE) {
					setText(I18N.getMsg(((VIEW_MODE) val).getI18nKey()));
				}
				return this;
			}
		});
		cbViewMode.addActionListener(e -> {
			VIEW_MODE mode = (VIEW_MODE) cbViewMode.getSelectedItem();
			if (mode != null && mode != currentViewMode) {
				currentViewMode = mode;
				App.pref.albumViewLastSet(mode.level);
				tree.reload(currentViewMode);
				treeChanged();
			}
		});
		pTree.add(cbViewMode, MIG.get(MIG.GROWX, MIG.SPAN));
		tree = new AlbumTree(this);
		tree.addTreeSelectionListener(e -> treeChanged());
		JScrollPane scroll = new JScrollPane(tree);
		int minWidth = Ui.getTextWidth(" 9999/99/99 ", tree.getFont());
		scroll.setMinimumSize(new Dimension(minWidth, 100));
		pTree.add(scroll, MIG.get(MIG.GROW, MIG.PUSH));
		return pTree;
	}

	/**
	 * initialize the gallery panel
	 *
	 * @return
	 */
	private JPanel initGallery() {
		//LOG.trace(TT + "initGallery()");
		JPanel pgallery = new JPanel(new MigLayout(MIG.get(/*MIG.FILL, */MIG.INS0, MIG.WRAP1)));
		pgallery.setPreferredSize(new Dimension(800, 800));
		JToolBar tb = new JToolBar();
		tb.setFloatable(false);
		btAdd = new IconButton("", ICONS.K.PLUS, e -> btAddPhotos());
		btAdd.setEnabled(false);
		tb.add(btAdd, MIG.RIGHT);
		pgallery.add(tb, MIG.GROWX);
		gallery = new AlbumGallery(this, null);
		//gallery.setPreferredSize(new Dimension(800, 800));
		pgallery.add(gallery, MIG.get(MIG.GROW, MIG.PUSH));
		return pgallery;
	}

	/**
	 * initialize the view
	 *
	 * @return
	 */
	private JPanel initTable() {
		//LOG.trace(TT + "initTable()");
		pTable = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.INS0, MIG.GAP + " 5"),
				"[grow]", "[][grow]"));
		JPanel ptitle = new JPanel(new MigLayout(/*MIG.GROWX*/));
		ptitle.add(new JLabel(I18N.getColonMsg("album.title")), MIG.SPLIT2);
		ptitle.add(title = new JTextField(), MIG.get(MIG.SPAN, MIG.GROWX));
		XmlAlbum xmlAlbum = xml.albumGet();
		title.setText(xmlAlbum != null ? xmlAlbum.titleGet() : "");
		title.setColumns(32);
		title.addCaretListener(e -> titleChange());
		pTable.add(ptitle, MIG.get(MIG.GROWX));
		table = new AlbumTable(this);
		//table.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		JScrollPane scroll = new JScrollPane(table);
		//scroll.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		pTable.add(scroll, MIG.get(MIG.NEWLINE, MIG.SPAN, MIG.GROW));
		//pTable.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
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
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (node == null) {
			gallery.rootdirSet(null);
			return;
		}
		File imgFile = (File) node.getUserObject();
		if (imgFile.isDirectory()) {
			if (isSelectionAllowedForMode(imgFile)) {
				gallery.rootdirSet(imgFile);
				String path = imgFile.getPath()
						.replace(App.pref.photosDirGet(), "")
						.substring(1);
				App.pref.setString(Pref.KEY.ALBUM_LASTNODE, path);
			} else {
				gallery.rootdirSet(null);
			}
		} else {
			gallery.rootdirSet(null);
		}
		imageAllowed = true;
	}

	/**
	 * Vérifie si le dossier sélectionné correspond au niveau autorisé par le VIEW_MODE
	 * courant
	 */
	private boolean isSelectionAllowedForMode(File file) {
		if (!isNormedDir(file)) {
			return true;
		}

		int depth = getNormedDepth(file);
		int targetDepth = currentViewMode.getLevel() + 1;

		return depth >= targetDepth;
	}

	/**
	 * Détermine la profondeur d'un dossier normé
	 */
	private int getNormedDepth(File file) {
		int depth = 0;
		File current = file;
		File root = new File(App.pref.photosDirGet());
		while (current != null && !current.equals(root) && current.getName().matches("\\d+")) {
			depth++;
			current = current.getParentFile();
		}
		return depth;
	}

	/**
	 * Vérifie si le dossier est normé (numérique)
	 */
	private boolean isNormedDir(File dir) {
		return dir.getName().matches("\\d+");
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
		fileSet(file);
		xml = new Xml(file);
		loadTable();
	}

	public XmlAlbumItem tableRowGet(int i) {
		if (table == null || table.getRowCount() >= i) {
			return (XmlAlbumItem) table.getRow(i);
		}
		return null;
	}

	/**
	 * save the Table content
	 */
	public void save() {
		XmlAlbum xmlAlbum = xml.albumGet();
		if (xmlAlbum != null) {
			xmlAlbum.titleSet(title.getText());
		}
		table.save(title.getText());
	}

	/**
	 * save pref
	 */
	public void savePref() {
		param.updateXml(xml);
	}

	/**
	 * get the Album into XML format
	 *
	 * @return
	 */
	public Xml xmlGet() {
		return table.xmlGet();
	}

	/**
	 * get Album parameters
	 *
	 * @return the param
	 */
	public DiapoParam diapoParamGet() {
		return param;
	}

	/**
	 * load Album parameters
	 */
	public void loadParam() {
		param = new DiapoParam(table.xmlGet());
	}

	/**
	 * create Album parameters
	 */
	public void diapoParamCreate() {
		if (table != null && table.xml.isOpened()) {
			loadParam();
		}
	}

	/**
	 * set the current file
	 *
	 * @param file
	 */
	public void fileSet(File file) {
		if (table.isModified()) {
			table.save(title.getText());
		}
		this.file = file;
		xml = new Xml(file);
		table.load(xml);
		loadParam();
		XmlAlbum xmlAlbum = xml.albumGet();
		if (xmlAlbum != null) {
			title.setText(xmlAlbum.titleGet());
		}
		tree.reload(currentViewMode);
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
	 * set the Photos directory
	 *
	 * @param file
	 */
	public void setPhotosDir(File file) {
		fileSet(new File(App.pref.photosDirGet() + File.separator + "Album.xml"));
	}

	/**
	 * refresh the panel
	 */
	public void refreshAll() {
		tree.reload(currentViewMode);
		gallery.refresh();
		fileSet(new File(App.pref.photosDirGet() + File.separator + "Album.xml"));
	}

	/**
	 * add selected photos to the current album
	 */
	public void btAddPhotos() {
		if (CommentParamDlg.showing(this, true)) {
			param.setComment(xml.albumGet().getPrefComment());
		}
		List<AlbumGalleryCell> imgList = gallery.cellListGet();
		for (AlbumGalleryCell il : imgList) {
			File f = il.fileGet();
			if (il.getSel() == AlbumGalleryCell.SEL) {
				table.rowAdd(new XmlAlbumItem("" + (table.getRowCount() + 1),
						f.getAbsolutePath(), param.getComment(f)));
				il.setSel(AlbumGalleryCell.SEL_ALBUM);
			}
		}
		table.setModified();
		gallery.refresh();
	}

	/**
	 * action for button to add folder photos to the current album
	 */
	public void btAddAction() {
		if (CommentParamDlg.showing(this, true)) {
			//todo save comment template
		}
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (node == null) {
			return;
		}
		List<XmlAlbumItem> treeItems = new ArrayList<>();
		TreePath[] paths = tree.getSelectionPaths();
		for (TreePath path : paths) {
			String nf = path.getLastPathComponent().toString();
			File fl = new File(nf);
			if (fl.isDirectory()) {
				addDir(fl, treeItems);
			} else if (App.jpegIs(fl)) {
				addFile(fl, treeItems);
			}
		}
		if (!treeItems.isEmpty()) {
			Collections.sort(treeItems, (XmlAlbumItem f1, XmlAlbumItem f2)
					-> f1.photoFile().getAbsolutePath().compareTo(f2.photoFile().getAbsolutePath()));
			int n = table.getRowCount() + 1;
			for (XmlAlbumItem item : treeItems) {
				item.idSet("" + n++);
				table.rowAdd(item);
			}
			table.setModified();
			gallery.refresh();
		}
	}

	/**
	 * action to change a comment
	 */
	public void changeComments() {
		CommentParamDlg dlg = new CommentParamDlg(this, false);
		dlg.setVisible(true);
		if (dlg.isCanceled()) {
			return;
		}
		String newComment = dlg.getComment();
		AlbumTable tb = getTable();
		int[] rows = tb.getSelectedRows();
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
	private void addDir(File dir, List<XmlAlbumItem> treeItems) {
		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}
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
	private void addFile(File file, List<XmlAlbumItem> treeItems) {
		if (App.jpegIs(file)) {
			treeItems.add(new XmlAlbumItem("" + treeItems.size() + 1,
					param.getComment(file), file.getAbsolutePath()));
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
	 * @param node
	 */
	public void showPopup(MouseEvent e, DefaultMutableTreeNode node) {
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem item1 = new JMenuItem(I18N.getMsg("album.add"));
		item1.addActionListener(act -> btAddAction());
		popupMenu.add(item1);
		popupMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	/**
	 * update the enabled button to add to the album
	 *
	 * @param b
	 */
	public void updateBtAdd(boolean b) {
		btAdd.setEnabled(b);
	}

	/**
	 * change the date-time of the given file image
	 *
	 * @param file
	 */
	public void changeDate(File file) {
		if (file != null && App.jpegIs(file)) {
			ChangeDateDlg dlg = new ChangeDateDlg(this, file);
			dlg.setVisible(true);
			if (!dlg.isCancel()) {
				String origin = FileUtil.removeExtension(file.getName());
				String date = dlg.getDate(); // AAAAMMJJ_hhmmss
				if (!date.equals(origin)) try {
					String subdir = getSubdir(date, 2);
					File out = new File(App.pref.photosDirGet()
							+ File.separator + subdir
							+ File.separator + date + ".jpg");
					out.getParentFile().mkdirs();
					Files.move(file.toPath(), out.toPath(), REPLACE_EXISTING);
					gallery.refresh();
				} catch (IOException ex) {
					LOG.err(TT + "changeDate() move error", ex);
				}
			}
		}
	}

	/**
	 * change Album title
	 */
	private void titleChange() {
		XmlAlbum xmlAlbum = xml.albumGet();
		if (xmlAlbum != null && !title.getText().equals(xmlAlbum.titleGet())) {
			xmlAlbum.titleSet(title.getText());
			table.setModified();
		}
	}

	/**
	 * get the Album title
	 *
	 * @return
	 */
	public String diapoTitleGet() {
		XmlAlbum xmlAlbum = xml.albumGet();
		return xmlAlbum != null ? xmlAlbum.titleGet() : "";
	}

	public AlbumGallery getGallery() {
		return gallery;
	}

	/**
	 * add the given photo to the current album
	 *
	 * @param il
	 */
	public void photoAdd(AlbumGalleryCell il) {
		if (CommentParamDlg.showing(this, true)) {
			// save comment template
		}
		File f = il.fileGet();
		XmlAlbumItem item = new XmlAlbumItem("" + (table.getRowCount() + 1),
				f.getAbsolutePath(), param.getComment(f));
		table.rowAdd(item);
		table.setModified();
		save();
		gallery.refresh();
	}

	public void photoRemove(AlbumGalleryCell lb) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		String targetPath = lb.fileGet().getAbsolutePath();
		for (int row = 0; row < table.getRowCount(); row++) {
			Object val = model.getValueAt(row, 1);
			String itemPath = "";
			if (val instanceof XmlAlbumItem) {
				itemPath = ((XmlAlbumItem) val).fileGet().getAbsolutePath();
			} else if (val instanceof File) {
				itemPath = ((File) val).getAbsolutePath();
			}
			if (itemPath.equals(targetPath)) {
				table.rowRemove(row);
				table.setModified();
				save();
				gallery.refresh();
				break;
			}
		}
	}

	public MainFrame getMainFrame() {
		return App.mainFrame;
	}

	public VIEW_MODE currentViewModeGet() {
		return currentViewMode;
	}

	public void currentViewModeSet(VIEW_MODE mode) {
		this.currentViewMode = mode;
		if (cbViewMode != null) {
			cbViewMode.setSelectedItem(mode);
		}
	}

}
