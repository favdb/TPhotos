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

import api.mig.MIG;
import api.mig.swing.MigLayout;
import app.App;
import static app.print.Print.*;
import app.xml.XmlPrintPage;
import java.awt.Dimension;
import javax.swing.JPanel;
import tools.LaF;

/**
 * JPanel class for the grid (Simule une page papier A4 avec marges)
 *
 * @author favdb
 */
public class Grid extends JPanel {

	private static final String TT = "Grid.";

	private final Print print;
	public Dimension cellConf, cellDim, pageDim;
	private int rows, cols;
	private int imgWidth, imgHeight;
	private GridCell gridCellSelected;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Grid(Print print) {
		this.print = print;
		initialize();
	}

	public void setDim(String format, String orientation) {
		int pH = (LaF.getScreenHeight() - (App.fontGet().getSize() * 12) - 5);
		int pW = (LaF.getScreenWidth() - 256) - 5;
		boolean isPortrait = PORTRAIT.equalsIgnoreCase(orientation);
		rows = isPortrait ? 5 : 3;
		cols = isPortrait ? 3 : 5;
		cellConf = new Dimension(cols, rows);
		int size = Math.min(pW / cols, pH / rows);
		size = Math.min(pW, pH) / 5;
		cellDim = new Dimension(size, size);
		imgWidth = size;
		imgHeight = size;
	}

	/**
	 * initialize
	 */
	public void initialize() {
		setDim("A4", print.paperOrientationGet());

		// Suppression de l'aspect "Page blanche papier" (Border, Background opaque)
		// Le JPanel s'adapte naturellement à ses enfants ou au conteneur parent
		StringBuilder rowC = new StringBuilder();
		for (int i = 0; i < rows; i++) {
			rowC.append("[]");
		}
		StringBuilder colC = new StringBuilder();
		for (int j = 0; j < cols; j++) {
			colC.append("[]");
		}
		this.setLayout(new MigLayout(MIG.get("gap 0, ins 0"), colC.toString(), rowC.toString()));
	}

	/**
	 * Gère les opérations de placement et applique les contraintes métiers.
	 *
	 * @param targetCell La cellule de la grille (réelle ou fantôme) ciblée.
	 */
	public void placeCell(PrintCell targetCell) {
		//LOG.trace(TT + "placeCell(" + targetCell.toString() + ")");
		PrintCell pendingCell = print.pendingCellToPlaceGet();
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
			boolean comingFromPool = (pendingCell.pageGet() == 0);
			int indexPage = currentPageNum - 1;
			if (targetIsReal && indexPage >= 0 && indexPage < print.printPagesGet().size()) {
				XmlPrintPage page = print.printPagesGet().get(indexPage);
				PrintCell cellToRemove = null;
				for (PrintCell c : page.cellsGet()) {
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
			if (comingFromPool) {
				print.xmlGet().printGet().updateCell(pendingCell, page, pos);
			} else {
				print.xmlGet().printGet().updateCell(pendingCell, page, pos);
			}
			setModified();
			if (print.poolGet() != null) {
				print.poolGet().poolCellUnselect();
			}
			print.pendingCellClear();
			print.refresh();
		}
	}

	/**
	 * set modified
	 */
	public void setModified() {
		//LOG.trace(TT + "setModified()");
		print.actionSave();
	}

	public Dimension imgGetSize() {
		return new Dimension(imgWidth, imgHeight);
	}

	public void orientationSet(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		String orientation = (rows >= cols) ? PORTRAIT : LANDSCAPE;
		setDim(print.xmlPrintGet().formatGet(), orientation);
	}

	public int colsGet() {
		return cols;
	}

	public int rowsGet() {
		return rows;
	}

	/**
	 * refresh the JPanel as a clean table grid layout with empty cells placeholder
	 */
	public void refresh() {
		this.removeAll();

		// 1. Re-mise à jour dynamique des contraintes MigLayout en fonction de rows / cols actuels
		StringBuilder rowC = new StringBuilder();
		for (int i = 0; i < rows; i++) {
			rowC.append("[]");
		}
		StringBuilder colC = new StringBuilder();
		for (int j = 0; j < cols; j++) {
			colC.append("[]");
		}
		this.setLayout(new MigLayout(MIG.get("gap 2, ins 0"), colC.toString(), rowC.toString()));

		imgWidth = cellDim.width;
		imgHeight = cellDim.height;

		int currentPage = print.gridCurrentPageGet();
		boolean[][] occupied = new boolean[rows][cols];

		// 2. Recherche et placement des cellules RÉELLES de la page courante depuis la liste globale
		if (print.getCells() != null) {
			for (PrintCell cell : print.getCells()) {
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

					// Marquer les cases occupées
					for (int i = 0; i < sV; i++) {
						for (int j = 0; j < sH; j++) {
							if (r + i < rows && c + j < cols) {
								occupied[r + i][c + j] = true;
							}
						}
					}

					GridCell img = new GridCell(this, cell);
					String constraint = String.format("top, cell %d %d %d %d", c, r, sH, sV);
					this.add(img, constraint);
				}
			}
		}

		// 3. Remplissage des cases restées vides (Fantômes)
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (!occupied[r][c]) {
					int targetCellNum = (r * cols) + c + 1;

					PrintCell emptyCell = new PrintCell();
					emptyCell.cellNumSet(targetCellNum);
					emptyCell.pageSet(currentPage);
					emptyCell.posSet((r + 1) + "," + (c + 1));

					GridCell emptyImg = new GridCell(this, emptyCell);
					String constraint = String.format("cell %d %d 1 1", c, r);
					this.add(emptyImg, constraint);
					occupied[r][c] = true;
				}
			}
		}

		this.revalidate();
		this.repaint();
		if (this.getParent() != null) {
			this.getParent().revalidate();
			this.getParent().repaint();
		}
	}

	public Print getPrint() {
		return print;
	}

	public void gridCellSelect(GridCell cell) {
		gridCellSelected = cell;
	}

	public void gridCellUnselect() {
		gridCellSelected = null;
	}

	public GridCell gridCellSelectedGet() {
		return gridCellSelected;
	}

	/**
	 * Vérifie si la cellule peut augmenter son span horizontal (+1)
	 *
	 * @param item
	 * @return
	 */
	public boolean isAllowedSpanH(PrintCell item) {
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
		for (PrintCell other : print.getCells()) {
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
	 * Vérifie si la cellule peut augmenter son span vertical (+1)
	 *
	 * @param item
	 * @return
	 */
	public boolean isAllowedSpanV(PrintCell item) {
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
		for (PrintCell other : print.getCells()) {
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

	/**
	 * Update the horizontal span for the given cell (+1 ou -1).
	 *
	 * @param item The cell to be modifiied
	 * @param value The variation (+1 or -1)
	 */
	public void setSpanH(PrintCell item, int value) {
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

	/**
	 * Update the vertical span of the given cell (+1 or -1).
	 *
	 * @param item The cell to be modifiied
	 * @param value The variation (+1 or -1)
	 */
	public void setSpanV(PrintCell item, int value) {
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

}
