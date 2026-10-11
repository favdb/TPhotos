package app.print;

import api.mig.MIG;
import api.mig.swing.MigLayout;
import static app.print.Print.*;
import app.tools.LOG;
import app.xml.XmlPrintCell;
import app.xml.XmlPrintPage;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * JPanel class as a viewer container. Include a PagePanel representing a virtual sheet.
 *
 * @author favdb
 */
public class Grid extends JPanel {

	private static final String TT = "Grid.";

	private final Print print;
	private final PagePanel pagePanel;
	Dimension cellConf, cellDim;
	private int rows, cols;
	private int imgWidth, imgHeight;
	private GridCell gridCellSelected;

	/**
	 * defining a Grid for the given Print object
	 *
	 * @param print
	 */
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Grid(Print print) {
		this.print = print;
		this.setBackground(new Color(220, 220, 220)); // Fond gris zone de travail
		this.setLayout(new GridBagLayout()); // Centre le PagePanel

		pagePanel = new PagePanel();
		this.add(pagePanel);

		initialize();
	}

	/**
	 * set dimensions (depending format, orientation, base grid, margins)
	 *
	 * @param format
	 * @param orientation
	 */
	public void setDim(String format, String orientation) {
		//LOG.trace(TT + "setDim(format=" + format + ", orientation=" + orientation + ")");
		boolean isLandscape = !PORTRAIT.equalsIgnoreCase(orientation);

		if ("A3".equalsIgnoreCase(format)) {
			cols = isLandscape ? 10 : 6;
			rows = isLandscape ? 6 : 10;
		} else {
			cols = isLandscape ? 6 : 4;
			rows = isLandscape ? 4 : 6;
		}

		int baseCols = isLandscape ? cols : rows;
		int baseRows = isLandscape ? rows : cols;

		int panelWidth = 1024;
		int panelHeight = 768;

		int size = Math.min(panelWidth / baseCols, panelHeight / baseRows);

		cellDim = new Dimension(size, size);
		cellConf = new Dimension(cols, rows);
		imgWidth = size;
		imgHeight = size;
	}

	/**
	 * initialize
	 */
	public void initialize() {
		//LOG.trace(TT + "initialize()");
		setDim(print.paperFormatGet(), print.paperOrientationGet());
	}

