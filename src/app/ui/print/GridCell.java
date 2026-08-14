package app.ui.print;

import app.i18n.I18N;
import app.tools.ImageUtil;
import app.tools.LOG;
import app.tools.SwingTools;
import app.xml.XmlPrintCell;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * handles a cell for Grid
 *
 * @author favdb
 */
public class GridCell extends JLabel {

	private static final String TT = "GridCell.";

	private XmlPrintCell item;
	private final Grid grid;
	private boolean selected = false;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public GridCell(Grid grid, XmlPrintCell item) {
		this.grid = grid;
		this.item = item;
		initialize();
		setupInteractions();
	}

	public void selectedSet() {
		selected = true;
	}

	public void selectedUnset() {
		selected = false;
	}

	public boolean selectedCheck() {
		return selected;
	}

	public XmlPrintCell printCellGet() {
		return item;
	}

	public void initialize() {
		this.setLayout(new BorderLayout());
		SwingTools.setFixedSize(this, grid.cellDim);
		this.setOpaque(true);
		this.setBackground(Color.WHITE);
		this.setHorizontalAlignment(SwingConstants.CENTER);
		this.setVerticalAlignment(SwingConstants.CENTER);
		refresh();
	}

	public void refresh() {
		this.removeAll();
		this.setIcon(null);
		this.setText("");
		this.setBorder(BorderFactory.createLineBorder((selected ? Color.red : Color.WHITE), 2));
		int w = grid.imgGetSize().width;
		int h = grid.imgGetSize().height;
		if (w <= 0 || h <= 0) {
			int disponibleWidth = grid.getPreferredSize().width - (56 * 2);
			int disponibleHeight = grid.getPreferredSize().height - (56 * 2);
			w = disponibleWidth / grid.colsGet();
			h = disponibleHeight / grid.rowsGet();
		}
		int spanH = item.spanHorizontalGet() > 0 ? item.spanHorizontalGet() : 1;
		int spanV = item.spanVerticalGet() > 0 ? item.spanVerticalGet() : 1;

		int cellWidth = w * spanH;
		int cellHeight = h * spanV;

		int targetW = Math.max(10, cellWidth);
		int targetH = Math.max(10, cellHeight);

		if (item.isPhoto()) {
			this.setBackground(Color.WHITE);
			if (item.photoFileGet() != null && item.photoFileGet().exists()) {
				int sz = Math.min(cellWidth, cellHeight);
				this.setIcon(ImageUtil.getImage(item.photoFileGet(),
						Math.max(targetW, targetH), item.zoomGet()));
			} else {
				this.setText("Photo introuvable (#" + item.photoIdGet() + ")");
				this.setHorizontalAlignment(JLabel.CENTER);
			}
		} else if (item.isText()) {
			this.setBackground(new Color(255, 255, 245));
			String textContent = (item.textGet() != null) ? item.textGet() : "";
			String txt = "<html>"
					+ "<body style='"
					+ "font-size:" + 10 + "px;'"
					+ "h1, h2, h3, p { "
					+ "margin-top: 1px; "
					+ "margin-bottom: 2px; "
					+ "padding: 0; }"
					+ ">"
					+ textContent
					+ "</body>"
					+ "</html>";
			this.setVerticalAlignment(JLabel.TOP);
			setText(txt);
		} else {
			this.setBorder(BorderFactory.createDashedBorder(Color.LIGHT_GRAY, 2, 2, 1, false));
			this.setBackground(new Color(248, 248, 248));
			this.setText(String.valueOf(item.cellNumGet()));
			this.setFont(this.getFont().deriveFont(14.0f));
			this.setForeground(Color.LIGHT_GRAY);
			this.setHorizontalAlignment(JLabel.CENTER);
			this.setVerticalAlignment(JLabel.CENTER);
		}
		SwingTools.setFixedSize(this, new Dimension(targetW, targetH));
		this.revalidate();
		this.repaint();
	}

	private void actionSimpleClick() {
		//LOG.trace(TT + "actionSimpleClick()");
		if (grid.gridCellSelectedGet() != null && item.isEmpty()) {
			//there is a selected cell selected in Grid
			/* not used
			GridCell srce = grid.gridCellSelectedGet();
			if (srce.isSelected) srce.unSelect();
			if (!srce.item.spanGet().equals("1,1")) {
				//not allowed
				return;
			}
			if (cell.item.isText() || cell.item.isPhoto()) {
				XmlPrintCell dest = cell.item;
				dest.pageSet(item.pageGet());
				dest.posSet(item.posGet());
				grid.getPrint().updateCell(dest, item.pageGet(), item.posGet());
			}
			 */
			return;
		}
		PoolCell poolCell = grid.getPrint().poolGet().poolCellSelectedGet();
		if (item.isEmpty() && poolCell != null) {
			//there is a poolcell to place to thie empty cell
			grid.getPrint().updateCell(poolCell.printCellGet(), item.pageGet(), item.posGet());
		}
	}

