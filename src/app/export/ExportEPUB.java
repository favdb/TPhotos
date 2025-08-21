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
import i18n.I18N;
import java.io.File;
import java.util.List;
import tools.file.FileUtil;
import tools.file.ZipUtil;

/**
 *
 * @author favdb
 */
public class ExportEPUB {

	private static final String TT = "ExportEPUB.";

	private final Export export;
	private final File dir;
	private File OEBPS, CSS, META;
	private List<AlbumItem> items;

	public static void create(Export export, File dir) {
		ExportEPUB epub = new ExportEPUB(export, dir);
		epub.start(export.getItems());
	}
	private final String title;

	public ExportEPUB(Export export, File dir) {
		this.export = export;
		this.dir = new File(dir, "EPUB");
		this.title = export.getMainFrame().albumTitleGet();
	}

	public void start(List<AlbumItem> items) {
		this.items = items;
		CSS = new File(dir, "CSS");
		CSS.mkdirs();
		createCSS();
		META = new File(dir, "META-INF");
		META.mkdirs();
		createMETA_INF();
		createOPF();
		createTocNcx();
		OEBPS = new File(dir, "OEBPS");
		OEBPS.mkdirs();
		createTitlepage();
		createPages();
		File outfile = new File(dir.getParentFile(), "Album.epub");
		ZipUtil.fromDir(dir, outfile, "application/epub+zip");
		FileUtil.dirDelete(dir);
	}

	private void createMETA_INF() {
		StringBuilder buf = new StringBuilder();
		buf.append("<?xml version=\"1.0\"?>\n"
				+ "<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">\n"
				+ "  <rootfiles>\n"
				+ "    <rootfile full-path=\"content.opf\" media-type=\"application/oebps-package+xml\"/>\n"
				+ "  </rootfiles>\n"
				+ "</container>");
		META.mkdirs();
		File containerXML = new File(META, "container.xml");
		FileUtil.fileWriteString(containerXML, buf.toString());
	}

	private void createCSS() {
		StringBuilder buf = new StringBuilder();
		buf.append("* {\n"
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
				+ "}");
		File css = new File(CSS, "styles.css");
		FileUtil.fileWriteString(css, buf.toString());
	}

	private static final String TOC_BEGIN = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
			+ "<ncx version=\"2005-1\" xml:lang=\"en\" xmlns=\"http://www.daisy.org/z3986/2005/ncx/\">\n"
			+ "\n"
			+ "<head>\n"
			+ "    <meta content=\"isbn\" name=\"dtb:uid\"/>\n"
			+ "    <meta content=\"2\" name=\"dtb:depth\"/>\n"
			+ "    <meta content=\"0\" name=\"dtb:totalPageCount\"/>\n"
			+ "    <meta content=\"0\" name=\"dtb:maxPageNumber\"/>\n"
			+ "</head>\n"
			+ "<docTitle>\n"
			+ "    <text>Sample .epub Book</text>\n"
			+ "</docTitle>\n"
			+ "<navMap>\n",
			TOC_NAVPOINT = ""
			+ "    <navPoint id=\"img_%03d\" playOrder=\"%d\">\n"
			+ "        <navLabel><text>img %03d</text></navLabel>\n"
			+ "        <content src=\"OEBPS/%s.xhtml\" />\n"
			+ "    </navPoint>\n",
			TOC_END = "</navMap>\n"
			+ "</ncx>";

	private void createTocNcx() {
		File outfile = new File(dir, "toc.ncx");
		StringBuilder b = new StringBuilder();
		b.append(TOC_BEGIN);
		for (AlbumItem item : items) {
			int id = item.id + 1;
			String str = FileUtil.removeExtension(item.file.getName());
			b.append(String.format(TOC_NAVPOINT, id, id, id, str));
		}
		b.append(TOC_END);
		FileUtil.fileWriteString(outfile, b.toString());
	}

	private static final String TOCNAV_BEGIN = "<?xml version='1.0' encoding='utf-8'?>\n"
			+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">\n"
			+ "<head>\n"
			+ "    <title>%s</title>\n"
			+ "<style>\n"
			+ "h1, h2, p {\n"
			+ "    text-align: center;"
			+ "    }\n"
			+ "</style>\n"
			+ "</head>\n"
			+ "<body>\n"
			+ "    <h1>%s</h1>\n"
			+ "    <p><i>" + I18N.getMsg("export.by") + "</i></p>\n"
			+ "    <nav epub:type=\"toc\" id=\"toc\">\n"
			+ "        <h2>" + I18N.getMsg("export.list") + "</h2>\n"
			+ "        <ol>\n",
			TOCNAV_ITEM = "            <li><a href=\"%s.xhtml\">%s</a></li>\n",
			TOCNAV_END = "        </ol>\n"
			+ "    </nav>\n"
			+ "</body>\n"
			+ "</html>";

