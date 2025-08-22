/*
 * Copyright (C) 2024 favdb
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
package app.export;

import app.album.AlbumItem;
import java.io.File;
import java.util.List;
import tools.file.FileUtil;

/**
 *
 * @author favdb
 */
public class ExportHTML {

	private static final String TT = "ExportHTML.";
	private final Export export;
	private StringBuilder html;
	private final File dir;
	private List<AlbumItem> items;
	private final String title;

	public ExportHTML(Export export, File dir) {
		this.export = export;
		this.dir = dir;
		this.title = export.getMainFrame().albumTitleGet();
	}

	public void begin(List<AlbumItem> items) {
		this.items = items;
		createScript(items);
		createStyle();
		end();
	}

	private void createScript(List<AlbumItem> items) {
		html = new StringBuilder();
		StringBuilder files = new StringBuilder();
		StringBuilder texts = new StringBuilder();
		for (AlbumItem item : items) {
			if (!files.toString().isEmpty()) {
				files.append(", \n");
				texts.append(", \n");
			}
			files.append("\"").append(item.file.getName()).append("\"");
			String ftxt = FileUtil.changeExt(item.file.getName(), "txt");
			texts.append("\"").append(item.text).append("\"");
		}
		html.append("let slideIndex = 0;\n")
				.append("const imagePaths = [").append(files.toString()).append("];\n")
				.append("const textPaths = [").append(texts.toString()).append("];\n")
				.append("window.onload = function() { showSlide(slideIndex);\n};\n")
				.append("function changeSlide(n) { slideIndex += n;\n")
				.append("    if (slideIndex >= imagePaths.length) { slideIndex = 0;}\n")
				.append("    else if (slideIndex < 0) { slideIndex = imagePaths.length - 1;}\n")
				.append("    showSlide(slideIndex);\n")
				.append("}\n")
				.append("function showSlide(index) {\n"
						+ "    const imageElement = document.getElementById(\"slideshow-image\");\n"
						+ "    imageElement.src = imagePaths[index];\n"
						+ "    const textElement = document.getElementById(\"slideshow-text\");\n"
						+ "    textElement.textContent=textPaths[index];\n"
						+ "}\n");
		FileUtil.fileWriteString(new File(dir, "script.js"), html.toString());
	}

	private void createStyle() {
		html = new StringBuilder();
		html.append("* {\n"
				+ "    box-sizing: border-box;\n"
				+ "    margin: 0; padding: 0;\n"
				+ "}\n"
				+ "body {\n"
				+ "    font-family: sans-serif;\n"
				+ "    background-color: black;\n"
				+ "    height: 100vh;\n"
				+ "    display: flex;\n"
				+ "    justify-content: center; align-items: center;\n"
				+ "    overflow: hidden;\n"
				+ "}\n"
				+ ".slideshow-container {\n"
				+ "    position: relative;\n"
				+ "    width: 100%; height: 100vh;\n"
				+ "    display: flex;\n"
				+ "    justify-content: center;\n"
				+ "    align-items: center;\n"
				+ "    overflow: hidden;\n"
				+ "}\n"
				+ ".slideshow {\n"
				+ "    position: relative;\n"
				+ "    display: flex;\n"
				+ "    justify-content: center; align-items: center;\n"
				+ "    max-width: 100%; max-height: 100%;\n"
				+ "}\n"
				+ "#slideshow-image {\n"
				+ "    max-width: 100%; max-height: 100vh;\n"
				+ "    width: auto; height: auto;\n"
				+ "    object-fit: contain;\n"
				+ "}\n"
				+ ".slideshow-text {\n"
				+ "    position: absolute;\n"
				+ "    bottom: 8px;\n"
				+ "    background-color: rgba(0, 0, 0, 0.5); color: #fff;\n"
				+ "    width: 100%;\n"
				+ "    text-align: center;\n"
				+ "    padding: 10px 0;\n"
				+ "    font-size: 16px;\n"
				+ "}\n"
				+ ".prev, .next {\n"
				+ "    cursor: pointer;\n"
				+ "    position: absolute;\n"
				+ "    top: 50%;\n"
				+ "    width: auto;\n"
				+ "    padding: 16px;\n"
				+ "    color: white; background-color: black;\n"
				+ "    font-weight: bold; font-size: 18px;\n"
				+ "    transition: 0.6s ease;\n"
				+ "    border-radius: 0 3px 3px 0;\n"
				+ "    user-select: none;\n"
				+ "}\n"
				+ ".prev { left: 0;}\n"
				+ ".next { right: 0;}\n"
				+ ".prev:hover, .next:hover {\n"
				+ "    color:black; background-color: white;\n"
				+ "}");
		FileUtil.fileWriteString(new File(dir, "styles.css"), html.toString());
	}

	public void end() {
		html = new StringBuilder();
		html.append("<!DOCTYPE html>\n"
				+ "<html lang=\"fr\">\n"
				+ "<head>\n"
				+ "    <meta charset=\"UTF-8\">\n"
				+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
				+ "    <title>" + title + "</title>\n"
				+ "    <link rel=\"stylesheet\" href=\"styles.css\">\n"
				+ "</head>\n"
				+ "<body>\n"
				+ "    <div class=\"slideshow-container\">\n"
				+ "        <div class=\"slideshow\">\n"
				+ "            <img id=\"slideshow-image\" src=\"\" alt=\"Image\" />\n"
				+ "            <div id=\"slideshow-text\" class=\"slideshow-text\"></div>\n"
				+ "        </div>\n"
				+ "        <a class=\"prev\" onclick=\"changeSlide(-1)\">&#10094;</a>\n"
				+ "        <a class=\"next\" onclick=\"changeSlide(1)\">&#10095;</a>\n"
				+ "    </div>\n"
				+ "    <script src=\"script.js\"></script>\n"
				+ "</body>\n"
				+ "</html>");
		FileUtil.fileWriteString(new File(dir, "Album.html"), html.toString());
	}

}
