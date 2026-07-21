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
package app.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import tools.LOG;
import tools.file.FileUtil;

/**
 * Classe pivot technique gérant le document DOM XML global.
 */
public class Xml {

	private static final String TT = "Xml.";
	private static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

	private Document document = null;
	private File file = null;
	private Element rootNode = null;
	private boolean opened = false;
	private XmlAlbum xmlAlbum;
	private XmlPrint xmlPrint;
	private XmlLibs xmlLibs;

	public Xml(File file) {
		this.file = file;
		open();
	}

	public static String getHeader() {
		return HEADER;
	}

	private void open() {
		if (file == null || !file.exists()) {
			create();
			return;
		}
		try (InputStream in = new FileInputStream(file)) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(in);
			rootNode = document.getDocumentElement();
		} catch (FileNotFoundException ex) {
			create();
		} catch (ParserConfigurationException | SAXException | IOException ex) {
			LOG.err(TT + "open() parser error on " + file.getName(), ex);
			create();
		}
		this.opened = true;
		xmlAlbum = new XmlAlbum(this);
		xmlLibs = new XmlLibs(this);
		xmlPrint = new XmlPrint(this);
	}

	public boolean isOpened() {
		return opened;
	}

	private void create() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();
			rootNode = document.createElement("album");
			document.appendChild(rootNode);
		} catch (ParserConfigurationException ex) {
			LOG.err(TT + "create() error", ex);
		}
	}

	public void close() {
		// Nettoyage de ressources si nécessaire
	}

	public Document getDocument() {
		return document;
	}

	public File fileGet() {
		return file;
	}

	public Element rootGet() {
		return rootNode;
	}

	public Node nodeGet(String tagName) {
		NodeList list = document.getElementsByTagName(tagName);
		if (list.getLength() > 0) {
			return list.item(0);
		}
		return null;
	}

	public String attributeGet(Element el, String attr) {
		if (el == null) {
			return "";
		}
		return el.getAttribute(attr);
	}

	public void attributeSet(Element el, String attr, String value) {
		if (el != null) {
			el.setAttribute(attr, value);
		}
	}

	public void nodeRemove(String str) {
		NodeList nodes = document.getElementsByTagName(str);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			rootNode.removeChild(node);
		}
	}

	public XmlAlbum albumGet() {
		return xmlAlbum;
	}

	public XmlPrint printGet() {
		return xmlPrint;
	}

	public XmlLibs libsGet() {
		return xmlLibs;
	}

	public Element childCreate(Node parent, String tag, String... attribs) {
		return childCreate(document, parent, tag, attribs);
	}

	public Element childCreate(Document doc, Node parent, String tag, String... attribs) {
		/*LOG.trace("XmlUtil" + ".createChild("
				+ "parent=" + parent.getNodeName()
				+ ", tag=" + tag
				+ ", attribs nb="
				+ (attribs == null ? "0" : attribs.length)
				+ ")");*/
		Element child = doc.createElement(tag);
		if (attribs != null && attribs.length > 0) {
			for (String a : attribs) {
				if (a.contains("=")) {
					String v[] = a.split("=");
					child.setAttribute(v[0], v[1].replace("\"", ""));
				}
			}
		}
		parent.appendChild(child);
		return child;
	}

	/**
	 * save with text method
	 */
	public void save() {
		//LOG.trace(TT + "save()");
		if (document == null || file == null) {
			return;
		}
		StringBuilder b = new StringBuilder();
		b.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
		b.append("<album title=\"").append(albumGet().titleGet()).append("\">\n");
		b.append(albumGet().toXml());
		b.append(xmlLibs.toXml());
		b.append(printGet().toXml());
		b.append("</album>\n");

		//String nf = FileUtil.changeExt(file.getAbsolutePath(), "2.xml");
		FileUtil.fileWriteString(file, b.toString());
	}

}