	private void actionDoubleClick() {
		//LOG.trace(TT + "actionDoubleClick()" + item.toString());
		if (item.isPhoto()) {
			grid.getPrint().getMainFrame().showPhoto(item.photoFileGet());
		} else if (item.isText()) {
			grid.getPrint().textEdit(item);
		}/* else if (item.isEmpty()) {
			grid.getPrint().textCreate(item);
		}*/
		grid.gridCellUnselect();
	}

	private final Timer timer = new Timer(250, e -> actionSimpleClick());

	{
		timer.setRepeats(false);
	}

	private void setupInteractions() {
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					switch (e.getClickCount()) {
						case 1:
							timer.restart();
							break;
						case 2:
							timer.stop();
							actionDoubleClick();
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

			private void showPopupMenu(MouseEvent e) {
				if (e.isPopupTrigger()) {
					timer.stop();
					showContextMenu(e);
				}
			}
		}
		);
	}

	/**
	 * show context menu
	 *
	 * @param e
	 */
	private void showContextMenu(MouseEvent e) {
		if (grid == null) {
			LOG.err(TT + "showContextMenu(e) grid is null");
			return;
		}
		JPopupMenu menu = new JPopupMenu();
		int totalCols = grid.colsGet(), totalRows = grid.rowsGet();
		int cellNum = item.cellNumGet();
		int col = (cellNum - 1) % totalCols, row = (cellNum - 1) / totalCols;
		if (item.isEmpty()) {
			JMenuItem edit = new JMenuItem(I18N.getMsg("print.text_create"));
			edit.addActionListener(l -> grid.getPrint().textCreate(item));
			menu.add(edit);
		} else {
			//call textEdit editor if text
			if (item.isText()) {
				JMenuItem textEdit = new JMenuItem(I18N.getMsg("print.text_edit"));
				textEdit.addActionListener(al -> {
					grid.getPrint().textEdit(item);
				});
				menu.add(textEdit);
			} else if (item.isPhoto()) {
				JMenuItem textEdit = new JMenuItem(I18N.getMsg("print.pool.open_photo"));
				textEdit.addActionListener(al -> {
					grid.getPrint().getMainFrame().showPhoto(item.photoFileGet());
				});
				menu.add(textEdit);
			}/* else if (item.isEmpty()) {
				JMenuItem textCreate = new JMenuItem(I18N.getMsg("print.text_create"));
				textCreate.addActionListener(al -> {
					grid.getPrint().textCreate(item);
				});
				menu.add(textCreate);
			}*/
			//clear the cell
			JMenuItem clearCell = new JMenuItem(I18N.getMsg("print.clear"));
			clearCell.setEnabled(item.photoIdGet() != -1
					|| item.textIdGet() != -1
					|| !item.textGet().isEmpty());
			clearCell.addActionListener(al -> {
				releaseCellInPool();
				grid.setModified();
				grid.refresh();
			});
			menu.add(clearCell);
			//increase decrease cell span
			JMenu sub = new JMenu(I18N.getMsg("print.menu.span"));
			menu.add(sub);
			JMenuItem incSpanH = new JMenuItem(I18N.getMsg("print.menu.spanh.inc") + " (+1)");
			incSpanH.setEnabled(grid.isAllowedSpanH(item));
			incSpanH.addActionListener(al -> {
				grid.setSpanH(item, +1);
			});
			sub.add(incSpanH);
			JMenuItem decSpanH = new JMenuItem(I18N.getMsg("print.menu.spanh.dec") + " (-1)");
			decSpanH.setEnabled(item.spanHorizontalGet() > 1);
			decSpanH.addActionListener(al -> {
				grid.setSpanH(item, -1);
			});
			sub.add(decSpanH);
			//vertical span
			JMenuItem incSpanV = new JMenuItem(I18N.getMsg("print.menu.spanv.inc") + " (+1)");
			incSpanV.setEnabled(grid.isAllowedSpanV(item));
			incSpanV.addActionListener(al -> {
				grid.setSpanV(item, +1);
			});
			sub.add(incSpanV);
			JMenuItem decSpanV = new JMenuItem(I18N.getMsg("print.menu.spanv.dec") + " (-1)");
			decSpanV.setEnabled(item.spanVerticalGet() > 1);
			decSpanV.addActionListener(al -> {
				grid.setSpanV(item, -1);
			});
			sub.add(decSpanV);
		}
		menu.show(e.getComponent(), e.getX(), e.getY());
	}

	private void releaseCellInPool() {
		//LOG.trace(TT + "releaseCellInPool() item=" + item.toString());
		Print print = grid.getPrint();
		if (print == null || print.getCells() == null) {
			return;
		}
		item.pageSet(0);
		print.xmlGet().save();
		print.poolGet().poolCellUnselect();
		print.poolGet().refresh();
		print.gridGet().refresh();
	}

}
