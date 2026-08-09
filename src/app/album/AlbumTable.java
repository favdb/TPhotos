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
import app.xml.Xml;
import app.xml.XmlAlbum;
import app.xml.XmlAlbumItem;
import i18n.I18N;
import java.awt.Component;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import tools.TableColumnAdjuster;

/**
 * Table for items in photo album.
 *
 * @author favdb
 */
public class AlbumTable extends JTable {

	private static final String TT = "AlbumTable.";
	public Xml xml;
	private final Album album;
	private boolean modified = false;
	private List<XmlAlbumItem> photos;

	public AlbumTable(Album album) {
		super();
		this.album = album;
		initialize();
	}

	public boolean isModified() {
		return modified;
	}

	public Album getAlbumPanel() {
		return album;
	}

	/**
	 * initialize
	 */
	private void initialize() {
		this.setFont(App.fontGet());
		setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
		CellEditorListener notif = new CellEditorListener() {
			@Override
			public void editingCanceled(ChangeEvent e) {
				// empty
			}

			@Override
			public void editingStopped(ChangeEvent e) {
				try {
					modified = true;
					App.updateTitle();
				} catch (Exception ex) {
				}
			}
		};
		getDefaultEditor(String.class).addCellEditorListener(notif);
		setModel(new DefaultTableModel(
				new Object[][]{},
				new String[]{"N°", "Photo", "Commentaire"}) {
			boolean[] canEdit = new boolean[]{false, false, true};
			final Class[] columnClass = new Class[]{Integer.class, File.class, String.class};

			@Override
			public boolean isCellEditable(int row, int col) {
				return canEdit[col];
			}

			@Override
			public Class<?> getColumnClass(int col) {
				return columnClass[col];
			}
		});
		setDefaultRenderer(File.class, new FileRenderer());
		TableColumnAdjuster tca = new TableColumnAdjuster(this);
		tca.adjustColumns();
		this.addKeyListener(new KeyListener(this));
		this.addMouseListener(new TableMouse(this));
	}

	/**
	 * load table from Xml / XmlAlbum
	 *
	 * @param xml
	 */
	public void load(Xml xml) {
		this.xml = xml;
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.setRowCount(0);

		if (xml != null && xml.albumGet() != null) {
			XmlAlbum xmlAlbum = xml.albumGet();
			photos = xmlAlbum.itemsGet();
			for (XmlAlbumItem item : photos) {
				Object[] objs = {
					item.idGet(),
					item,
					item.commentGet()
				};
				model.addRow(objs);
			}
		}
		modified = false;
		App.updateTitle();
	}

	/**
	 * delete selected lines
	 */
	void removeSelectedRows() {
		if (isEditing()) {
			getCellEditor().stopCellEditing();
		}
		DefaultTableModel model = (DefaultTableModel) this.getModel();
		int[] rows = getSelectedRows();
		for (int i = 0; i < rows.length; i++) {
			model.removeRow(rows[i] - i);
		}
		renumber();
		this.clearSelection();
		album.getGallery().refresh();
	}

	/**
	 * add an AlbumItem in table
	 *
	 * @param item
	 */
	void rowAdd(XmlAlbumItem item) {
		Object[] objs = {item.idGet(), item, item.commentGet()};
		DefaultTableModel model = (DefaultTableModel) this.getModel();
		model.addRow(objs);
		renumber();
	}

	public void rowRemove(int row) {
		DefaultTableModel model = (DefaultTableModel) this.getModel();
		if (row >= 0 && row < model.getRowCount()) {
			model.removeRow(row);
			renumber();
		}
	}

	/**
	 * move given line to up
	 */
	private void mouveUp() {
		int row = getSelectedRow();
		if (row < 1) {
			return;
		}
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.moveRow(row, row, row - 1);
		renumber();
		setRowSelectionInterval(row - 1, row - 1);
	}

	/**
	 * move given line down
	 */
	private void moveDown() {
		int row = getSelectedRow();
		if (row >= getRowCount() - 1) {
			return;
		}
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.moveRow(row, row, row + 1);
		renumber();
		setRowSelectionInterval(row + 1, row + 1);
	}

	/**
	 * update comment for the given line
	 *
	 * @param row
	 * @param comment
	 */
	public void updateComment(int row, String comment) {
		File file = (File) getValueAt(row, 1);
		String nc = album.diapoParamGet().getComment(file, comment);
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.setValueAt(nc, row, 2);
		setModified();
	}

