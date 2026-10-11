package app.print;

import static app.print.Print.PORTRAIT;
import app.tools.LOG;
import app.xml.XmlPrintCell;
import app.xml.XmlPrintPage;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
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

	private static String buildCSS(Print print) {
		String format = print.paperFormatGet();
		String orientation = print.paperOrientationGet();
		int pageWidth = 210, pageHeight = 297;
		boolean isPortrait = PORTRAIT.equalsIgnoreCase(orientation);
		if ("A4".equalsIgnoreCase(format)) {
			pageWidth = isPortrait ? 210 : 297;
			pageHeight = isPortrait ? 297 : 210;
		} else if ("A3".equalsIgnoreCase(format)) {
			pageWidth = isPortrait ? 297 : 420;
			pageHeight = isPortrait ? 420 : 297;
		}

		int[] margins = print.xmlPrintGet().marginsIntGet();
		int top = (margins != null && margins.length > 0) ? margins[0] : 0;
		int left = (margins != null && margins.length > 1) ? margins[1] : 0;
		int right = (margins != null && margins.length > 2) ? margins[2] : 0;
		int bottom = (margins != null && margins.length > 3) ? margins[3] : 0;

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
				+ "    padding: " + top + "mm " + right + "mm " + bottom + "mm " + left + "mm;\n"
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
				+ "        margin: 0; border: none; box-shadow: none;\n"
				+ "    }\n"
				+ "    .cell {\n"
				+ "        border: none;\n"
				+ "    }\n"
				+ "}";
	}

	private static String buildOrientation(Print print) {
		String orientation = print.paperOrientationGet();
		// Convertit le libellé français en mot-clé CSS standard (landscape / portrait)
		String cssOrientation = "landscape";
		if (orientation != null && (orientation.equalsIgnoreCase("portrait")
				|| orientation.contains("portrait")
				|| orientation.contains("Port"))) {
			cssOrientation = "portrait";
		}

		return "@page { size: " + print.paperFormatGet() + " " + cssOrientation + "; margin: 0;}\n"
				+ buildCSS(print);
	}

	private static String buildPage(XmlPrintPage page, Print print, int pageNum, int totalPages) {
		StringBuilder b = new StringBuilder();
		int rows = print.gridGet().rowsGet();
		int cols = print.gridGet().colsGet();
		boolean pagenum = print.pagenumGet();
		boolean isLandscape = !PORTRAIT.equalsIgnoreCase(print.paperOrientationGet());
		boolean hasCentralBinding = print.xmlPrintGet().centralGet();
		int gap = print.xmlPrintGet().gapGet() ? 12 : 0; // Marge de photo (gap en pixels ou mm selon rendu)

		b.append("<div class=\"page\">\n");
		if (pagenum) {
			b.append("  <div class=\"page-number\">")
					.append(pageNum).append("/").append(totalPages).append("</div>\n");
		}

		// Construction des colonnes Grid CSS en tenant compte de la reliure centrale si paysage
		String gridTemplateCols;
		int halfCols = cols / 2;
		if (isLandscape && hasCentralBinding && cols >= 2) {
			int[] margins = print.xmlPrintGet().marginsIntGet();
			int centralGap = (margins != null && margins.length > 2) ? (margins[1] + margins[2]) : 20;

			StringBuilder leftCols = new StringBuilder();
			for (int j = 0; j < halfCols; j++) {
				leftCols.append("1fr ");
			}
			StringBuilder rightCols = new StringBuilder();
			for (int j = halfCols; j < cols; j++) {
				rightCols.append("1fr ");
			}

			gridTemplateCols = leftCols.toString().trim() + " " + centralGap + "mm " + rightCols.toString().trim();
		} else {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < cols; j++) {
				sb.append("1fr ");
			}
			gridTemplateCols = sb.toString().trim();
		}

		StringBuilder gridRows = new StringBuilder();
		for (int i = 0; i < rows; i++) {
			gridRows.append("1fr ");
		}

		b.append("  <div class=\"grid-container\" style=\"")
				.append("grid-template-rows: ").append(gridRows.toString().trim()).append("; ")
				.append("grid-template-columns: ").append(gridTemplateCols).append("; ")
				.append("gap: ").append(gap).append("px;\">\n");

		for (XmlPrintCell cell : page.cellsGet()) {
			int indexZeroBased = cell.cellIdGet() - 1;
			int line = (indexZeroBased / cols) + 1;
			int col = (indexZeroBased % cols) + 1;

			// Décalage de la colonne CSS si on dépasse la reliure centrale en mode paysage
			int cssCol = col;
			if (isLandscape && hasCentralBinding && cols >= 2 && (col - 1) >= halfCols) {
				cssCol = col + 1; // +1 pour sauter la colonne centrale fictive
			}

			String gridStyle = String.format("grid-row: %d / span %d;"
					+ " grid-column: %d / span %d;",
					line, cell.spanVerticalGet(), cssCol, cell.spanHorizontalGet());

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

				if (realFile != null && realFile.exists()) {
					b.append("      <img src=\"")
							.append(realFile.toURI().toString())
							.append("\" style=\"object-fit: ")
							.append(objectFit)
							.append(";\" alt=\"\">\n");
				} else {
					b.append("      <span style=\"color:#c00;font-size:9px;\">")
							.append("Photo introuvable (#").append(cell.photoIdGet()).append(")")
							.append("</span>\n");
				}
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

	public static void generateHTML(Print print, File outfile, boolean toOpen) {
		try {
			StringBuilder b = new StringBuilder();
			String css = buildOrientation(print);
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
				b.append(buildPage(pages.get(i), print, i + 1, totalPages));
			}

			b.append("</body>\n</html>");
			Files.write(outfile.toPath(), b.toString().getBytes(StandardCharsets.UTF_8));
			if (toOpen && Desktop.isDesktopSupported()
					&& Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				Desktop.getDesktop().browse(outfile.toURI());
			}
		} catch (IOException e) {
			LOG.err("generateHTML error", e);
		}
	}
}
