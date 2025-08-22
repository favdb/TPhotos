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
import i18n.I18N;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import resources.icons.ICONS;
import resources.icons.IconUtil;

/**
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
	}

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

	public void reload() {
		rootNode.removeAllChildren();
		DefaultTreeModel model = (DefaultTreeModel) getModel();
		model.reload();
		rootDir = new File(App.preferences.photosDirGet());
		rootNode.setUserObject(rootDir);
		createChildren(rootDir, rootNode);
		expandPath(new TreePath(model.getRoot()));
	}

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

	public void createChildren(File fileRoot, DefaultMutableTreeNode node) {
		//LOG.trace(TT + "createChildren(root=" + fileRoot.getName() + ", node=" + node.getPath() + ")");
		File[] files = fileRoot.listFiles();
		if (files == null) {
			return;
		}
		Arrays.sort(files);
		for (File file : files) {
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(file);
			if (App.jpegIs(file) || file.isDirectory()) {
				node.add(childNode);
			}
			if (file.isDirectory()) {
				createChildren(file, childNode);
			}
		}
	}

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

	private static class KeyActions implements KeyListener {

		private final JTree tree;

		public KeyActions(JTree tree) {
			this.tree = tree;
		}

		@Override
		public void keyTyped(KeyEvent e) {
			//LOG.trace("KeyActions.keyTyped(e=" + e.toString() + ")");
			int key = e.getKeyCode();
			char keychar = e.getKeyChar();
			if (keychar == 0x007F) {
				TreePath[] paths = tree.getSelectionPaths();
				if (paths.length == 0) {
					return;
				}
				List<File> ls = new ArrayList<>();
				StringBuilder b = new StringBuilder();
				for (int i = 0; i < paths.length; i++) {
					if (i > 0) {
						b.append("\n");
					}
					File file = new File(paths[i].getLastPathComponent().toString());
					b.append(file.getAbsolutePath());
					ls.add(file);
				}
				if (App.preferences.getBoolean(Pref.KEY.ASK_DELETE)) {
					Object[] options = {I18N.getMsg("ask.yes"), I18N.getMsg("ask.no")};
					if (JOptionPane.showOptionDialog(tree,
							I18N.getMsg("ask.delete", b.toString()), I18N.getMsg("ask.confirm"),
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
							null, options, options[1]) != JOptionPane.YES_OPTION) {
						return;
					}
				}
				for (File f : ls) {
					f.delete();
				}
				DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
				for (TreePath path : paths) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
					if (node.getParent() != null) {
						model.removeNodeFromParent(node);
					}
				}
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

}
