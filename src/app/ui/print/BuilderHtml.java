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

import app.tools.LOG;
import static app.ui.print.Print.PORTRAIT;
import app.xml.XmlPrintCell;
import app.xml.XmlPrintPage;
import java.awt.Desktop;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * class for building the result HTML from the PrintXML
 *
 * @author favdb
 */
public class BuilderHtml {

	private static final String TT = "BuilderHtml.";

	private static String buildCSS(String format, String orientation) {
		int pageWidth = 210, pageHeight = 297;
		boolean isPortrait = PORTRAIT.equalsIgnoreCase(orientation);
		if ("A4".equalsIgnoreCase(format)) {
			pageWidth = isPortrait ? 210 : 297;
			pageHeight = isPortrait ? 297 : 210;
		} else if ("A3".equalsIgnoreCase(format)) {
			pageWidth = isPortrait ? 297 : 420;
			pageHeight = isPortrait ? 420 : 297;
		}
		return "html, body {\n"
				+ "    margin: 0;\n"
				+ "    padding: 0;\n"
				+ "    background-color: #FAFAFA;\n"
				+ "    font-family: Arial, sans-serif;\n"
				+ "    -webkit-print-color-adjust: exact;\n"
				+ "    print-color-adjust: exact;\n"
				+ "}\n"
				+ "\n"
				+ ".page {\n"
				+ "    width: " + pageWidth + "mm;\n"
				+ "    height: " + pageHeight + "mm;\n"
				+ "    margin: 10mm auto;\n"
				+ "    padding: 10mm;\n"
				+ "    background: white;\n"
				+ "    box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);\n"
				+ "    box-sizing: border-box;\n"
				+ "    page-break-after: always;\n"
				+ "    page-break-inside: avoid;\n"
				+ "    position: relative;\n"
				+ "}\n"
				+ "\n"
				+ ".page-number {\n"
				+ "    position: absolute;\n"
				+ "    top: 3mm;\n"
				+ "    right: 10mm;\n"
				+ "    font-size: 9pt;\n"
				+ "    color: #333333;\n"
				+ "}\n"
				+ "\n"
				+ ".grid-container {\n"
				+ "    display: grid;\n"
				+ "    grid-template-columns: repeat(3, 1fr); \n"
				+ "    grid-template-rows: repeat(5, 1fr);    \n"
				+ "    gap: 1mm;\n"
				+ "    width: 100%;\n"
				+ "    height: 100%;\n"
				+ "}\n"
				+ "\n"
				+ ".cell {\n"
				+ "    overflow: hidden;\n"
				+ "    display: flex;\n"
				+ "    justify-content: center;\n"
				+ "    align-items: center;\n"
				+ "    box-sizing: border-box;\n"
				+ "}\n"
				+ "\n"
				+ ".cell.photo img {\n"
				+ "    width: 100%;\n"
				+ "    height: 100%;\n"
				+ "    display: block;\n"
				+ "}\n"
				+ "\n"
				+ ".cell.text {\n"
				+ "    display: block;\n"
				+ "    padding: 5px;\n"
				+ "    text-align: left;\n"
				+ "    word-wrap: break-word;\n"
				+ "}\n"
				+ "\n"
				+ "@media print {\n"
				+ "    html, body { background-color: white; }\n"
				+ "    .page {\n"
				+ "        margin: 0;  border: none; box-shadow: none;\n"
				+ "    }\n"
				+ "    .cell {\n"
				+ "        border: none;\n"
				+ "    }\n"
				+ "}";
	}

	/**
	 * Build the CSS to inject for format and orientation.
	 */
	private static String builOrientation(String format, String orientation) {
		//LOG.trace(TT + "buildOrientation(" + format + ", " + orientation + ")");
		return "@page { size: " + format + " " + orientation + "; margin: 0;}\n"
				+ buildCSS(format, orientation);
	}

	/**
	 * Build the given page into the grid.
	 */
	private static String buildPage(XmlPrintPage page,
			String rows, String cols, int pageNum, int totalPages) {
		//LOG.trace(TT+"buildPage(page, rows, cols)");
		StringBuilder b = new StringBuilder();
		int colsCountInt = Integer.parseInt(cols);
		b.append("<div class=\"page\">\n");
		b.append("  <div class=\"page-number\">")
				.append(pageNum).append("/").append(totalPages).append("</div>\n");
		b.append("  <div class=\"grid-container\" style=\"")
				.append("grid-template-rows: repeat(")
				.append(rows).append(", 1fr); ")
				.append("grid-template-columns: repeat(")
				.append(cols)
				.append(", 1fr);\">\n");
		for (XmlPrintCell cell : page.cellsGet()) {
			int indexZeroBased = cell.cellIdGet() - 1;
			int line = (indexZeroBased / colsCountInt) + 1;
			int col = (indexZeroBased % colsCountInt) + 1;
			String gridStyle = String.format("grid-row: %d / span %d;"
					+ " grid-column: %d / span %d;",
					line, cell.spanVerticalGet(), col, cell.spanHorizontalGet());
			if (cell.isPhoto()) {
				b.append("    <div class=\"cell photo\" style=\"")
						.append(gridStyle).append("\">\n");
				File realFile = cell.photoFileGet();

				int zoomMode = cell.zoomGet();
				String objectFit = "none";
				if (zoomMode == 1) {
					objectFit = "contain";
				} else if (zoomMode == 2) {
					objectFit = "cover";
				}

				b.append("      <img src=\"")
						.append(realFile.getAbsolutePath())
						.append("\" style=\"object-fit: ")
						.append(objectFit)
						.append(";\" alt=\"\">\n");
				b.append("    </div>\n");
			} else {
				b.append("    <div class=\"cell text\" style=\"")
						.append(gridStyle).append("\">\n");
				b.append("      ").append(cell.textGet()).append("\n");
				b.append("    </div>\n");
			}
		}
		b.append("  </div>\n");
		b.append("</div>\n");
		return b.toString();
	}

	/**
	 * generate the HTML pages for the given Xml
	 *
	 * @param print
	 * @param outfile: HTML file to build
	 * @param toOpen: true to open the HTML result into the default browser
	 */
	public static void generateHTML(Print print, File outfile, boolean toOpen) {
		try {
			String rows = print.gridGet().rowsGet() + "";
			String cols = print.gridGet().colsGet() + "";
			StringBuilder b = new StringBuilder();
			String css = builOrientation(print.paperFormatGet(),
					print.paperOrientationGet());
			b.append("<!DOCTYPE html>\n")
					.append("<html lang=\"fr\">\n")
					.append("<head>\n")
					.append("    <meta charset=\"UTF-8\">\n")
					.append("    <meta name=\"viewport\" "
							+ "content=\"width=device-width, initial-scale=1.0\">\n")
					.append("    <link rel=\"stylesheet\" href=\"styles.css\">\n")
					.append("    <style>").append(css).append("</style>\n")
					.append("</head>\n")
					.append("<body>\n");

			List<XmlPrintPage> pages = print.printPagesGet();
			int totalPages = pages.size();
			for (int i = 0; i < totalPages; i++) {
				b.append(buildPage(pages.get(i), rows, cols, i + 1, totalPages));
			}

			b.append("</body>\n</html>");
			Files.write(outfile.toPath(), b.toString().getBytes(StandardCharsets.UTF_8));
			if (toOpen && Desktop.isDesktopSupported()
					&& Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				Desktop.getDesktop().browse(outfile.toURI());
			}
		} catch (Exception e) {
			LOG.err("generateHTML error", e);
		}
	}

}