	/**
	 * Re-number lines
	 */
	private void renumber() {
		for (int i = 0; i < getRowCount(); i++) {
			this.setValueAt(i + 1, i, 0);
		}
		modified = true;
		App.updateTitle();
	}

	/**
	 * get the Xml
	 *
	 * @return
	 */
	public Xml xmlGet() {
		return xml;
	}

	/**
	 * save table content in XmlAlbum and save to wml file
	 *
	 * @param title Le titre courant de l'album
	 */
	public void save(String title) {
		//LOG.trace(TT + "save()");
		if (modified && xml != null) {
			XmlAlbum xmlAlbum = xml.albumGet();
			if (xmlAlbum != null) {
				xmlAlbum.titleSet(title);

				// Reconstruit la liste des items à partir du tableau Swing
				List<XmlAlbumItem> newItems = new ArrayList<>();
				DefaultTableModel model = (DefaultTableModel) getModel();
				for (int i = 0; i < model.getRowCount(); i++) {
					Object val = model.getValueAt(i, 1);
					XmlAlbumItem item;
					if (val instanceof XmlAlbumItem) {
						item = (XmlAlbumItem) val;
					} else if (val instanceof File) {
						item = new XmlAlbumItem("" + (i + 1),
								((File) val).getAbsolutePath(),
								(String) model.getValueAt(i, 2));
					} else {
						continue;
					}
					item.idSet("" + (i + 1));
					item.commentSet((String) model.getValueAt(i, 2));
					newItems.add(item);
				}
				xmlAlbum.itemsSet(newItems);
			}
			xml.save();
			modified = false;
			album.xmlGet().printGet().updateAll();
			App.updateTitle();
		}
	}

	/**
	 * get AlbumItem for given row
	 *
	 * @param row : line index
	 * @return AlbumItem
	 */
	public XmlAlbumItem getRow(int row) {
		if (row >= 0 && row < getRowCount()) {
			int id = (Integer) getValueAt(row, 0);
			File file = (File) getValueAt(row, 1);
			String text = (String) getValueAt(row, 2);
			return new XmlAlbumItem("" + id, text, file.getAbsolutePath());
		}
		return null;
	}

	/**
	 * Tag table modified
	 */
	public void setModified() {
		modified = true;
		App.updateTitle();
	}

	/**
	 * KeyListener for table
	 */
	private static class KeyListener implements java.awt.event.KeyListener {

		private final AlbumTable table;

		public KeyListener(AlbumTable albumTable) {
			this.table = albumTable;
		}

		@Override
		public void keyTyped(KeyEvent e) {
			int key = e.getKeyCode();
			char keychar = e.getKeyChar();
			if (key == VK_DELETE || keychar == 0x007F) {
				table.rowRemove(table.getSelectedRow());
			}
			if (keychar == '-' && e.isControlDown()) {
				table.mouveUp();
			}
			if (keychar == '+' && e.isControlDown()) {
				table.moveDown();
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

	/**
	 * Mouse listener for contextual menu
	 */
	private static class TableMouse implements MouseListener {

		private final AlbumTable table;

		public TableMouse(AlbumTable albumTable) {
			this.table = albumTable;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {
				if (table.getSelectedRows().length > 1) {
					JPopupMenu popupMenu = new JPopupMenu();
					JMenuItem item1 = new JMenuItem(I18N.getMsg("album.modify.comments"));
					item1.addActionListener(act -> table.getAlbumPanel().changeComments());
					popupMenu.add(item1);
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// empty
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// empty
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// empty
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// empty
		}
	}

	/**
	 * renderer File/Photo
	 */
	public class FileRenderer extends JLabel implements TableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			if (value instanceof XmlAlbumItem) {
				XmlAlbumItem x = (XmlAlbumItem) value;
				JLabel lb = new JLabel(IconUtil.getIconSmall(ICONS.K.PHOTO));
				lb.setText("");
				lb.setToolTipText(x.photoGet());
				table.setRowHeight(row, IconUtil.getDefSize());
				if (isSelected || hasFocus) {
					lb.setBackground(table.getSelectionBackground());
					lb.setOpaque(true);
				}
				return lb;
			}
			return this;
		}
	}

}
