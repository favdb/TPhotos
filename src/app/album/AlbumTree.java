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

import app.album.Album;
import app.App;
import app.Pref;
import i18n.I18N;
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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import tools.file.FileUtil;

/**
 * JTree for the files
 *
 * @author favdb
 */
public class AlbumTree extends JTree {

	private static final String TT = "AlbumTree.";
	public final Album albumPanel;
	private File rootDir;
	private DefaultMutableTreeNode rootNode;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public AlbumTree(Album albumPanel) {
		super();
		this.albumPanel = albumPanel;
		initialize();
	}

	public void initialize() {
		rootDir = new File(App.preferences.photosDirGet());
		rootNode = new DefaultMutableTreeNode(rootDir);
		createChildren(rootDir, rootNode);
		DefaultTreeModel model = (DefaultTreeModel) getModel();
		model.setRoot(rootNode);
		this.expandRow(0);
		setCellRenderer(new CellRenderer());
		addKeyListener(new KeyActions(this));
		MouseActions mouseActions = new MouseActions(this);
		addMouseListener(mouseActions);
		addMouseMotionListener(mouseActions);
		javax.swing.ToolTipManager.sharedInstance().registerComponent(this);
	}

	/**
	 * get the sub folder name
	 *
	 * @param date
	 * @param mode 0 year only, 1=with year and month, 2=with year, month and day
	 * @return
	 */
	public static String getSubdir(String date, int mode) {
		StringBuilder subdir = new StringBuilder();
		if (mode < 3) {
			//allways add year
			subdir.append(date.substring(0, 4)).append(File.separator);
			if (mode > 0) {//add month
				subdir.append(date.substring(4, 6)).append(File.separator);
			}
			if (mode > 1) {//add day
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
			File file = (File) ((DefaultMutableTreeNode) paths[i].getLastPathComponent()).getUserObject();
			sb.append(file.getAbsolutePath());
			filesToDelete.add(file);
		}

		// Confirmation si l'option est activée dans les préférences
		if (App.preferences.getBoolean(Pref.KEY.ASK_DELETE)) {
			Object[] options = {I18N.getMsg("ask.yes"), I18N.getMsg("ask.no")};
			int choice = JOptionPane.showOptionDialog(this,
					I18N.getMsg("ask.delete", sb.toString()), I18N.getMsg("ask.confirm"),
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
					null, options, options[1]);

			if (choice != JOptionPane.YES_OPTION) {
				return;
			}
		}

		// Suppression physique et mise à jour visuelle
		int deleted = 0;
		DefaultTreeModel model = (DefaultTreeModel) getModel();
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
			albumPanel.refreshAll();
		}
	}

	/**
	 * reload the tree
	 */
	public void reload() {
		rootNode.removeAllChildren();
		DefaultTreeModel model = (DefaultTreeModel) getModel();
		model.reload();
		rootDir = new File(App.preferences.photosDirGet());
		rootNode.setUserObject(rootDir);
		createChildren(rootDir, rootNode);
		expandPath(new TreePath(model.getRoot()));
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
	 * create a children node
	 *
	 * @param fileRoot
	 * @param node
	 */
	public void createChildren(File fileRoot, DefaultMutableTreeNode node) {
		//LOG.trace(TT + "createChildren(root=" + fileRoot.getName() + ", node=" + node.getPath() + ")");
		File[] files = fileRoot.listFiles();
		if (files == null) {
			return;
		}
		Arrays.sort(files);
		for (File file : files) {
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(file);
			if (/*App.jpegIs(file) || */file.isDirectory()) {
				node.add(childNode);
			}
			if (file.isDirectory()) {
				createChildren(file, childNode);
			}
		}
	}

	/**
	 * cell renderer
	 */
	private static class CellRenderer extends DefaultTreeCellRenderer {

		public CellRenderer() {
			setClosedIcon(IconUtil.getIconSmall(ICONS.K.FOLDER));
			setOpenIcon(IconUtil.getIconSmall(ICONS.K.FOLDER_OPEN));
			setLeafIcon(IconUtil.getIconSmall(ICONS.K.PIC));
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
				setIcon(file.isDirectory()
						? IconUtil.getIconSmall(ICONS.K.FOLDER)
						: IconUtil.getIconSmall(ICONS.K.PHOTO));
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
			//LOG.trace("KeyActions.keyTyped(e=" + e.toString() + ")");
			int key = e.getKeyCode();
			char keychar = e.getKeyChar();
			if (keychar == 0x007F) {
				tree.deleteSelection();
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
			//empty
		}

		@Override
		public void keyReleased(KeyEvent e) {
			//empty
		}
	}

	private class MouseActions extends MouseAdapter {

		private final AlbumTree tree;

		public MouseActions(AlbumTree tree) {
			this.tree = tree;
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			// --- SURVOL : Informations sur l'élément ---
			TreePath path = tree.getPathForLocation(e.getX(), e.getY());
			if (path != null) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				File file = (File) node.getUserObject();
				// On affiche le chemin absolu en ToolTip
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
				tree.setSelectionPath(path);
				JPopupMenu popup = new JPopupMenu();
				JMenuItem itemAdd = new JMenuItem(I18N.getMsg("album.add"));//add to album
				itemAdd.addActionListener(al -> {
					albumPanel.btAddAction();
				});
				popup.add(itemAdd);
				popup.addSeparator();
				JMenuItem itemDelete = new JMenuItem(I18N.getMsg("action.delete"));
				itemDelete.setIcon(IconUtil.getIconSmall(ICONS.K.CANCEL)); // Si tu as une icône delete
				itemDelete.addActionListener(al -> tree.deleteSelection());
				popup.add(itemDelete);

				popup.show(tree, e.getX(), e.getY());
			}
		}
	}
}
