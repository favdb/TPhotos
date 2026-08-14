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
package app.ui.print;

import app.tools.ImageUtil;
import app.xml.XmlPrintCell;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.List;
import javax.swing.ImageIcon;

/**
 * Manages pages layout and rendering for hardware printing.
 *
 * @author favdb
 */
public class GridPrint implements Printable, Pageable {

	private static final String TT = "GridPrint.";

	public static final double MARGIN = 10.0,
			MARGIN_PT = (MARGIN * 72.0) / 25.4,
			GAP_MM = 1.0,
			GAP_PT = (GAP_MM * 72.0) / 25.4;
	private final Print print;

	public GridPrint(Print print) {
		this.print = print;
	}

	@Override
	public int getNumberOfPages() {
		return print.printPagesGet().size();
	}

	@Override
	public PageFormat getPageFormat(int pi) throws IndexOutOfBoundsException {
		PageFormat pf = new PageFormat();
		Paper paper = new Paper();
		double width = 595.27, height = 841.89;
		if ("A3".equalsIgnoreCase(print.paperFormatGet())) {
			width = 841.89;
			height = 1190.55;
		}
		if (Print.LANDSCAPE.equalsIgnoreCase(print.paperOrientationGet())) {
			pf.setOrientation(PageFormat.LANDSCAPE);
			paper.setSize(height, width);
			paper.setImageableArea(MARGIN_PT,
					MARGIN_PT, height - (2 * MARGIN_PT),
					width - (2 * MARGIN_PT));
		} else {
			pf.setOrientation(PageFormat.PORTRAIT);
			paper.setSize(width, height);
			paper.setImageableArea(MARGIN_PT,
					MARGIN_PT,
					width - (2 * MARGIN_PT),
					height - (2 * MARGIN_PT));
		}
		pf.setPaper(paper);
		return pf;
	}

	@Override
	public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
		return this;
	}

	@Override
	public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
		if (pi >= getNumberOfPages()) {
			return NO_SUCH_PAGE;
		}
		Graphics2D g2d = (Graphics2D) g;
		int rows = print.gridGet().rowsGet(),
				cols = print.gridGet().colsGet(),
				pageNum = pi + 1,
				totalPages = getNumberOfPages();
		double pW = pf.getWidth(),
				pH = pf.getHeight(),
				uMleft = pf.getImageableX(),
				uMtop = pf.getImageableY(),
				uMright = pW - (uMleft + pf.getImageableWidth()),
				uMbottom = pH - (uMtop + pf.getImageableHeight()),
				mLeft = Math.max(uMleft, MARGIN_PT),
				mTop = Math.max(uMtop, MARGIN_PT),
				mRight = Math.max(uMright, MARGIN_PT),
				mBottom = Math.max(uMbottom, MARGIN_PT),
				availW = pW - (mLeft + mRight),
				availH = pH - (mTop + mBottom),
				totalGapsW = (cols - 1) * GAP_PT,
				totalGapsH = (rows - 1) * GAP_PT,
				cellW = (availW - totalGapsW) / cols,
				cellH = (availH - totalGapsH) / rows;
		List<XmlPrintCell> cells = print.getCells();
		for (XmlPrintCell cell : cells) {
			if (cell.pageGet() == pageNum) {
				int cellNum = cell.cellNumGet();
				if (cellNum < 1 || cellNum > (rows * cols)) {
					continue;
				}
				int r = (cellNum - 1) / cols,
						c = (cellNum - 1) % cols,
						sH = cell.spanHorizontalGet() > 0 ? cell.spanHorizontalGet() : 1,
						sV = cell.spanVerticalGet() > 0 ? cell.spanVerticalGet() : 1,
						x = (int) Math.round(mLeft + c * (cellW + GAP_PT)),
						y = (int) Math.round(mTop + r * (cellH + GAP_PT)),
						w = (int) Math.round(sH * cellW + (sH - 1) * GAP_PT),
						h = (int) Math.round(sV * cellH + (sV - 1) * GAP_PT);

				if (cell.isPhoto() && cell.photoFileGet() != null && cell.photoFileGet().exists()) {
					ImageIcon icon = ImageUtil.getImage(cell.photoFileGet(), new Dimension(w, h), cell.zoomGet());
					if (icon != null && icon.getImage() != null) {
						int imgW = icon.getIconWidth();
						int imgH = icon.getIconHeight();
						int drawX = x + (w - imgW) / 2;
						int drawY = y + (h - imgH) / 2;
						g2d.drawImage(icon.getImage(), drawX, drawY, null);
					}
				} else if (cell.isText()) {
					String textContent = (cell.textGet() != null) ? cell.textGet() : "";
					String html = "<html><body>" + textContent + "</body></html>";
					ImageIcon icon = ImageUtil.createTextImage(html, new Dimension(w, h));
					if (icon != null && icon.getImage() != null) {
						g2d.drawImage(icon.getImage(), x, y, null);
					}
				}
			}
		}

		// 3. Dessin du numéro de page (p/T) APRES les cellules dans un contexte graphique isolé
		drawPageNumber(g2d, pageNum, totalPages, pW, mTop, mRight);

		return PAGE_EXISTS;
	}

	/**
	 * Draw page numbere (p/T) into the top margin on right.
	 */
	private void drawPageNumber(Graphics2D g2d, int pageNum, int totalPages,
			double pageWidth, double marginTop, double marginRight) {
		Graphics2D g2dText = (Graphics2D) g2d.create();
		try {
			g2dText.setClip(null);
			g2dText.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2dText.setFont(new Font("SansSerif", Font.PLAIN, 9));
			g2dText.setColor(Color.BLACK);
			String text = pageNum + "/" + totalPages;
			FontMetrics fm = g2dText.getFontMetrics();
			int textWidth = fm.stringWidth(text);
			int x = (int) Math.round(pageWidth - marginRight - textWidth);
			int y = (int) Math.round(marginTop - 2.0);
			g2dText.drawString(text, x, y);
		} finally {
			g2dText.dispose();
		}
	}

}
