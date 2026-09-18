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
package app.album;

import app.App;
import static app.App.mainFrame;
import app.MainFrame;
import app.Pref;
import static app.album.AlbumTree.getSubdir;
import app.diapo.DiapoParam;
import app.i18n.I18N;
import app.media.Media;
import app.resources.icons.ICONS;
import app.resources.icons.IconButton;
import app.tools.GBC;
import app.tools.LOG;
import app.tools.Ui;
import app.tools.file.FileUtil;
import app.xml.Xml;
import app.xml.XmlAlbum;
import app.xml.XmlAlbumItem;
import app.zdlg.ChangeDateDlg;
import app.zdlg.CommentParamDlg;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
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

/**
 * class for managing Album
 *
 * @author favdb
 */
public class Album extends JFrame {

	private static final String TT = "Album.";

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
	// tree, gallery and table
	public AlbumTree tree;
	public AlbumGallery gallery;
	private AlbumTable table;
	private JPanel pTree, pGallery, pTable;
	private JComboBox<VIEW_MODE> cbViewMode;
	private VIEW_MODE currentViewMode = VIEW_MODE.MONTH;
	// other components
	private String albumName = "Album";
	private Xml xml;
	private JTextField title;
	private IconButton btAdd;
	public JButton btDiapo;
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
		setLayout(new GridBagLayout());
		currentViewMode = VIEW_MODE.values()[App.pref.albumViewLastGet()];
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
		JPanel ptree = treeInit();
		JPanel pgallery = galleryInit();
		JPanel ptable = tableInit();
		JSplitPane spRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pgallery, ptable);
		spRight.setResizeWeight(1.0);
		JSplitPane spAll = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, ptree, spRight);
		spAll.setResizeWeight(0.10);
		spAll.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
		add(spAll, new GBC("0, 0, grow, wx 1.0, wy 1.0"));
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
	private JPanel treeInit() {
		pTree = new JPanel(new GridBagLayout());
		pTree.add(Ui.initButton("btChangePhotoDir",
				"photo.dir",
				ICONS.K.COGS,
				"",
				e -> App.photosDirSelect()), new GBC("0, 0, growx, wx 1.0"));

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
		pTree.add(cbViewMode, new GBC("1, 0, growx, wx 1.0"));

		tree = new AlbumTree(this);
		tree.addTreeSelectionListener(e -> treeChanged());
		JScrollPane scroll = new JScrollPane(tree);
		int minWidth = Ui.getTextWidth(" 9999/99/99 ", tree.getFont());
		scroll.setMinimumSize(new Dimension(minWidth, 100));
		pTree.add(scroll, new GBC("2, 0, grow, wx 1.0, wy 1.0"));
		return pTree;
	}

	/**
	 * initialize the gallery panel
	 *
	 * @return
	 */
	private JPanel galleryInit() {
		pGallery = new JPanel(new GridBagLayout());
		pGallery.setPreferredSize(new Dimension(800, 800));

		JToolBar tb = new JToolBar();
		tb.setFloatable(false);
		btAdd = new IconButton("", ICONS.K.PLUS, e -> btAddPhotos());
		btAdd.setEnabled(false);

		// Alignement à droite dans la JToolBar via un panneau flexible intermédiaire
		JPanel tbPanel = new JPanel(new BorderLayout());
		tbPanel.setOpaque(false);
		tbPanel.add(btAdd, BorderLayout.EAST);
		tb.add(tbPanel);

		pGallery.add(tb, new GBC("0, 0, growx, wx 1.0"));

		gallery = new AlbumGallery(this, null);
		pGallery.add(gallery, new GBC("1, 0, grow, wx 1.0, wy 1.0"));
		return pGallery;
	}

	/**
	 * initialize the view
	 *
	 * @return
	 */
	private JPanel tableInit() {
		pTable = new JPanel(new GridBagLayout());

		// 1. Boutons en haut (Changer album & Créer album) - Ligne 0
		pTable.add(Ui.initButton("albumBtChange",
				"album.change",
				ICONS.K.COGS,
				"",
				e -> App.albumFileOpen()),
				new GBC("0, 0, growx, wx 1.0"));
		pTable.add(Ui.initButton("albumBtCreate",
				"",
				ICONS.K.F_NEW,
				"album.new_tips",
				e -> App.albumFileNew()),
				new GBC("0, 1, growx"));

		// 2. Titre de l'album - Ligne 1
		JPanel ptitle = new JPanel(new GridBagLayout());
		ptitle.add(new JLabel(I18N.getColonMsg("album.title")),
				new GBC("0, 0, left"));
		title = new JTextField();
		XmlAlbum xmlAlbum = xml.albumGet();
		title.setText(xmlAlbum != null ? xmlAlbum.titleGet() : "");
		title.setColumns(32);
		title.addCaretListener(e -> titleChange());
		ptitle.add(title, new GBC("0, 1, growx, wx 1.0, ins 0 5 0 0"));
		pTable.add(ptitle, new GBC("1, 0, growx, gw 2, wx 1.0"));

		// 3. Tableau avec scroller - Ligne 2
		table = new AlbumTable(this);
		int minWidth = Ui.getTextWidth("WW | Photo | Commentaire ", table.getFont());
		table.setMinimumSize(new Dimension(minWidth, 100));
		JScrollPane scroll = new JScrollPane(table);
		scroll.setMinimumSize(new Dimension(minWidth, 100));
		pTable.add(scroll, new GBC("2, 0, grow, gw 2, wx 1.0, wy 1.0"));

		// 4. Bouton Diaporama juste sous le tableau - Ligne 3
		btDiapo = Ui.initButton("app.diapo",
				ICONS.K.PIC,
				e -> mainFrame.doDiaporama());
		btDiapo.setEnabled(false);
		pTable.add(btDiapo, new GBC("3, 0, growx, gw 2, wx 1.0"));

		return pTable;
	}

	/**
	 * get the Album table
	 *
	 * @return
	 */
	public AlbumTable tableGet() {
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
		File nodeFile = (File) node.getUserObject();
		if (nodeFile.isDirectory()) {
			if (selectionAllowedForMode(nodeFile)) {
				gallery.rootdirSet(nodeFile);
				String path = "";
				try {
					path = nodeFile.getPath()
							.replace(App.pref.photosDirGet(), "")
							.substring(1);
				} catch (Exception ex) {
				}
				App.pref.setString(Pref.KEY.ALBUM_LASTNODE, path);
			} else {
				gallery.rootdirSet(null);
			}
		} else {
			gallery.rootdirSet(null);
		}
	}

	/**
	 * check if the selected folder correspond to the current VIEW_MODE
	 */
	private boolean selectionAllowedForMode(File file) {
		if (!isNormedDir(file)) {
			return true;
		}
		int depth = getNormedDepth(file);
		int targetDepth = currentViewMode.getLevel() + 1;
		return depth >= targetDepth;
	}

	/**
	 * get the normed depth
	 */
	private int getNormedDepth(File file) {
		int depth = 0;
		File current = file;
		File root = new File(App.pref.photosDirGet());
		while (current != null
				&& !current.equals(root)
				&& current.getName().matches("\\d+")) {
			depth++;
			current = current.getParentFile();
		}
		return depth;
	}

	/**
	 * check if folder is normed (numerical)
	 */
	private boolean isNormedDir(File dir) {
		return dir.getName().matches("\\d+");
	}

	/**
	 * load the Album Table from current file
	 */
	public void tableLoad() {
		table.load(xml);
		paramLoad();
	}

	/**
	 * load the Album Table from the given file
	 *
	 * @param file
	 */
	public void tableLoad(File file) {
		fileSet(file);
		xml = new Xml(file);
		tableLoad();
	}

	/**
	 * get the row as XmlAlbumItem for the given index
	 *
	 * @param i
	 * @return
	 */
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
	public void prefSave() {
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
	public void paramLoad() {
		param = new DiapoParam(table.xmlGet());
	}

	/**
	 * create Album parameters
	 */
	public void paramDiapoCreate() {
		if (table != null && table.xml.isOpened()) {
			paramLoad();
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
		paramLoad();
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
	public void photosDirSet(File file) {
		fileSet(new File(App.pref.photosDirGet() + File.separator + "Album.xml"));
	}

	/**
	 * refresh all component (tree and gallery)
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
			if (il.selGet() == AlbumGalleryCell.SEL) {
				table.rowAdd(new XmlAlbumItem("" + (table.getRowCount() + 1),
						f.getAbsolutePath(), param.getComment(f)));
				il.selSet(AlbumGalleryCell.SEL_ALBUM);
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
			param.setComment(xml.albumGet().getPrefComment());
		} else {
			return;
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
			} else if (Media.jpegIs(fl)) {
				addFile(fl, treeItems);
			}
		}
		if (!treeItems.isEmpty()) {
			Collections.sort(treeItems, (XmlAlbumItem f1, XmlAlbumItem f2)
					-> f1.fileGet().getAbsolutePath()
							.compareTo(f2.fileGet().getAbsolutePath()));
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
			if (Media.jpegIs(f)) {
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
		if (Media.jpegIs(file)) {
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
	public void popupShow(MouseEvent e, DefaultMutableTreeNode node) {
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
	public void btAddUpdate(boolean b) {
		btAdd.setEnabled(b);
	}

	/**
	 * change the date-time of the given file image
	 *
	 * @param file
	 */
	public void dateChange(File file) {
		if (file != null && (Media.jpegIs(file) || Media.mp4Is(file))) {
			ChangeDateDlg dlg = new ChangeDateDlg(this, file);
			dlg.setVisible(true);
			if (!dlg.isCancel()) {
				String origin = FileUtil.removeExtension(file.getName());
				String date = dlg.getDate();
				if (!date.equals(origin)) try {
					String subdir = getSubdir(date, 2);
					File out = new File(App.pref.photosDirGet()
							+ File.separator + subdir
							+ File.separator + date + ".jpg");
					out.getParentFile().mkdirs();
					Files.move(file.toPath(), out.toPath(), REPLACE_EXISTING);
					FileUtil.dirRemove(file.getParentFile());
					tree.reload(currentViewMode);
					String node = App.pref.getString(Pref.KEY.ALBUM_LASTNODE);
					if (!node.isEmpty()) {
						File sel = new File(App.pref.photosDirGet(), node);
						tree.select(sel);
					}
					gallery.refresh();
				} catch (IOException ex) {
					LOG.err(TT + "changeDate() move error", ex);
				}
			}
		}
	}

	/**
	 * change the date-time of the given list image
	 *
	 * @param file
	 */
	public void dateChange(List<AlbumGalleryCell> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		ChangeDateDlg dlg = new ChangeDateDlg(this, list.get(0).fileGet());
		dlg.setVisible(true);
		if (!dlg.isCancel()) {
			try {
				SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd_HHmmss");
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
				Date startDate = fmt.parse(dlg.getDate());
				LocalDateTime currentDateTime = startDate.toInstant()
						.atZone(ZoneId.systemDefault()).toLocalDateTime();
				for (AlbumGalleryCell il : list) {
					File inf = il.fileGet();
					if (inf == null || !inf.exists()) {
						continue;
					}
					String formattedDate = currentDateTime.format(dtf);
					String subdir = getSubdir(formattedDate, 2);
					String extension = FileUtil.getExtension(inf);
					if (!extension.isEmpty() && !extension.startsWith(".")) {
						extension = "." + extension;
					}
					File out = new File(App.pref.photosDirGet()
							+ File.separator + subdir
							+ File.separator + formattedDate + extension);
					out.getParentFile().mkdirs();
					Files.move(inf.toPath(), out.toPath(), REPLACE_EXISTING);
					FileUtil.dirRemove(inf.getParentFile());
					currentDateTime = currentDateTime.plusSeconds(1);
				}
				tree.reload(currentViewMode);
				String node = App.pref.getString(Pref.KEY.ALBUM_LASTNODE);
				if (!node.isEmpty()) {
					File sel = new File(App.pref.photosDirGet(), node);
					tree.select(sel);
				}
				gallery.refresh();
			} catch (ParseException | IOException ex) {
				LOG.err(TT + "changeDate(list) error", ex);
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

	public AlbumGallery galleryGet() {
		return gallery;
	}

	/**
	 * add the given photo to the current album
	 *
	 * @param il
	 */
	public void photoAdd(AlbumGalleryCell il) {
		if (CommentParamDlg.showing(this, true)) {
			param.setComment(xml.albumGet().getPrefComment());
		} else {
			return;
		}
		File f = il.fileGet();
		XmlAlbumItem item = new XmlAlbumItem("" + (table.getRowCount() + 1),
				f.getAbsolutePath(), param.getComment(f));
		table.rowAdd(item);
		table.setModified();
		save();
		gallery.refresh();
	}

	/**
	 * add the given list of photo to the current album
	 *
	 * @param il
	 */
	public void photoAdd(List<AlbumGalleryCell> list) {
		for (AlbumGalleryCell il : list) {
			File f = il.fileGet();
			XmlAlbumItem item = new XmlAlbumItem("" + (table.getRowCount() + 1),
					f.getAbsolutePath(), param.getComment(f));
			table.rowAdd(item);
		}
		table.setModified();
		save();
		gallery.refresh();
	}

	/**
	 * remove given photo from the current album table
	 *
	 * @param il
	 */
	public void photoRemove(AlbumGalleryCell il) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		String targetPath = il.fileGet().getAbsolutePath();
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

	/**
	 * remove given list of photo from the current album table
	 *
	 * @param il
	 */
	public void photoRemove(List<AlbumGalleryCell> list) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		for (AlbumGalleryCell il : list) {
			String targetPath = il.fileGet().getAbsolutePath();
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
					break;
				}
			}
		}
		table.setModified();
		save();
		gallery.refresh();
	}

	/**
	 * delete the given photo
	 *
	 * @param il
	 */
	public void photoDelete(AlbumGalleryCell il) {
		File parent = il.fileGet().getParentFile();
		il.fileGet().delete();
		FileUtil.dirRemove(parent);
		refreshAll();
		String node = App.pref.getString(Pref.KEY.ALBUM_LASTNODE);
		if (!node.isEmpty()) {
			File sel = new File(App.pref.photosDirGet(), node);
			tree.select(sel);
			gallery.refresh();
		}
	}

	/**
	 * delete the given list of photo
	 *
	 * @param il
	 */
	public void photoDelete(List<AlbumGalleryCell> list) {
		LOG.trace(TT + "photoDelete(list nb=" + list.size() + ")");
		for (AlbumGalleryCell il : list) {
			File parent = il.fileGet().getParentFile();
			il.fileGet().delete();
			FileUtil.dirRemove(parent);
		}
		refreshAll();
		String node = App.pref.getString(Pref.KEY.ALBUM_LASTNODE);
		if (!node.isEmpty()) {
			File sel = new File(App.pref.photosDirGet(), node);
			tree.select(sel);
			gallery.refresh();
		}
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
	 * get the current VIEW_MODE
	 *
	 * @return
	 */
	public VIEW_MODE currentViewModeGet() {
		return currentViewMode;
	}

	/**
	 * set the current VIEW_MODE
	 *
	 * @param mode
	 */
	public void currentViewModeSet(VIEW_MODE mode) {
		this.currentViewMode = mode;
		if (cbViewMode != null) {
			cbViewMode.setSelectedItem(mode);
		}
	}

}