	/**
	 * refresh
	 */
	public void refresh() {
		LOG.trace(TT + "refresh()");
		pagePanel.removeAll();

		// 1. Load margins (Top, Left, Right, Bottom) and central binding flag
		int[] margins = print.xmlPrintGet().marginsIntGet();
		int top = (margins != null && margins.length > 0) ? margins[0] : 0;
		int left = (margins != null && margins.length > 1) ? margins[1] : 0;
		int right = (margins != null && margins.length > 2) ? margins[2] : 0;
		int bottom = (margins != null && margins.length > 3) ? margins[3] : 0;

		boolean isLandscape = !print.xmlPrintGet().isPortrait();
		boolean hasCentralBinding = print.xmlPrintGet().centralGet();

		// 2. Configure MigLayout rows and cols
		StringBuilder rowC = new StringBuilder();
		for (int i = 0; i < rows; i++) {
			rowC.append("[]");
		}

		StringBuilder colC = new StringBuilder();
		int halfCols = cols / 2; // Répartition des colonnes de chaque côté

		if (isLandscape && hasCentralBinding && cols >= 2) {
			// En mode paysage avec reliure centrale : Colonnes gauche + Colonne vide centrale + Colonnes droite
			for (int j = 0; j < halfCols; j++) {
				colC.append("[]");
			}
			// Colonne centrale de reliure (largeur définie par left + right)
			int centralGap = left + right;
			colC.append("[").append(centralGap).append("!]");
			for (int j = halfCols; j < cols; j++) {
				colC.append("[]");
			}
		} else {
			for (int j = 0; j < cols; j++) {
				colC.append("[]");
			}
		}

		int gap = (print.xmlPrintGet().gapGet() ? 12 : 0);
		String layoutConstraints = String.format("gap " + gap + ", ins %d %d %d %d",
				top, left, bottom, right);
		pagePanel.setLayout(new MigLayout(MIG.get(layoutConstraints),
				colC.toString(), rowC.toString()));

		imgWidth = cellDim.width;
		imgHeight = cellDim.height;

		int currentPage = print.gridCurrentPageGet();
		boolean[][] occupied = new boolean[rows][cols];

		// 3. Place real cells with offset adjustment if past the central column
		if (print.getCells() != null) {
			for (XmlPrintCell cell : print.getCells()) {
				if (cell.pageGet() == currentPage) {
					int cellNum = cell.cellNumGet();
					if (cellNum < 1 || cellNum > (rows * cols)) {
						continue;
					}
					int r = (cellNum - 1) / cols;
					int c = (cellNum - 1) % cols;
					int sH = cell.spanHorizontalGet() > 0 ? cell.spanHorizontalGet() : 1;
					int sV = cell.spanVerticalGet() > 0 ? cell.spanVerticalGet() : 1;

					if (c + sH > cols) {
						sH = cols - c;
					}
					if (r + sV > rows) {
						sV = rows - r;
					}

					for (int i = 0; i < sV; i++) {
						for (int j = 0; j < sH; j++) {
							if (r + i < rows && c + j < cols) {
								occupied[r + i][c + j] = true;
							}
						}
					}

					GridCell img = new GridCell(this, cell);

					// Ajustement de l'index de la colonne MigLayout si on est après la reliure centrale
					int layoutCol = c;
					if (isLandscape && hasCentralBinding && cols >= 2 && c >= halfCols) {
						layoutCol = c + 1; // +1 pour sauter la colonne centrale insérée
					}

					String constraint = String.format("top, cell %d %d %d %d", layoutCol, r, sH, sV);
					pagePanel.add(img, constraint);
				}
			}
		}

		// 4. Place empty cells
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (!occupied[r][c]) {
					int targetCellNum = (r * cols) + c + 1;

					XmlPrintCell emptyCell = new XmlPrintCell();
					emptyCell.cellNumSet(targetCellNum);
					emptyCell.pageSet(currentPage);
					emptyCell.posSet(targetCellNum + ",1,1");

					GridCell emptyImg = new GridCell(this, emptyCell);

					int layoutCol = c;
					if (isLandscape && hasCentralBinding && cols >= 2 && c >= halfCols) {
						layoutCol = c + 1;
					}

					String constraint = String.format("cell %d %d 1 1", layoutCol, r);
					pagePanel.add(emptyImg, constraint);
					occupied[r][c] = true;
				}
			}
		}

		this.revalidate();
		this.repaint();
	}

	/**
	 * Internal component representing the virtual sheet.
	 */
	private class PagePanel extends JPanel {

		public PagePanel() {
			this.setBackground(Color.WHITE);
			this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1)); // BORDURE DU PAPIER
		}

		/**
		 * paint component of the grid
		 *
		 * @param g
		 */
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			int[] margins = print.xmlPrintGet().marginsIntGet();
			int top = (margins != null && margins.length > 0) ? margins[0] : 0;
			int left = (margins != null && margins.length > 1) ? margins[1] : 0;
			int right = (margins != null && margins.length > 2) ? margins[2] : 0;
			int bottom = (margins != null && margins.length > 3) ? margins[3] : 0;

			// Vérifie si on est en paysage et si la marge centrale est active
			boolean isLandscape = !print.xmlPrintGet().isPortrait();
			boolean hasCentralBinding = print.xmlPrintGet().centralGet();

			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor(Color.RED);

			if (isLandscape && hasCentralBinding) {
				// Espace central égal à left + right
				int centralGap = left + right;
				int totalWidth = getWidth();
				int availableWidth = totalWidth - left - right - centralGap;
				int halfWidth = availableWidth / 2;
				int h = getHeight() - top - bottom;

				if (halfWidth > 0 && h > 0) {
					// Premier cadre (gauche)
					g2d.drawRect(left, top, halfWidth - 1, h - 1);
					// Second cadre (droite) après l'espace central
					int secondX = left + halfWidth + centralGap;
					g2d.drawRect(secondX, top, halfWidth - 1, h - 1);
				}
			} else {
				// Comportement standard (portrait ou paysage sans reliure centrale)
				int x = left;
				int y = top;
				int w = getWidth() - left - right;
				int h = getHeight() - top - bottom;

				if (w > 0 && h > 0) {
					g2d.drawRect(x, y, w - 1, h - 1);
				}
			}

			g2d.dispose();
		}
	}

	/**
	 * get current Print object
	 *
	 * @return
	 */
	public Print getPrint() {
		return print;
	}

	/**
	 * select the given GridCell
	 *
	 * @param cell
	 */
	public void gridCellSelect(GridCell cell) {
		gridCellSelected = cell;
	}

	/**
	 * unselect the current selected GridCell
	 */
	public void gridCellUnselect() {
		gridCellSelected = null;
	}

	/**
	 * get the selected GridCell
	 *
	 * @return
	 */
	public GridCell gridCellSelectedGet() {
		return gridCellSelected;
	}

	/**
	 * get the cols number
	 *
	 * @return
	 */
	public int colsGet() {
		return cols;
	}

	/**
	 * get the rows number
	 *
	 * @return
	 */
	public int rowsGet() {
		return rows;
	}

	/**
	 * place a given cell in the grid
	 *
	 * @param targetCell
	 */
	public void placeCell(XmlPrintCell targetCell) {
		XmlPrintCell pendingCell = print.pendingCellToPlaceGet();
		if (pendingCell == null) {
			return;
		}
		if (pendingCell.isText() && pendingCell.pageGet() > 0) {
			print.pendingCellClear();
			return;
		}
		boolean targetIsReal = (targetCell.isPhoto() || targetCell.isText());
		if (pendingCell.isText() && targetIsReal) {
			return;
		}

		int currentPageNum = print.gridCurrentPageGet();
		if (pendingCell.isPhoto() && targetCell.isPhoto() && pendingCell.pageGet() > 0) {
			int posPending = pendingCell.cellNumGet();
			int posTarget = targetCell.cellNumGet();
			pendingCell.cellNumSet(posTarget);
			targetCell.cellNumSet(posPending);
			if (pendingCell.spanHorizontalGet() != targetCell.spanHorizontalGet()
					|| pendingCell.spanVerticalGet() != targetCell.spanVerticalGet()) {
				pendingCell.spanHorizontalSet(1);
				pendingCell.spanVerticalSet(1);
				targetCell.spanHorizontalSet(1);
				targetCell.spanVerticalSet(1);
			}
			setModified();
			print.pendingCellClear();
			refresh();
			return;
		}
		if (!targetIsReal || (pendingCell.isPhoto() && targetCell.isPhoto())) {
			int indexPage = currentPageNum - 1;
			if (targetIsReal && indexPage >= 0 && indexPage < print.printPagesGet().size()) {
				XmlPrintPage page = print.printPagesGet().get(indexPage);
				XmlPrintCell cellToRemove = null;
				for (XmlPrintCell c : page.cellsGet()) {
					if (c.cellNumGet() == targetCell.cellNumGet()) {
						cellToRemove = c;
						break;
					}
				}
				if (cellToRemove != null) {
					page.cellsGet().remove(cellToRemove);
					cellToRemove.pageSet(0);
					cellToRemove.cellNumSet(0);
				}
			}
			int page = print.gridCurrentPageGet();
			String pos = targetCell.posGet();
			print.xmlGet().printGet().updateCell(pendingCell, page, pos);
			setModified();
			if (print.poolGet() != null) {
				print.poolGet().poolCellUnselect();
			}
			print.pendingCellClear();
		}
	}

	/**
	 * set modification action, force save
	 */
	public void setModified() {
		print.actionSave();
	}

	/**
	 * set orientation
	 *
	 * @param rows
	 * @param cols
	 */
	public void orientationSet(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		String orientation = (rows >= cols) ? PORTRAIT : LANDSCAPE;
		setDim(print.xmlPrintGet().formatGet(), orientation);
	}

	/**
	 * check if given cell may be horizontaly extended
	 *
	 * @param item
	 * @return
	 */
	public boolean isAllowedSpanH(XmlPrintCell item) {
		if (item == null || item.pageGet() <= 0) {
			return false;
		}
		int currentSpanH = item.spanHorizontalGet() > 0 ? item.spanHorizontalGet() : 1;
		int currentSpanV = item.spanVerticalGet() > 0 ? item.spanVerticalGet() : 1;
		int cellNum = item.cellNumGet();
		int r = (cellNum - 1) / cols;
		int c = (cellNum - 1) % cols;
		int targetSpanH = currentSpanH + 1;
		if (c + targetSpanH > cols) {
			return false;
		}
		int targetCol = c + currentSpanH;
		for (XmlPrintCell other : print.getCells()) {
			if (other.pageGet() == item.pageGet() && other.cellNumGet() != item.cellNumGet()) {
				int otherCellNum = other.cellNumGet();
				if (otherCellNum < 1 || otherCellNum > (rows * cols)) {
					continue;
				}
				int otherR = (otherCellNum - 1) / cols;
				int otherC = (otherCellNum - 1) % cols;
				int otherSpanH = other.spanHorizontalGet() > 0 ? other.spanHorizontalGet() : 1;
				int otherSpanV = other.spanVerticalGet() > 0 ? other.spanVerticalGet() : 1;
				boolean overlapH = (targetCol >= otherC) && (targetCol < otherC + otherSpanH);
				boolean overlapV = (r < otherR + otherSpanV) && (r + currentSpanV > otherR);
				if (overlapH && overlapV) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * check if given cell may be verticaly extended
	 *
	 * @param item
	 * @return
	 */
	public boolean isAllowedSpanV(XmlPrintCell item) {
		if (item == null || item.pageGet() <= 0) {
			return false;
		}
		int currentSpanH = item.spanHorizontalGet() > 0 ? item.spanHorizontalGet() : 1;
		int currentSpanV = item.spanVerticalGet() > 0 ? item.spanVerticalGet() : 1;
		int cellNum = item.cellNumGet();
		int r = (cellNum - 1) / cols;
		int c = (cellNum - 1) % cols;
		int targetSpanV = currentSpanV + 1;
		if (r + targetSpanV > rows) {
			return false;
		}
		int targetRow = r + currentSpanV;
		for (XmlPrintCell other : print.getCells()) {
			if (other.pageGet() == item.pageGet() && other.cellNumGet() != item.cellNumGet()) {
				int otherCellNum = other.cellNumGet();
				if (otherCellNum < 1 || otherCellNum > (rows * cols)) {
					continue;
				}
				int otherR = (otherCellNum - 1) / cols;
				int otherC = (otherCellNum - 1) % cols;
				int otherSpanH = other.spanHorizontalGet() > 0 ? other.spanHorizontalGet() : 1;
				int otherSpanV = other.spanVerticalGet() > 0 ? other.spanVerticalGet() : 1;
				boolean overlapH = (c < otherC + otherSpanH) && (c + currentSpanH > otherC);
				boolean overlapV = (targetRow >= otherR) && (targetRow < otherR + otherSpanV);
				if (overlapH && overlapV) {
					return false;
				}
			}
		}
		return true;
	}

	public void setSpanH(XmlPrintCell item, int value) {
		if (item == null) {
			return;
		}
		int currentSpan = item.spanHorizontalGet() > 0 ? item.spanHorizontalGet() : 1;
		int newSpan = currentSpan + value;
		if (newSpan >= 1 && newSpan <= cols) {
			item.spanHorizontalSet(newSpan);
			setModified();
			refresh();
		}
	}

	public void setSpanV(XmlPrintCell item, int value) {
		if (item == null) {
			return;
		}
		int currentSpan = item.spanVerticalGet() > 0 ? item.spanVerticalGet() : 1;
		int newSpan = currentSpan + value;
		if (newSpan >= 1 && newSpan <= rows) {
			item.spanVerticalSet(newSpan);
			setModified();
			refresh();
		}
	}

	public void zoomSet(XmlPrintCell item, int value) {
		item.zoomSet(value);
		setModified();
		refresh();
	}

	void offsetSet(XmlPrintCell cell) {
		if (cell.isPhoto()) {
			if (cell.zoomGet() != 1) {
				//enter dragn'drop if zoom is none or
			}
		}
	}

	void rotateSet(XmlPrintCell cell, int rotateValue) {
	}
}
