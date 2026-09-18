package app.print;

import app.i18n.I18N;
import app.tools.ImageUtil;
import app.tools.LOG;
import app.tools.SwingTools;
import app.xml.XmlPrintCell;
import app.zdlg.ShowPhoto;
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

/**
 * handles a cell for Grid
 *
 * @author favdb
 */
public class GridCell extends JLabel {

	private static final String TT = "GridCell.";

	private final XmlPrintCell cell;
	private final Grid grid;
	private boolean selected = false;

	/**
	 * a cell for Print
	 *
	 * @param grid
	 * @param cell
	 */
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public GridCell(Grid grid, XmlPrintCell cell) {
		this.grid = grid;
		this.cell = cell;
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
		return cell;
	}

	/**
	 * initialize
	 */
	public void initialize() {
		this.setLayout(new BorderLayout());
		SwingTools.setFixedSize(this, grid.cellDim);
		this.setOpaque(true);
		this.setBackground(Color.WHITE);
		this.setHorizontalAlignment(SwingConstants.CENTER);
		this.setVerticalAlignment(SwingConstants.CENTER);
		refresh();
	}

	/**
	 * refresh
	 */
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
		int spanH = cell.spanHorizontalGet() > 0 ? cell.spanHorizontalGet() : 1;
		int spanV = cell.spanVerticalGet() > 0 ? cell.spanVerticalGet() : 1;
		int cellWidth = w * spanH;
		int cellHeight = h * spanV;
		int targetW = Math.max(10, cellWidth);
		int targetH = Math.max(10, cellHeight);
		if (cell.isPhoto()) {
			this.setBackground(Color.WHITE);
			if (cell.photoFileGet() != null && cell.photoFileGet().exists()) {
				int sz = Math.min(cellWidth, cellHeight);
				this.setIcon(ImageUtil.getImage(cell.photoFileGet(),
						Math.max(targetW, targetH), cell.zoomGet()));
			} else {
				this.setText("Photo introuvable (#" + cell.photoIdGet() + ")");
				this.setHorizontalAlignment(JLabel.CENTER);
			}
		} else if (cell.isText()) {
			this.setBackground(new Color(255, 255, 245));
			String textContent = (cell.textGet() != null) ? cell.textGet() : "";
			String txt = "<html>"
					+ "<head><style>"
					+ "body { font-size: 10px; }"
					+ "h1, h2, h3, p { margin-top: 1px; margin-bottom: 2px; padding: 0; }"
					+ "</style></head>"
					+ "<body>"
					+ textContent
					+ "</body>"
					+ "</html>";
			this.setVerticalAlignment(JLabel.TOP);
			setText(txt);
		} else {
			this.setBorder(BorderFactory.createDashedBorder(Color.LIGHT_GRAY, 2, 2, 1, false));
			this.setBackground(new Color(248, 248, 248));
			this.setText(String.valueOf(cell.cellNumGet()));
			this.setFont(this.getFont().deriveFont(14.0f));
			this.setForeground(Color.LIGHT_GRAY);
			this.setHorizontalAlignment(JLabel.CENTER);
			this.setVerticalAlignment(JLabel.CENTER);
		}
		SwingTools.setFixedSize(this, new Dimension(targetW, targetH));
		this.revalidate();
		this.repaint();
	}

	/**
	 * action for simple click
	 */
	private void actionSimpleClick() {
		//LOG.trace(TT + "actionSimpleClick()");
		if (grid.gridCellSelectedGet() != null && cell.isEmpty()) {
			grid.gridCellUnselect();
			return;
		}
		PoolCell poolCell = grid.getPrint().poolGet().poolCellSelectedGet();
		if (cell.isEmpty() && poolCell != null) {
			grid.getPrint().updateCell(poolCell.printCellGet(), cell.pageGet(), cell.posGet());
			grid.getPrint().poolGet().poolCellUnselect();
		}
	}

	/**
	 * action for double click
	 */
	private void actionDoubleClick() {
		//LOG.trace(TT + "actionDoubleClick()" + item.toString());
		if (cell.isPhoto()) {
			ShowPhoto.show(cell.photoFileGet(), grid.getPrint().getCells());
		} else if (cell.isText()) {
			grid.getPrint().textEdit(cell);
		}
		grid.gridCellUnselect();
	}

	/**
	 * set up interaction
	 */
	private final ClickDispatcher clickDispatcher = new ClickDispatcher(250);

	private void setupInteractions() {
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				clickDispatcher.dispatch(e,
						GridCell.this::actionSimpleClick,
						GridCell.this::actionDoubleClick);
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
					clickDispatcher.cancelPending();
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
		int cellNum = cell.cellNumGet();
		int col = (cellNum - 1) % totalCols, row = (cellNum - 1) / totalCols;
		if (cell.isEmpty()) {
			//create a text
			JMenuItem edit = new JMenuItem(I18N.getMsg("print.text_create"));
			edit.addActionListener(l -> grid.getPrint().textCreate(-1, cellNum + ",1,1"));
			menu.add(edit);
		} else {
			//call textEdit editor if text
			if (cell.isText()) {
				JMenuItem textEdit = new JMenuItem(I18N.getMsg("print.text_edit"));
				textEdit.addActionListener(al -> {
					grid.getPrint().textEdit(cell);
				});
				menu.add(textEdit);
			} else if (cell.isPhoto()) {
				JMenuItem textEdit = new JMenuItem(I18N.getMsg("print.pool.open_photo"));
				textEdit.addActionListener(al -> {
					ShowPhoto.show(cell.photoFileGet(), grid.getPrint().getCells());
				});
				menu.add(textEdit);
			}
			//set the zoom mode
			JMenu zoom = new JMenu(I18N.getMsg("print.zoom"));
			JMenuItem z0 = new JMenuItem(I18N.getMsg("print.zoom_none"));
			z0.addActionListener(z0n -> grid.zoomSet(cell, 0));
			zoom.add(z0);
			JMenuItem z1 = new JMenuItem(I18N.getMsg("print.zoom_contain"));
			z1.addActionListener(z1n -> grid.zoomSet(cell, 1));
			zoom.add(z1);
			JMenuItem z2 = new JMenuItem(I18N.getMsg("print.zoom_cover"));
			z2.addActionListener(z2n -> grid.zoomSet(cell, 2));
			zoom.add(z2);
			menu.add(zoom);
			//clear the cell
			JMenuItem clearCell = new JMenuItem(I18N.getMsg("print.clear"));
			clearCell.setEnabled(cell.photoIdGet() != "-1"
					|| cell.textIdGet() != "-1"
					|| (cell.textGet() != null && !cell.textGet().isEmpty()));
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
			incSpanH.setEnabled(grid.isAllowedSpanH(cell));
			incSpanH.addActionListener(al -> {
				grid.setSpanH(cell, +1);
			});
			sub.add(incSpanH);
			JMenuItem decSpanH = new JMenuItem(I18N.getMsg("print.menu.spanh.dec") + " (-1)");
			decSpanH.setEnabled(cell.spanHorizontalGet() > 1);
			decSpanH.addActionListener(al -> {
				grid.setSpanH(cell, -1);
			});
			sub.add(decSpanH);
			//vertical span
			JMenuItem incSpanV = new JMenuItem(I18N.getMsg("print.menu.spanv.inc") + " (+1)");
			incSpanV.setEnabled(grid.isAllowedSpanV(cell));
			incSpanV.addActionListener(al -> {
				grid.setSpanV(cell, +1);
			});
			sub.add(incSpanV);
			JMenuItem decSpanV = new JMenuItem(I18N.getMsg("print.menu.spanv.dec") + " (-1)");
			decSpanV.setEnabled(cell.spanVerticalGet() > 1);
			decSpanV.addActionListener(al -> {
				grid.setSpanV(cell, -1);
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
		cell.pageSet(0);
		print.actionSave();
		print.poolGet().poolCellUnselect();
		print.poolGet().refresh();
		print.gridGet().refresh();
		print.refreshButtons();
	}

}
