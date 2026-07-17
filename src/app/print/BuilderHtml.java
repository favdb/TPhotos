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

import app.xml.Xml;
import app.xml.XmlPrintPage;
import java.awt.Desktop;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import tools.LOG;

/**
 * class for building the result HTML from the PrintXML
 *
 * @author favdb
 */
public class BuilderHtml {

	private static String buildCSS() {
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
				+ "    width: 210mm;\n"
				+ "    height: 297mm;\n"
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
				+ ".grid-container {\n"
				+ "    display: grid;\n"
				+ "    grid-template-columns: repeat(3, 1fr); \n"
				+ "    grid-template-rows: repeat(5, 1fr);    \n"
				+ "    gap: 4mm;\n"
				+ "    width: 100%;\n"
				+ "    height: 100%;\n"
				+ "}\n"
				+ "\n"
				+ ".cell {\n"
				//+ "    border: 1px dashed #ccc;\n"
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
				+ "    object-fit: cover;\n"
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
	 * Build the CSS to inject for A4 orientation.
	 */
	private static String buildCSSOrientation(String format, String orientation) {
		return "@page { size: " + format + " " + orientation + "; margin: 0;}";
	}

	/**
	 * Build a page into the grid.
	 */
	private static String buildPagesHtml(Xml xml) {
		StringBuilder b = new StringBuilder();
		String[] sizeConfig = xml.getPrint().sizeGet().split(",");
		String rowsCount = sizeConfig[0];
		String colsCount = sizeConfig[1];
		int colsCountInt = Integer.parseInt(colsCount);

		for (XmlPrintPage page : xml.getPrint().printPageGetAll()) {
			b.append("<div class=\"page\">\n");
			b.append("  <div class=\"grid-container\" style=\"")
					.append("grid-template-rows: repeat(").append(rowsCount).append(", 1fr); ")
					.append("grid-template-columns: repeat(").append(colsCount).append(", 1fr);\">\n");
			for (PrintItem cell : page.cellsGet()) {
				int indexZeroBased = cell.cellIdGet() - 1;
				int line = (indexZeroBased / colsCountInt) + 1;
				int col = (indexZeroBased % colsCountInt) + 1;
				String gridStyle = String.format("grid-row: %d / span %d; grid-column: %d / span %d;",
						line, cell.spanVerticalGet(), col, cell.spanHorizontalGet());
				if (cell.isPhoto()) {
					b.append("    <div class=\"cell photo\" style=\"").append(gridStyle).append("\">\n");
					String src = xml.getPrint().findPhoto(cell.photoId);
					b.append("      <img src=\"").append(src).append("\" alt=\"\">\n");
					b.append("    </div>\n");
				} else {
					b.append("    <div class=\"cell text\" style=\"").append(gridStyle).append("\">\n");
					b.append("      ").append(cell.textGet()).append("\n");
					b.append("    </div>\n");
				}
			}
			b.append("  </div>\n");
			b.append("</div>\n");
		}
		return b.toString();
	}

	/**
	 * generate the HTML preview photoId from the given Xml
	 *
	 * @param xml: input xml photoId
	 * @param outfile: photoId to build
	 * @param toOpen: true to open the result HTML photoId into the default browser
	 */
	public static void generateHTML(Xml xml, File outfile, boolean toOpen) {
		try {
			StringBuilder b = new StringBuilder();
			String css = buildCSSOrientation("A4", "portrait") + buildCSS();
			//the HTML header with style CSS
			b.append("<!DOCTYPE html>\n")
					.append("<html lang=\"fr\">\n")
					.append("<head>\n")
					.append("    <meta charset=\"UTF-8\">\n")
					.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
					.append("    <link rel=\"stylesheet\" href=\"styles.css\">\n")
					.append("    <style>").append(css).append("</style>\n")
					.append("</head>\n")
					.append("<body>\n");
			b.append(buildPagesHtml(xml));
			b.append("</body>\n</html>");
			Files.write(outfile.toPath(), b.toString().getBytes(StandardCharsets.UTF_8));
			if (toOpen && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				Desktop.getDesktop().browse(outfile.toURI());
			}
		} catch (Exception e) {
			LOG.err("generateHTML error", e);
		}
	}

}
