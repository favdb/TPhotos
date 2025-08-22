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

import api.mig.swing.MigLayout;
import app.App;
import static app.album.AlbumTree.getSubdir;
import app.dialog.ChangeDateDlg;
import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import resources.icons.IconUtil;
import tools.DateUtil;
import tools.LOG;
import tools.MIG;
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
	private AlbumTable table;
	// other components
	private JPanel pTable, pImage, pTree;
	private JLabel image;
	private String albumName = "Album";
	private Xml xml;
	private JTextField title;
	private boolean imageAllowed;
	private Color originBK, originFG;
	private JSplitPane spleft, sp;
	private File curImg;
	private String curTxt;
	private Dimension curSz;

	public Album() {
		super();
		initialize();
	}

	public void albumNameSet(String value) {
		this.albumName = value;
	}

	public String albumNameGet() {
		return table.getXml().getFile().getName();
	}

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
		JPanel pimage = imageInit();
		JPanel ptable = initTable();
		setLayout(new MigLayout(MIG.FILL));
		spleft = new JSplitPane(JSplitPane.VERTICAL_SPLIT, ptree, pimage);
		spleft.setResizeWeight(0.85);
		sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, spleft, ptable);
		sp.setResizeWeight(0.5);
		add(sp, MIG.GROW);
		table.getSelectionModel().addListSelectionListener((ListSelectionEvent event) -> {
			imageAllowed = false;
			if (table.getRowCount() == 0) {
				imageReset();
				return;
			}
			int[] rows = table.getSelectedRows();
			if (rows == null || rows.length != 1) {
				imageReset();
				return;
			}
			imageSet((File) table.getValueAt(rows[0], 1), null);
		});
		spleft.addPropertyChangeListener(e -> splitChanged());
	}

	private JPanel initTree() {
		//LOG.trace(TT + "initTree()");
		pTree = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.INS0)));
		tree = new AlbumTree(this);
		tree.addTreeSelectionListener(e -> treeChanged());
		JScrollPane scroll = new JScrollPane(tree);
		scroll.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		pTree.add(scroll, MIG.GROW);
		return pTree;
	}

	private JPanel imageInit() {
		//LOG.trace(TT + "initImage()");
		pImage = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.INS0), "[grow][]"));
		pImage.add(image = new JLabel(I18N.getMsg("photo.preview")), MIG.get(MIG.SPAN, MIG.GROW));
		image.setMinimumSize(new Dimension(IMG_SIZE + 5, IMG_SIZE + 5));
		image.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		image.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		image.setBorder(BorderFactory.createLineBorder(originBK));
		image.addMouseListener(new ImageListener(this));
		return pImage;
	}

	private JPanel initTable() {
		//LOG.trace(TT + "initTable()");
		pTable = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.INS0, MIG.GAP + " 5"), "[grow]", "[][grow]"));
		JPanel ptitle = new JPanel(new MigLayout(MIG.FLOWX));
		ptitle.add(new JLabel(I18N.getColonMsg("album.title")), MIG.SPLIT2);
		ptitle.add(title = new JTextField(), MIG.get(MIG.SPAN, MIG.GROWX));
		title.setText(xml.getTitle());
		title.setColumns(32);
		title.addCaretListener(e -> titleChange());
		pTable.add(ptitle, MIG.get(MIG.SPAN, MIG.GROWX));
		table = new AlbumTable(this);
		table.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		JScrollPane scroll = new JScrollPane(table);
		scroll.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		pTable.add(scroll, MIG.get(MIG.SPAN, MIG.GROW));
		pTable.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		return pTable;
	}

	public AlbumTable getTable() {
		return table;
	}

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
		imageSet(img, txt);
	}

	public void loadTable() {
		table.load(xml);
		loadParam();
	}

	public void loadTable(File file) {
		xml = new Xml(file.getAbsolutePath());
		loadTable();
	}

	public void save() {
		table.save(title.getText());
	}

	public void savePref() {
		param.updateXml(xml);
	}

	public Xml getXml() {
		return table.getXml();
	}

	public AlbumParam getAlbumParam() {
		return param;
	}

	public void loadParam() {
		param = new AlbumParam(table.getXml());
	}

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

	public void setPhotosDir(File file) {
		setAlbumFile(new File(App.preferences.photosDirGet() + File.separator + "Album.xml"));
	}

	private void imageSet(File file, String txt) {
		//LOG.trace(TT + "imageSet("
		//		+ "file=" + (file == null ? "null" : file.getName())
		//		+ "txt=" + (txt == null || txt.isEmpty() ? "null" : txt)
		//);
		if (file == null) {
			imageReset();
			image.setText(txt);
		} else {
			Dimension sz = pImage.getSize();
			if (file.equals(curImg) && curSz.equals(sz)) {
				return;
			}
			curSz = sz;
			image.setIcon(IconUtil.getJpegIconFromFile(file, sz));
			image.setToolTipText(DateUtil.toFormatted(FileUtil.removeExtension(file.getName())));
			image.setText(null);
		}
		image.setForeground(imageAllowed ? Color.WHITE : originFG);
		pImage.setBackground(imageAllowed ? Color.BLACK : originBK);
		pImage.setBorder(BorderFactory.createLineBorder((imageAllowed ? Color.GREEN : originBK), 4));
		curImg = file;
		curTxt = txt;
	}

	private void imageReset() {
		//LOG.trace(TT + "imageReset()");
		image.setIcon(null);
		image.setToolTipText(null);
		image.setText(null);
		pImage.setBackground(Color.GRAY);
	}

	public void refreshAll() {
		tree.reload();
		setAlbumFile(new File(App.preferences.photosDirGet() + File.separator + "Album.xml"));
	}

	public void btAddAction() {
		//LOG.trace(TT + "btAdd");
		if (!AlbumParamDlg.showing(this)) {
			return;
		}
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (node == null) {
			return;
		}
		List<AlbumItem> treeItems = new ArrayList<>();
		TreePath[] paths = tree.getSelectionPaths();
		if (node.isLeaf() && paths.length == 1) {
			File imgFile = (File) node.getUserObject();
			if (App.jpegIs(imgFile)) {
				treeItems.add(new AlbumItem(1, param.getComment(imgFile), imgFile));
			} else {
				LOG.err(TT + "btAddAction " + imgFile.getAbsolutePath() + " is not JPEG");
			}
		} else {
			for (TreePath path : tree.getSelectionPaths()) {
				String nf = path.getLastPathComponent().toString();
				File fl = new File(nf);
				if (fl.isDirectory()) {
					addDir(fl, treeItems);
				} else if (App.jpegIs(fl)) {
					addFile(fl, treeItems);
				}
			}
		}
		Collections.sort(treeItems, (AlbumItem f1, AlbumItem f2)
				-> f1.file.getAbsolutePath().compareTo(f2.file.getAbsolutePath()));
		if (!treeItems.isEmpty()) {
			for (AlbumItem item : treeItems) {
				table.addRow(item);
			}
		}
	}

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

	private void addFile(File file, List<AlbumItem> treeItems) {
		//LOG.trace(TT + "addFile(file=" + file.getName() + ")");
		if (App.jpegIs(file)) {
			treeItems.add(new AlbumItem(treeItems.size() + 1, param.getComment(file), file));
		}
	}

	private void splitChanged() {
		//LOG.trace(TT + "splitChanged() hasChanged=" + hasChanged);
		imageSet(curImg, curTxt);
	}

	/**
	 * replace the guven image file by the new given file
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

	public void showPopup(MouseEvent e) {
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem item1 = new JMenuItem(I18N.getMsg("album.add"));
		item1.addActionListener(act -> btAddAction());
		popupMenu.add(item1);
		JMenuItem item2 = new JMenuItem(I18N.getMsg("date.change"));
		popupMenu.add(item2);
		item2.addActionListener(act -> changeDate());
		popupMenu.show(e.getComponent(), e.getX(), e.getY());
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
						replace(imgFile, out);
					} catch (IOException ex) {
						LOG.err(TT + "changeDate() move error", ex);
					}
				}
			}
		}
	}

	private void titleChange() {
		if (!title.getText().equals(xml.getTitle())) {
			table.setModified();
		}
	}

	public String albumTitleGet() {
		return xml.getTitle();
	}

	private static class ImageListener implements MouseListener {

		private final Album album;

		public ImageListener(Album album) {
			this.album = album;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {
				//LOG.trace("MouseAction.mouseClicked(e=" + e.toString() + ") triggered");
				TreePath[] paths = album.tree.getSelectionPaths();
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) album.tree.getLastSelectedPathComponent();
				if (node != null && node.isLeaf()) {
					album.showPopup(e);
				}
			}
			if (e.getClickCount() > 1) {
				album.btAddAction();
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			//empty
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			//empty
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			//empty
		}

		@Override
		public void mouseExited(MouseEvent e) {
			//empty
		}
	}

}