	private void createTitlepage() {
		StringBuilder b = new StringBuilder();
		b.append(String.format(TOCNAV_BEGIN, title, title));
		for (AlbumItem item : items) {
			String fname = FileUtil.removeExtension(item.file.getName());
			String str = String.format(TOCNAV_ITEM, fname, fname);
			b.append(str);
		}
		b.append(TOCNAV_END);
		File outfile = new File(OEBPS, "titlepage.xhtml");
		FileUtil.fileWriteString(outfile, b.toString());
	}

	private void createPages() {
		for (AlbumItem item : items) {
			createPage(item);
		}
	}

	private static final String HTML_PAGE = ""
			+ "<?xml version='1.0' encoding='utf-8'?>\n"
			+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
			+ "<head>\n"
			//+ "    <meta charset=\"UTF-8\" />\n"
			//+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n"
			+ "    <title>%s</title>\n"
			+ "    <link rel=\"stylesheet\" href=\"../CSS/styles.css\" />\n"
			+ "</head>\n"
			+ "<body>\n"
			+ "    <div class=\"slideshow-container\">\n"
			+ "        <div class=\"slideshow\">\n"
			+ "            <img id=\"slideshow-image\" src=\"%s\" alt=\"Image\" />\n"
			+ "            <div id=\"slideshow-text\" class=\"slideshow-text\">%s</div>\n"
			+ "        </div>\n"
			+ "    </div>\n"
			+ "</body>\n"
			+ "</html>";

	private void createPage(AlbumItem item) {
		String str = String.format(HTML_PAGE, title, item.file.getName(), item.text);
		File outfile = new File(OEBPS, FileUtil.changeExt(item.file.getName(), "xhtml"));
		FileUtil.fileWriteString(outfile, str);
	}

	private static final String OPF_START = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
			+ "<package xmlns=\"http://www.idpf.org/2007/opf\" "
			+ "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
			+ "unique-identifier=\"db-id\" version=\"3.0\">\n"
			+ "	<metadata>\n"
			+ "	    <dc:title id=\"t1\">Album photos</dc:title>\n"
			+ "	    <dc:identifier id=\"db-id\">isbn</dc:identifier>\n"
			+ "	    <meta property=\"dcterms:modified\">2014-03-27T09:14:09Z</meta>\n"
			+ "	    <dc:language>fr</dc:language>\n"
			+ "	</metadata>\n"
			+ "	<manifest>\n"
			+ "	    <item id=\"titlepage\" href=\"OEBPS/titlepage.xhtml\" media-type=\"application/xhtml+xml\" properties=\"nav\" />\n"
			+ "	    <item id=\"ncx\" href=\"toc.ncx\" media-type=\"application/x-dtbncx+xml\" />\n"
			+ "	    <item id=\"css\" href=\"CSS/styles.css\" media-type=\"text/css\" />\n",
			OPF_MANIFEST = ""
			+ "	    <item id=\"%s\" href=\"OEBPS/%s\" media-type=\"%s\" />\n",
			OPF_SPINE = " </manifest>\n"
			+ "	<spine toc=\"ncx\">\n"
			+ "<itemref idref=\"titlepage\"/>";
	private static final String OPF_ITEM = "		<itemref idref=\"%s\" />\n";
	private static final String OPF_END = "</spine>\n</package>";

	private void createOPF() {
		StringBuilder b = new StringBuilder();
		b.append(OPF_START);
		// manifest for xhtml files
		String type = "application/xhtml+xml";
		int i = 1;
		for (AlbumItem item : items) {
			String str = FileUtil.removeExtension(item.file.getName()) + ".xhtml";
			b.append(String.format(OPF_MANIFEST, String.format("img_%03d", i++), str, type));
		}
		//manifest for jpg files
		type = "image/jpeg";
		i = 1;
		for (AlbumItem item : items) {
			String str = item.file.getName();
			b.append(String.format(OPF_MANIFEST, String.format("jpg_%03d", i++), str, type));
		}
		b.append(OPF_SPINE);
		i = 1;
		for (AlbumItem item : items) {
			b.append(String.format(OPF_ITEM, String.format("img_%03d", i++)));
		}
		b.append(OPF_END);
		File outfile = new File(dir, "content.opf");
		FileUtil.fileWriteString(outfile, b.toString());
	}

}
