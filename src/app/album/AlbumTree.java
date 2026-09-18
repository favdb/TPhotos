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

import app.App;
import app.Pref;
import app.i18n.I18N;
import app.resources.icons.ICONS;
import app.resources.icons.IconUtil;
import app.tools.file.FileUtil;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * JTree for the files
 *
 * @author favdb
 */
public class AlbumTree extends JTree {

	private static final String TT = "AlbumTree.";
	public final Album album;
	private File rootDir;
	private DefaultMutableTreeNode rootNode;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public AlbumTree(Album album) {
		super();
		this.album = album;
		initialize();
	}

	/**
	 * initialization
	 */
	public void initialize() {
		rootDir = new File(App.pref.photosDirGet());
		rootNode = new DefaultMutableTreeNode(rootDir);
		DefaultTreeModel model = (DefaultTreeModel) getModel();
		model.setRoot(rootNode);
		reload(album.currentViewModeGet());
		setCellRenderer(new CellRenderer());
		addKeyListener(new KeyActions(this));
		MouseActions mouseActions = new MouseActions(this);
		addMouseListener(mouseActions);
		addMouseMotionListener(mouseActions);
		ToolTipManager.sharedInstance().registerComponent(this);
	}

	/**
	 * get the sub-directory name for the given date String, the mode is not used
	 * (allways=2)
	 *
	 * @param date
	 * @param mode 0 year only, 1=with year and month, 2=with year, month and day
	 * @return
	 */
	public static String getSubdir(String date, int mode) {
		StringBuilder subdir = new StringBuilder();
		if (mode < 3) {
			subdir.append(date.substring(0, 4)).append(File.separator);
			if (mode > 0) {
				subdir.append(date.substring(4, 6)).append(File.separator);
			}
			if (mode > 1) {
				subdir.append(date.substring(6, 8)).append(File.separator);
			}
		}
		return subdir.toString();
	}

