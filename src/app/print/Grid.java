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
import app.xml.XmlPrintPage;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * JPanel class for the grid (Simule une page papier A4 avec marges)
 *
 * @author favdb
 */
public class Grid extends JPanel {

	private static final String TT = "Grid.";

	private final Print print;
	private int rows;
	private int cols;
	private int imgWidth;
	private int imgHeight;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Grid(Print print) {
		this.print = print;
		initialize();
	}

	/**
	 * initialize
	 */
	public void initialize() {
		String sizeRaw = print.xmlPrintGet().sizeGet();
		rows = 5;
		cols = 3;
		if (!sizeRaw.isEmpty()) {
			String[] tokens = sizeRaw.split(",");
			if (tokens.length == 2) {
				rows = Integer.parseInt(tokens[0].trim());
				cols = Integer.parseInt(tokens[1].trim());
			}
		}

		// 1. Configuration des dimensions et de l'aspect Page Blanche Papier A4
		Dimension dim = new Dimension(595, 842);
		this.setPreferredSize(dim);
		this.setMinimumSize(dim);
		this.setMaximumSize(dim);
		this.setOpaque(true);
		this.setBackground(Color.WHITE);

		// 2. Marges de 1.5 cm tout autour (~56 pixels) + fine bordure de feuille
		this.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
				BorderFactory.createEmptyBorder(56, 56, 56, 56)
		));

		StringBuilder rowC = new StringBuilder();
		for (int i = 0; i < rows; i++) {
			rowC.append("[grow, fill]");
		}
		StringBuilder colC = new StringBuilder();
		for (int j = 0; j < cols; j++) {
			colC.append("[grow, fill]");
		}
		this.setLayout(new MigLayout(MIG.get(MIG.FILL, "gap 2, ins 0"), colC.toString(), rowC.toString()));
	}

	/**
	 * set modified
	 */
	public void setModified() {
		print.actionSave();
	}

	public Dimension imgGetSize() {
		return new Dimension(imgWidth, imgHeight);
	}

	public void orientationSet(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
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
		Dimension preferredDim = this.getPreferredSize();
		imgWidth = preferredDim.width / cols;
		imgHeight = preferredDim.height / rows;

		int indexPage = print.gridCurrentPageGet() - 1;
		if (indexPage < 0 || indexPage >= print.printPagesGet().size()) {
			this.revalidate();
			this.repaint();
			return;
		}

		XmlPrintPage page = print.printPagesGet().get(indexPage);
		boolean[][] occupied = new boolean[rows][cols];

		// Étape A : Placer les cellules réelles existantes
		for (PrintItem cell : page.cellsGet()) {
			int cellNum = cell.cellNumGet();
			if (cellNum < 1 || cellNum > (rows * cols)) {
				continue;
			}
			int r = (cellNum - 1) / cols;
			int c = (cellNum - 1) % cols;

			int sH = cell.spanHorizontalGet();
			int sV = cell.spanVerticalGet();
			if (c + sH > cols) {
				sH = cols - c;
			}
			if (r + sV > rows) {
				sV = rows - r;
			}
			for (int i = 0; i < sV; i++) {
				for (int j = 0; j < sH; j++) {
					occupied[r + i][c + j] = true;
				}
			}
			GridCell img = new GridCell(this, cell);
			String constraint = String.format("cell %d %d %d %d", c, r, sH, sV);
			this.add(img, constraint);
		}

		// Étape B : Remplir les vides avec des GridCell fantômes numérotés
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (!occupied[r][c]) {
					int targetCellNum = (r * cols) + c + 1;

					// On utilise le constructeur vide
					PrintItem emptyCell = new PrintItem(c, targetCellNum);

					GridCell emptyImg = new GridCell(this, emptyCell);
					String constraint = String.format("cell %d %d 1 1", c, r);
					this.add(emptyImg, constraint);
					occupied[r][c] = true;
				}
			}
		}
		this.revalidate();
		this.repaint();
	}

	public Print getPrint() {
		return print;
	}
}
