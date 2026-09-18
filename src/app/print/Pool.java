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

import app.i18n.I18N;
import app.tools.ImageUtil;
import app.xml.XmlPrintCell;
import app.zdlg.ShowPhoto;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * class to manage the Pool
 *
 * @author favdb
 */
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
	private final ClickDispatcher clickDispatcher = new ClickDispatcher(250);

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Pool(Print print) {
		this.print = print;
		initialize();
	}

	/**
	 * get the Print
	 *
	 * @return
	 */
	public Print printGet() {
		return print;
	}

	/**
	 * initialize
	 */
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

	/**
	 * refresh
	 */
	public void refresh() {
		//LOG.trace(TT + "refresh()");
		photosBranch.removeAllChildren();
		textsBranch.removeAllChildren();
		XmlPrintCell.sortById(print.xmlPrintGet().getCells());
		for (XmlPrintCell p : print.getCells()) {
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
	 * get data object associated with selected node (Photo or Text)
	 *
	 * @return
	 */
	public Object getSelectedResource() {
		//LOG.trace(TT + "getSelectedResource()");
		TreePath path = tree.getSelectionPath();
		if (path == null) {
			return null;
		}
		return path.getLastPathComponent();
	}

	/**
	 * Contextual Menu
	 */
	private void showContextMenu(MouseEvent e, Object obj) {
		//LOG.trace(TT + "showContextMenu(e, obj=" + obj.toString() + ")");
		if (obj == null) {
			return;
		}
		JPopupMenu menu = new JPopupMenu();
		if (obj instanceof PoolCell) {
			XmlPrintCell cell = ((PoolCell) obj).printCellGet();
			if (cell.isPhoto()) {
				JMenuItem openItem = new JMenuItem(I18N.getMsg("print.pool.open_photo"));
				openItem.addActionListener(al -> openPreviewAction(cell));
				menu.add(openItem);
			} else if (cell.isText()) {
				JMenuItem editItem = new JMenuItem(I18N.getMsg("print.text_edit"));
				editItem.addActionListener(al -> print.textEdit(cell));
				menu.add(editItem);
				if (cell.pageGet() == 0) {
					JMenuItem supItem = new JMenuItem(I18N.getMsg("print.text_delete"));
					supItem.addActionListener(al -> print.textDelete(cell));
					menu.add(supItem);
				}
			}
		}
		JMenuItem createtext = new JMenuItem(I18N.getMsg("print.text_create"));
		createtext.addActionListener(l -> {
			print.textCreate(0, "");
		});
		menu.add(createtext);
		menu.show(e.getComponent(), e.getX(), e.getY());
	}

	/**
	 * show Photo as a dialog
	 */
	private void openPreviewAction(XmlPrintCell photo) {
		ShowPhoto.show(photo.photoFileGet(), print.getCells());
	}

	/**
	 * update the given node
	 *
	 * @param cell
	 */
	public void updatePoolNode(XmlPrintCell cell) {
		//LOG.trace(TT + "updatePoolNode(cell=" + cell.toString() + ")");
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

	/**
	 * get the selected cell
	 *
	 * @return
	 */
	public PoolCell poolCellSelectedGet() {
		return poolCellSelected;
	}

	/**
	 * select the given cell
	 *
	 * @param cell
	 */
	public void poolCellSelect(PoolCell cell) {
		poolCellSelected = cell;
	}

	/**
	 * unselect current cell
	 */
	public void poolCellUnselect() {
		poolCellSelected = null;
		if (this.tree != null) {
			this.tree.clearSelection();
		}
	}

	/**
	 * handle for simpleclick
	 */
	private void handleSimpleClick() {
		//LOG.trace(TT + "handleSimpleClick()");
		if (!(pendingClickedObject instanceof PoolCell)) {
			return;
		}
		PoolCell cellClicked = (PoolCell) pendingClickedObject;
		XmlPrintCell cell = cellClicked.printCellGet();
		if (cell.pageGet() > 0) {
			return;
		}
		if (poolCellSelected == cellClicked) {
			poolCellUnselect();
			print.pendingCellClear();
		} else {
			poolCellSelect(cellClicked);
			print.pendingCellToPlaceSet(cell);
		}
		tree.repaint();
	}

	/**
	 * handle for double click
	 *
	 * @param userObject
	 */
	private void handleDoubleClick(Object userObject) {
		//LOG.trace(TT + "handleDoubleClick()");
		poolCellUnselect();
		print.pendingCellClear();
		tree.repaint();
		if (userObject instanceof PoolCell) {
			XmlPrintCell cell = ((PoolCell) userObject).printCellGet();
			if (cell.isPhoto()) {
				ShowPhoto.show(cell.photoFileGet(), print.getCells());
			} else if (cell.isText()) {
				print.textEdit(cell);
			}
		}
	}

	/**
	 * mouse listener
	 */
	private class PoolMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			//LOG.trace("PoolMouseListener.mouseClicked(e)");
			TreePath path = tree.getPathForLocation(e.getX(), e.getY());
			if (path == null) {
				poolCellUnselect();
				print.pendingCellClear();
				tree.repaint();
				return;
			}
			Object obj = path.getLastPathComponent();
			clickDispatcher.dispatch(e,
					() -> {
						pendingClickedObject = obj;
						handleSimpleClick();
					},
					() -> handleDoubleClick(obj));
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
					Object obj = path.getLastPathComponent();
					tree.setSelectionPath(path);
					showContextMenu(e, getSelectedResource());
					tree.setSelectionPath(null);
				}
			}
		}
	}

	/**
	 * renderer
	 */
	private class PoolRenderer extends DefaultTreeCellRenderer {

		private static final String TT = "PoolRenderer.";

		private final Pool pool;
		private final int ins = 4;
		private final int icon_sz = Pool.ROW_SZ - ins;
		private final Border BORDER_RED = BorderFactory.createLineBorder(Color.RED, 2),
				BORDER_WHITE = BorderFactory.createLineBorder(Color.WHITE, 2),
				BORDER_GREEN = BorderFactory.createLineBorder(Color.GREEN, 2);

		public PoolRenderer(Pool pool) {
			this.pool = pool;
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			Component comp = super.getTreeCellRendererComponent(tree, value, sel,
					expanded, leaf, row, hasFocus);
			XmlPrintCell cell = null;
			if (value instanceof PoolCell) {
				cell = ((PoolCell) value).printCellGet();
			} else if (value instanceof DefaultMutableTreeNode) {
				Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
				if (userObj instanceof XmlPrintCell) {
					cell = (XmlPrintCell) userObj;
				}
			}
			if (cell != null) {
				if (cell.isPhoto()) {
					File fileCheck = cell.photoFileGet();
					if (fileCheck.exists() && fileCheck.isFile()) {
						setIcon(ImageUtil.getThumb(fileCheck, icon_sz - ins));
					} else {
						String filename = fileCheck.getAbsolutePath();
						String errorTxt = "<html><body "
								+ "style=\"padding:2px; "
								+ "text-align:center; "
								+ "color:red; "
								+ "font-size:9px;\">"
								+ "<b>⚠️</b>" + filename + " not find"
								+ "</body>"
								+ "</html>";
						setIcon(ImageUtil.createTextImage(errorTxt, icon_sz - ins));
					}
				} else {
					String txt = "<html>"
							+ "<body style=\"padding:5px;\">" + cell.textGet()
							+ "</body>"
							+ "</html>";
					setIcon(ImageUtil.createTextImage(txt, icon_sz - ins));
				}
				setText("");
				Border colorBorder;
				if (cell.pageGet() > 0) {
					colorBorder = BORDER_GREEN;
				} else if (sel) {
					colorBorder = BORDER_RED;
				} else {
					colorBorder = BORDER_WHITE;
				}
				setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createEmptyBorder(ins, 0, ins, 0),
						colorBorder
				));
				return comp;
			} else {
				setBorder(null);
				return comp;
			}
		}

	}

}
