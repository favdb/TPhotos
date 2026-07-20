package app.print;

import app.App;
import i18n.I18N;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import tools.ImageUtil;
import tools.LOG;
import tools.SwingTools;

public class GridCell extends JLabel {

	private static final String TT = "GridImage.";

	private PrintCell item;
	private final Grid grid;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public GridCell(Grid grid, PrintCell item) {
		this.grid = grid;
		this.item = item;
		initialize();
		setupInteractions();
	}

	public PrintCell printCellGet() {
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
		this.setBorder(null);
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
			if (item.photoFileGet() != null && !item.photoFileGet().isEmpty()) {
				int sz = Math.min(cellWidth, cellHeight);
				this.setIcon(ImageUtil.getImage(item.photoFileGet(), Math.max(targetW, targetH)));
			} else {
				this.setText("Photo vide (#" + item.photoIdGet() + ")");
				this.setHorizontalAlignment(JLabel.CENTER);
			}
		} else if (item.isText()) {
			this.setBackground(new Color(255, 255, 245));
			String textContent = (item.textGet() != null) ? item.textGet() : "";
			String html = String.format("<html><body style='padding:1px; font-size:%dpx;'>%s</body></html>",
					App.fontGet().getSize(), textContent);
			String txt = "<html>"
					+ "<body style='"
					+ "font-size:" + 10 + "px;'"
					+ "h1, h2, h3, p { margin-top: 1px; margin-bottom: 2px; padding: 0; }"
					+ ">"
					+ textContent
					+ "</body></html>";
			this.setVerticalAlignment(JLabel.TOP);
			setText(txt);
		} else { // Affichage pour une case vide
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

	private void handleSimpleClick() {
		LOG.trace(TT + "handleSimpleClick()");
		if (grid.gridCellSelectedGet() != null) {
			GridCell selected = grid.gridCellSelectedGet();
			if (selected.item.isText()) {
				return;
			}
			if (selected.item.isPhoto()) { // swap avec selected
				grid.getPrint().swapCell(item, selected.item);
			}
			if (item.isEmpty()) { // affectation du Grid selected
				PrintCell dest = selected.item;
				dest.pageSet(item.pageGet());
				dest.posSet(item.posGet());
				grid.getPrint().updateCell(dest, item.pageGet(), item.posGet());
			}
			return;
		}
		PoolCell poolCell = grid.getPrint().poolGet().poolCellSelectedGet();
		if (poolCell != null) {
			grid.getPrint().updateCell(poolCell.printCellGet(), item.pageGet(), item.posGet());
		}
	}

	private void handleDoubleClick() {

		grid.gridCellUnselect();

		if (item.isPhoto()) {
			grid.getPrint().getMainFrame().showPhoto(item.photoFileGet());
		} else if (item.isText()) {
			grid.getPrint().shefEdit(item);
		}
	}

	private final Timer timer = new Timer(250, e -> handleSimpleClick());

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
							handleDoubleClick();
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

	private void showContextMenu(MouseEvent e) {
		if (grid == null) {
			return;
		}
		JPopupMenu menu = new JPopupMenu();
		int totalCols = grid.colsGet();
		int totalRows = grid.rowsGet();
		int cellNum = item.cellNumGet();
		int col = (cellNum - 1) % totalCols;
		int row = (cellNum - 1) / totalCols;

		JMenuItem incSpanH = new JMenuItem(I18N.getMsg("print.menu.spanh.inc") + " (+1)");
		incSpanH.setEnabled(grid.isAllowedSpanH(item));
		incSpanH.addActionListener(al -> {
			grid.setSpanH(item, +1);
			item.spanHorizontalSet(item.spanHorizontalGet() + 1);
			grid.setModified();
			grid.refresh();
		});
		menu.add(incSpanH);
		JMenuItem decSpanH = new JMenuItem(I18N.getMsg("print.menu.spanh.dec") + " (-1)");
		decSpanH.setEnabled(item.spanHorizontalGet() > 1);
		decSpanH.addActionListener(al -> {
			grid.setSpanH(item, -1);
		});
		menu.add(decSpanH);
		menu.addSeparator();

		JMenuItem incSpanV = new JMenuItem(I18N.getMsg("print.menu.spanv.inc") + " (+1)");
		incSpanV.setEnabled(grid.isAllowedSpanV(item));
		incSpanV.addActionListener(al -> {
			grid.setSpanV(item, +1);
		});
		menu.add(incSpanV);
		JMenuItem decSpanV = new JMenuItem(I18N.getMsg("print.menu.spanv.dec") + " (-1)");
		decSpanV.setEnabled(item.spanVerticalGet() > 1);
		decSpanV.addActionListener(al -> {
			grid.setSpanV(item, -1);
		});
		menu.add(decSpanV);

		menu.addSeparator();
		//shef editor if text
		if (item.isText()) {
			JMenuItem toggleType = new JMenuItem(I18N.getMsg("print.menu.type.text"));
			toggleType.addActionListener(al -> {
				grid.getPrint().shefEdit(item);
			});
			menu.add(toggleType);
		}
		//clear cell
		JMenuItem clearCell = new JMenuItem(I18N.getMsg("print.menu.clear"));
		clearCell.setEnabled(item.photoIdGet() != -1 || item.textIdGet() != -1 || !item.textGet().isEmpty());
		clearCell.addActionListener(al -> {
			releaseCellInPool();
			item.clear();
			grid.setModified();
			grid.getPrint().refresh();
		});
		menu.add(clearCell);
		menu.show(e.getComponent(), e.getX(), e.getY());
	}

	private void releaseCellInPool() {
		Print print = grid.getPrint();
		if (print == null || print.getCells() == null) {
			return;
		}
		item.pageSet(0);
		print.refresh();
	}

}