	/**
	 * select the given file
	 *
	 * @param s
	 */
	public void select(File s) {
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> e = rootNode.depthFirstEnumeration();
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node = e.nextElement();
			if (((File) node.getUserObject()).equals(s)) {
				TreePath path = new TreePath(node.getPath());
				setSelectionPath(path);
				scrollPathToVisible(path);
			}
		}
	}

	/**
	 * delete selected nodes
	 */
	public void deleteSelection() {
		TreePath[] paths = getSelectionPaths();
		if (paths == null || paths.length == 0) {
			return;
		}

		List<File> filesToDelete = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < paths.length; i++) {
			if (i > 0) {
				sb.append("\n");
			}
			File file = (File) ((DefaultMutableTreeNode) paths[i]
					.getLastPathComponent()).getUserObject();
			if (file.getName().equals("Photos")) {
				JOptionPane.showMessageDialog(album,
						I18N.getMsg("action.delete_unable", file.getAbsolutePath()),
						I18N.getMsg("action.delete"), JOptionPane.ERROR_MESSAGE);
				return;
			}
			sb.append(file.getAbsolutePath());
			filesToDelete.add(file);
		}
		if (App.pref.getBoolean(Pref.KEY.ASK_DELETE)) {
			Object[] options = {I18N.getMsg("ask.yes"), I18N.getMsg("ask.no")};
			int choice = JOptionPane.showOptionDialog(this,
					I18N.getMsg("ask.delete", sb.toString()), I18N.getMsg("ask.confirm"),
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
					null, options, options[1]);
			if (choice != JOptionPane.YES_OPTION) {
				return;
			}
		}
		int deleted = 0;
		for (int i = 0; i < paths.length; i++) {
			File f = filesToDelete.get(i);
			if (f.isDirectory()) {
				if (FileUtil.dirDelete(f)) {
					deleted++;
				}
			} else if (f.delete()) {
				deleted++;
			}
		}
		if (deleted > 0) {
			album.galleryGet().refresh();
		}
	}

	/**
	 * reload the tree according to VIEW_MODE
	 */
	public void reload(Album.VIEW_MODE mode) {
		rootNode.removeAllChildren();
		DefaultTreeModel model = (DefaultTreeModel) getModel();
		rootDir = new File(App.pref.photosDirGet());
		rootNode.setUserObject(rootDir);
		if (mode != Album.VIEW_MODE.NONE) {
			buildVirtualTree(rootDir, rootNode, mode, 0);
		}
		model.reload();
		expandPath(new TreePath(model.getRoot()));
	}

	/**
	 * Build the treewith all normal directories and sub-directories) and all presonalized
	 * sub-directories.
	 *
	 * @param fileRoot Current directory
	 * @param node the parent node
	 * @param mode The view mod
	 * @param depth Actual depth (0 = Root, 1 = Year, 2 = Month, 3 = Day)
	 */
	private void buildVirtualTree(File fileRoot, DefaultMutableTreeNode node,
			Album.VIEW_MODE mode, int depth) {
		File[] files = fileRoot.listFiles();
		if (files == null) {
			return;
		}
		Arrays.sort(files);
		int maxDepth = mode.getLevel() + 1;
		for (File file : files) {
			if (file.isDirectory()) {
				boolean normed = isNormedDir(file);
				if (normed && depth >= maxDepth) {
					continue;
				}
				DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(file);
				node.add(childNode);
				buildVirtualTree(file, childNode, mode, depth + 1);
			}
		}
	}

	/**
	 * Check if folder name is standard (Year / Month / Day)
	 */
	private boolean isNormedDir(File dir) {
		return dir.getName().matches("\\d+");
	}

	/**
	 * get the path of the given node
	 *
	 * @param node
	 * @return
	 */
	public String getPath(DefaultMutableTreeNode node) {
		TreeNode[] nodes = node.getPath();
		if (nodes.length < 4) {
			return node.toString();
		}
		StringBuilder rc = new StringBuilder();
		for (TreeNode n : nodes) {
			if (n.toString().equals(rootDir.getAbsolutePath())) {
				continue;
			}
			if (rc.length() > 1) {
				rc.append(File.separator);
			}
			rc.append(n.toString());
		}
		return rootDir + File.separator + rc.toString();
	}

	/**
	 * cell renderer
	 */
	private static class CellRenderer extends DefaultTreeCellRenderer {

		public CellRenderer() {
			setClosedIcon(IconUtil.getIconSmall(ICONS.K.FOLDER));
			setOpenIcon(IconUtil.getIconSmall(ICONS.K.FOLDER_OPEN));
			setLeafIcon(IconUtil.getIconSmall(ICONS.K.FOLDER));
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
				boolean expanded, boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			Object userObject = node.getUserObject();
			if (userObject instanceof File) {
				File file = (File) userObject;
				setText(file.getName());
				setIcon(IconUtil.getIconSmall(expanded ? ICONS.K.FOLDER_OPEN : ICONS.K.FOLDER));
			}
			return this;
		}

	}

	/**
	 * keyboard listener
	 */
	private static class KeyActions implements KeyListener {

		private final AlbumTree tree;

		public KeyActions(AlbumTree tree) {
			this.tree = tree;
		}

		@Override
		public void keyTyped(KeyEvent e) {
			char keychar = e.getKeyChar();
			if (keychar == 0x007F) {
				tree.deleteSelection();
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
			// empty
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// empty
		}
	}

	private class MouseActions extends MouseAdapter {

		private final AlbumTree tree;

		public MouseActions(AlbumTree tree) {
			this.tree = tree;
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			TreePath path = tree.getPathForLocation(e.getX(), e.getY());
			if (path != null) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				File file = (File) node.getUserObject();
				if (file.isDirectory()) {
					int nb = FileUtil.getNbElement(file);
					tree.setToolTipText(file.getAbsolutePath() + " (" + nb + ")");
				} else {
					tree.setToolTipText(file.getAbsolutePath());
				}
			} else {
				tree.setToolTipText(null);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showMenu(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showMenu(e);
			}
		}

		private void showMenu(MouseEvent e) {
			TreePath path = tree.getPathForLocation(e.getX(), e.getY());
			if (path != null) {
				File file = (File) ((DefaultMutableTreeNode) path
						.getLastPathComponent()).getUserObject();
				if (file.getName().equals("Photos")) {
					return;
				}
				tree.setSelectionPath(path);
				JPopupMenu popup = new JPopupMenu();
				JMenuItem itemAdd = new JMenuItem(I18N.getMsg("album.add"));
				itemAdd.addActionListener(al -> album.btAddAction());
				popup.add(itemAdd);
				popup.addSeparator();
				JMenuItem itemDelete = new JMenuItem(I18N.getMsg("action.delete"));
				itemDelete.setIcon(IconUtil.getIconSmall(ICONS.K.CANCEL));
				itemDelete.addActionListener(al -> tree.deleteSelection());
				popup.add(itemDelete);
				popup.show(tree, e.getX(), e.getY());
			}
		}
	}

}
