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
package app.print;

import i18n.I18N;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import tools.LOG;

public class Pool extends JScrollPane {

	private static final String TT = "Pool.";

	private final Print print;
	public JTree tree;
	public static int ROW_SZ = 128;
	private DefaultMutableTreeNode rootNode;
	private DefaultMutableTreeNode photosBranch;
	private DefaultMutableTreeNode textsBranch;
	private PoolCell poolCellSelected;

	private Object pendingClickedObject;

	private final Timer timer = new Timer(250, e -> handleSimpleClick());

	{
		timer.setRepeats(false);
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Pool(Print print) {
		this.print = print;
		initialize();
	}

	public Print printGet() {
		return print;
	}

	public void initialize() {
		rootNode = new DefaultMutableTreeNode("Pool");
		photosBranch = new DefaultMutableTreeNode(I18N.getMsg("print.pool.photos"));
		textsBranch = new DefaultMutableTreeNode(I18N.getMsg("print.pool.texts"));
		rootNode.add(photosBranch);
		rootNode.add(textsBranch);
		tree = new JTree(rootNode);
		tree.setRootVisible(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		tree.setCellRenderer(new PoolRenderer(this));
		tree.addMouseListener(new PoolMouseListener());
		this.setViewportView(tree);
	}

	public void refresh() {
		photosBranch.removeAllChildren();
		textsBranch.removeAllChildren();
		List<PrintCell> cells = print.getCells();
		PrintCell.sortById(cells);
		for (int i = 0; i < cells.size(); i++) {
			PrintCell p = cells.get(i);
			if (p.isPhoto()) {
				photosBranch.add(new PoolCell(p));
			} else {
				textsBranch.add(new PoolCell(p));
			}
		}
		((DefaultTreeModel) tree.getModel()).reload();
		tree.expandPath(new TreePath(photosBranch.getPath()));
		tree.expandPath(new TreePath(textsBranch.getPath()));
	}

	/**
	 * Return data object associated with selected node (Photo or Text)
	 *
	 * @return
	 */
	public Object getSelectedResource() {
		LOG.trace(TT + "getSelectedResource()");
		TreePath path = tree.getSelectionPath();
		if (path == null) {
			return null;
		}
		return path.getLastPathComponent();
	}

	/**
	 * Contextual Menu
	 */
	private void showContextMenu(MouseEvent e, Object userObject) {
		LOG.trace(TT + "showContextMenu(...");
		if (userObject == null || userObject instanceof String) {
			return;
		}
		if (userObject instanceof PoolCell) {
			JPopupMenu menu = new JPopupMenu();
			PrintCell cell = ((PoolCell) userObject).printCellGet();
			if (cell.isPhoto()) {
				JMenuItem openItem = new JMenuItem(I18N.getMsg("print.pool.open_photo"));
				openItem.addActionListener(al -> openPreviewAction(cell));
				menu.add(openItem);
			} else {
				JMenuItem editItem = new JMenuItem(I18N.getMsg("print.text_edit"));
				editItem.addActionListener(al -> print.shefEdit(cell));
				menu.add(editItem);
			}
			JMenuItem createtext = new JMenuItem(I18N.getMsg("print.text_create"));
			createtext.addActionListener(l -> {
				PrintCell ncell = new PrintCell();
				print.textCreate(ncell);
			});
			menu.add(createtext);
			menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/**
	 * show Photo as a dialog
	 */
	private void openPreviewAction(PrintCell photo) {
		try {
			String fullPath = app.App.preferences.photosDirGet()
					+ File.separator + photo.photoFileGet();
			File imageFile = new File(fullPath);
			if (!imageFile.exists()) {
				imageFile = new File(photo.photoFileGet());
			}
			if (imageFile.exists()) {
				ImageIcon originalIcon = new ImageIcon(imageFile.getAbsolutePath());
				Image img = originalIcon.getImage();
				int targetWidth = 640;
				int imgWidth = originalIcon.getIconWidth();
				int imgHeight = originalIcon.getIconHeight();
				if (imgWidth > targetWidth) {
					double ratio = (double) imgHeight / (double) imgWidth;
					int targetHeight = (int) (targetWidth * ratio);
					img = img.getScaledInstance(targetWidth, targetHeight, java.awt.Image.SCALE_SMOOTH);
				}
				ImageIcon scaledIcon = new ImageIcon(img);
				JLabel labelImage = new JLabel(scaledIcon);
				labelImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
				JScrollPane scrollPane = new JScrollPane(labelImage);
				scrollPane.setBorder(BorderFactory.createEmptyBorder());
				JDialog previewDialog = new JDialog((java.awt.Frame) null, photo.photoFileGet(), true);
				previewDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				previewDialog.setLayout(new BorderLayout());
				previewDialog.add(scrollPane, BorderLayout.CENTER);
				previewDialog.pack();
				previewDialog.setLocationRelativeTo(null);
				previewDialog.setVisible(true);
			} else {
				JOptionPane.showMessageDialog(this,
						fullPath + " " + I18N.getMsg("print.error.notfound"),
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception ex) {
			tools.LOG.err("PrintPool.openPreviewAction error", ex);
		}
	}

	public void updatePoolNode(PrintCell cell) {
		LOG.trace(TT + "updatePoolNode(cell=" + cell.toString() + ")");
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		for (int i = 0; i < root.getChildCount(); i++) {
			DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) root.getChildAt(i);
			for (int j = 0; j < groupNode.getChildCount(); j++) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) groupNode.getChildAt(j);
				if (node.getUserObject() instanceof PoolCell) {
					PoolCell pc = (PoolCell) node.getUserObject();
					if (pc.printCellGet() == cell) {
						model.nodeChanged(node);
						tree.revalidate();
						tree.repaint();
						return;
					}
				}
			}
		}
	}

	public PoolCell poolCellSelectedGet() {
		return poolCellSelected;
	}

	public void poolCellSelect(PoolCell cell) {
		poolCellSelected = cell;
	}

	public void poolCellUnselect() {
		poolCellSelected = null;
		if (this.tree != null) {
			this.tree.clearSelection();
		}
	}

	private void handleSimpleClick() {
		//LOG.trace(TT + "handleSimpleClick()");
		if (!(pendingClickedObject instanceof PoolCell)) {
			return;
		}

		PoolCell cellClicked = (PoolCell) pendingClickedObject;
		PrintCell cell = cellClicked.printCellGet();

		if (cell.pageGet() > 0) {
			return;
		}

		// Si l'élément cliqué est déjà sélectionné -> Désélection
		if (poolCellSelected == cellClicked) {
			poolCellUnselect();
			print.pendingCellClear();
		} else {
			// Sinon -> Sélection de l'élément
			poolCellSelect(cellClicked);
			print.pendingCellToPlaceSet(cell);
		}
		tree.repaint();
	}

	private void handleDoubleClick(Object userObject) {
		//LOG.trace(TT + "handleDoubleClick()");
		// Forcer la désélection de tout élément
		poolCellUnselect();
		print.pendingCellClear();
		tree.repaint();

		if (userObject instanceof PoolCell) {
			PrintCell cell = ((PoolCell) userObject).printCellGet();
			if (cell.isPhoto()) {
				print.getMainFrame().showPhoto(cell.photoFileGet());
			} else if (cell.isText()) {
				print.shefEdit(cell);
			}
		}
	}

	public void refreshCell(PrintCell item) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	private class PoolMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			TreePath path = tree.getPathForLocation(e.getX(), e.getY());
			if (path == null) {
				poolCellUnselect();
				print.pendingCellClear();
				tree.repaint();
				return;
			}

			Object userObject = path.getLastPathComponent();

			if (SwingUtilities.isLeftMouseButton(e)) {
				switch (e.getClickCount()) {
					case 1:
						pendingClickedObject = userObject;
						timer.restart();
						break;
					case 2:
						timer.stop();
						handleDoubleClick(userObject);
						break;
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			showPopupMenu(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			showPopupMenu(e);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		private void showPopupMenu(MouseEvent e) {
			if (e.isPopupTrigger()) {
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (path != null) {
					Object userObject = path.getLastPathComponent();
					if (userObject instanceof PoolCell) {
						PrintCell cell = ((PoolCell) userObject).printCellGet();
						if (cell.pageGet() > 0) {
							return;
						}
					}
					tree.setSelectionPath(path);
					showContextMenu(e, getSelectedResource());
				}
			}
		}
	}

}
