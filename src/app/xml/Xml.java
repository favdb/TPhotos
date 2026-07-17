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

import app.print.PrintItem;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import tools.LOG;

/**
 * Classe pivot technique gérant le document DOM XML global.
 */
public class Xml {

	private static final String TT = "Xml.";
	private static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

	public static String indent(int i, String list) {
		return "   ";
	}

	private Document document = null;
	private File file = null;
	private Element rootNode = null;
	private boolean opened = false;

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

	public File getFile() {
		return file;
	}

	public Element getRoot() {
		return rootNode;
	}

	public Node getNode(String tagName) {
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

	public void removeNode(String str) {
		NodeList nodes = document.getElementsByTagName(str);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			rootNode.removeChild(node);
		}
	}

	public XmlAlbum getAlbum() {
		return new XmlAlbum(this);
	}

	public XmlPrint getPrint() {
		return new XmlPrint(this);
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

	public PrintItem getTextCell(String id) {
		PrintItem pc = new PrintItem();
		XmlAlbum album = getAlbum();
		NodeList nodes = this.getRoot().getElementsByTagName("lib");
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				Element child = (Element) nodes.item(i);
				if (attributeGet(child, "id").equals(id)) {
					pc.cellIdSet(XmlUtil.getInteger(child, "id"));
					pc.textSet(child.getTextContent());
					return pc;
				}
			}
		}
		return null;
	}

	public PrintItem getPhotoCell(String id) {
		PrintItem pc = new PrintItem();
		XmlAlbum album = getAlbum();
		NodeList nodes = this.getRoot().getElementsByTagName("item");
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				Element child = (Element) nodes.item(i);
				if (attributeGet(child, "id").equals(id)) {
					pc.cellIdSet(XmlUtil.getInteger(child, "id"));
					pc.photoFileSet(XmlUtil.getString(child, "file"));
					pc.commentSet(XmlUtil.getString(child, "comment"));
					return pc;
				}
			}
		}
		return null;
	}

	/**
	 * Met à jour le contenu d'une bibliothèque de texte par son identifiant.
	 *
	 * * @param id l'identifiant du texte à modifier
	 * @param id
	 * @param newText le nouveau texte HTML provenant de SHEF
	 */
	public void textLibraryUpdate(int id, String newText) {
		if (document == null) {
			return;
		}

		org.w3c.dom.NodeList libNodes = document.getElementsByTagName("lib");
		for (int i = 0; i < libNodes.getLength(); i++) {
			org.w3c.dom.Element el = (org.w3c.dom.Element) libNodes.item(i);
			String idStr = attributeGet(el, "id");

			if (!idStr.isEmpty() && Integer.parseInt(idStr) == id) {
				// Remplacement du contenu par le nouveau texte épuré/HTML
				el.setTextContent(newText);
				return;
			}
		}
	}

	/**
	 * load all cell from album and print
	 *
	 * @return
	 */
	public List<PrintItem> loadCells() {
		List<PrintItem> list = new ArrayList<>();
		//load photos
		List<XmlPhoto> xphotos = getAlbum().load();
		for (XmlPhoto x : xphotos) {
			int cellid = Integer.parseInt(x.getId());
			list.add(new PrintItem(cellid, cellid, x.getFile(), x.getComment(), 0));
		}
		// load libs
		List<XmlLib> xlibs = getPrint().loadLibs();
		int cellId = list.size() + 1;
		for (XmlLib x : xlibs) {
			int textId = Integer.parseInt(x.getId());
			PrintItem cell = new PrintItem(cellId, x.getText(), 0);
			cell.textIdSet(textId);
			list.add(cell);
		}
		// update photos and libs for printing
		List<XmlPrintPage> ppx = getPrint().printPageGetAll();
		for (int np = 0; np < ppx.size(); np++) {
			XmlPrintPage pp = ppx.get(np);
			for (PrintItem pc : pp.cellsGet()) {
				updateCell(list, np + 1, pc);
			}
		}
		return list;
	}

	public void updateCell(List<PrintItem> list, int page, PrintItem pc) {
		for (PrintItem c : list) {
			if (c.isPhoto() && c.photoIdGet() == pc.photoIdGet()) {
				c.pageSet(page);
				c.posSet(pc.posGet());
				break;
			} else if (!c.isPhoto() && c.textIdGet() == pc.textIdGet()) {
				c.pageSet(page);
				c.posSet(pc.posGet());
				break;
			}
		}
	}

	/**
	 * Sauvegarde le document DOM actuel dans le fichier XML.
	 */
	public void save() {
		if (document == null || file == null) {
			return;
		}
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();

			// Configuration pour un rendu propre (indentation)
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(file);

			transformer.transform(source, result);
			//LOG.trace(TT + "save() XML sauvegardé avec succès.");
		} catch (javax.xml.transform.TransformerException ex) {
			LOG.err(TT + "save() Erreur lors de l'écriture du fichier XML", ex);
		}
	}

}
