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
import java.awt.Component;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.*;
import java.io.File;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import tools.file.FileUtil;
import tools.xml.XML;
import tools.xml.Xml;

/**
 *
 * @author favdb
 */
public class AlbumTable extends JTable {

	private static final String TT = "AlbumTable.";
	public Xml xml;
	private final Album albumPanel;
	private boolean modified = false;

	public AlbumTable(Album albumPanel) {
		super();
		this.albumPanel = albumPanel;
		initialize();
	}

	public boolean isModified() {
		return modified;
	}

	private void initialize() {
		this.setFont(App.fontGet());
		setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
		CellEditorListener notif = new CellEditorListener() {
			@Override
			public void editingCanceled(ChangeEvent e) {
				//empty
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
	}

	public void load(Xml xml) {
		//LOG.trace(TT + "load(xml)");
		this.xml = xml;
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.setRowCount(0);
		NodeList nodes = xml.getRoot().getElementsByTagName("item");
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				Element child = (Element) nodes.item(i);
				Object[] objs = {
					i + 1,//Integer.valueOf(child.getAttribute("id")),
					new File(App.preferences.photosDirGet() + File.separator + child.getAttribute("file")),
					child.getAttribute("comment")
				};
				addRow(objs);
			}
		}
		modified = false;
		App.updateTitle();
	}

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
	}

	void addRow(Object[] objs) {
		//LOG.trace(TT + "addRow(objs=" + objs.toString() + ")");
		DefaultTableModel model = (DefaultTableModel) this.getModel();
		model.addRow(objs);
		renumber();
	}

	void addRow(AlbumItem item) {
		Object[] objs = {item.id, item.file, item.text};
		DefaultTableModel model = (DefaultTableModel) this.getModel();
		model.addRow(objs);
		renumber();
	}

	private void mouveUp() {
		//LOG.trace(TT + "moveUp()");
		int row = getSelectedRow();
		if (row < 1) {
			return;
		}
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.moveRow(row, row, row - 1);
		renumber();
		setRowSelectionInterval(row - 1, row - 1);
	}

	private void moveDown() {
		//LOG.trace(TT + "moveDown()");
		int row = getSelectedRow();
		if (row >= getRowCount()) {
			return;
		}
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.moveRow(row, row, row + 1);
		renumber();
		setRowSelectionInterval(row + 1, row + 1);
	}

	private void renumber() {
		for (int i = 1; i < getRowCount(); i++) {
			this.setValueAt(i + 1, i, 0);
		}
		modified = true;
		App.updateTitle();
	}

	public Xml getXml() {
		return xml;
	}

	public void save(String title) {
		//LOG.trace(TT + "save()");
		if (modified && xml != null) {
			StringBuilder b = new StringBuilder(XML.getHeader())
					.append("<album title=\"").append(title).append("\">\n")
					.append(albumPanel.getAlbumParam().toXml())
					.append("   <list>\n");
			for (int i = 0; i < this.getRowCount(); i++) {
				b.append(rowToXml(i));
			}
			b.append("   </list>\n")
					.append("</album>");
			xml.close();
			FileUtil.fileWriteString(xml.getFile(), b.toString());
		}
	}

	private String rowToXml(int i) {
		Integer id = (Integer) getValueAt(i, 0);
		String file = ((File) getValueAt(i, 1)).getAbsolutePath()
				.replace(App.preferences.photosDirGet() + File.separator, "");
		String comment = (String) getValueAt(i, 2);
		return "      <item "
				+ "id=\"" + id + "\" "
				+ "file=\"" + file + "\" "
				+ "comment=\"" + comment + "\" "
				+ " />\n";
	}

	public void setModified() {
		modified = true;
		App.updateTitle();
	}

	private static class KeyListener implements java.awt.event.KeyListener {

		private final AlbumTable table;

		public KeyListener(AlbumTable albumTable) {
			this.table = albumTable;
		}

		@Override
		public void keyTyped(KeyEvent e) {
			//LOG.trace("KeyActions.keyTyped(e=" + e.toString() + ")");
			int key = e.getKeyCode();
			char keychar = e.getKeyChar();
			if (key == VK_DELETE || keychar == 0x007F) {
				table.removeSelectedRows();
				table.clearSelection();
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
			//empty
		}

		@Override
		public void keyReleased(KeyEvent e) {
			//empty
		}
	}

	public class FileRenderer extends JLabel implements TableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			if (value instanceof File) {
				JLabel lb = new JLabel(IconUtil.getIconSmall(ICONS.K.PHOTO));
				lb.setText("");
				lb.setToolTipText(((File) value).getName());
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
