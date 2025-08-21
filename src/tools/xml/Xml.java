/*
 * Copyright (C) 2022 favdb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tools.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import tools.LOG;
import tools.file.FileUtil;

/**
 * class for reading a XML file
 *
 * @author favdb
 */
public class Xml {

	private static final String TT = "Xml.";

	public static final String HEADER = "<?xml"
			+ " version=\"1.0\""
			+ " encoding=\"UTF-8\""
			+ "?>\n";

	public static String xmlToText(String xmlText) {
		return xmlText;
	}

	private DocumentBuilder documentBuilder;
	private InputStream stream;
	private Document document;
	private Element rootNode;
	private String fileName = "";
	private boolean fileOpened;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Xml(File file) {
		try {
			stream = new FileInputStream(file.getAbsolutePath());
			open();
		} catch (FileNotFoundException ex) {
			create();
		}
		this.fileName = file.getAbsolutePath();
	}

	public Xml(String fileName) {
		this(new File(fileName));
	}

	public File getFile() {
		return new File(fileName);
	}

	/**
	 * create an empty XML document
	 */
	private void create() {
		try {
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			LOG.err(TT + ".create() DocumentBuilder error", ex);
			return;
		}
		document = documentBuilder.newDocument();
		if (document == null) {
			rootNode = null;
			return;
		}
		rootNode = document.createElement("album");
		document.appendChild(rootNode);
	}

	/**
	 * open the XML file
	 *
	 * @return true if OK
	 */
	public boolean open() {
		//LOG.trace(TT+".open()");
		fileOpened = false;
		documentBuilder = null;
		try {
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			LOG.err(TT + ".open() DocumentBuilder error", ex);
			return false;
		}
		document = initDocument();
		if (document == null) {
			rootNode = null;
			return false;
		}
		rootNode = document.getDocumentElement();
		fileOpened = true;
		return true;
	}

	/**
	 * check if the file was opened
	 *
	 * @return
	 */
	public boolean isOpened() {
		return fileOpened;
	}

	/**
	 * close the XML file
	 *
	 */
	public void close() {
		//LOG.trace(TT+".close()");
		if (fileOpened) {
			fileOpened = false;
			document = null;
			documentBuilder = null;
		}
	}

	/**
	 * save the Xml to a file
	 *
	 * @param toFile
	 * @return true if save is OK
	 */
	public boolean save(String... toFile) {
		//LOG.trace(TT + "save(toFile=" + (toFile != null && toFile.length > 0 ? toFile[0] : "empty") + ")");
		boolean rc = false;
		String fn;
		if (toFile != null && toFile.length > 0) {
			fn = toFile[0];
		} else if (!fileName.isEmpty()) {
			fn = fileName;
		} else {
			LOG.err(TT + ".save(toFile) missing fileName");
			return false;
		}
		//LOG.trace(TT + ".save(...) fileName=" + fn);
		File fileSave = new File(fn);
		if (fileSave.exists() && toFile.length > 0) {
			LOG.err(TT + ".save(...) fn=\"" + fn + "\" exists");
			return rc;
		}
		return save(document, fileSave);
	}

	/**
	 * save a XMLdocument to a file
	 *
	 * @param document
	 * @param file
	 * @return
	 */
	public static boolean save(Document document, File file) {
		try {
			DOMSource domSource = new DOMSource(document);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer;
			transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.transform(domSource,
					new StreamResult(
							new OutputStreamWriter(
									new FileOutputStream(file),
									StandardCharsets.UTF_8)));
			return true;
		} catch (TransformerConfigurationException ex) {
			LOG.err(TT + ".save() exception", ex);
			return false;
		} catch (FileNotFoundException | TransformerException ex) {
			LOG.err(TT + ".save() exception", ex);
			return false;
		}
	}

	/**
	 * get the XML Document object
	 *
	 * @return
	 */
	public Document initDocument() {
		//LOG.trace(TT+".getDocument()");
		Document rc = null;
		try {
			rc = documentBuilder.parse(stream);
		} catch (SAXException ex) {
			LOG.err(TT + ".initDocument() Parsing XML error", ex);
		} catch (IOException ex) {
			LOG.err(TT + ".initDocument() I/O error", ex);
		}
		return rc;
	}

	public Document getDocument() {
		return document;
	}

	/**
	 * get the root node
	 *
	 * @return
	 */
	public Element getRoot() {
		return rootNode;
	}

	/**
	 * get the root node of a parsing XML String
	 *
	 * @param xml: String containing the XML
	 *
	 * @return the root node as an Element
	 */
	public static Element getRootNode(String xml) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(xml)));
			Element rootNode = doc.getDocumentElement();
			return (rootNode);
		} catch (ParserConfigurationException | SAXException | IOException ex) {
			LOG.err("TempUtil restore failed", ex);
			return (null);
		}
	}

	/**
	 * get a named node from the root node
	 *
	 * @param key
	 * @return
	 */
	public Node getNode(String key) {
		return rootNode.getElementsByTagName(key).item(0);
	}

	/**
	 * get the value of a child of a node
	 *
	 * @param node
	 * @param child
	 * @return
	 */
	public String getChildValue(Node node, String child) {
		NodeList childs = ((Element) node).getElementsByTagName(child);
		if (childs.getLength() > 0) {
			return childs.item(0).getTextContent();
		}
		return "none";
	}

	/**
	 * get the value of an attribute of child of a node
	 *
	 * @param node
	 * @param child
	 * @param qualifier
	 * @return
	 */
	public String getChildValue(Node node, String child, String qualifier) {
		NodeList childs = ((Element) node).getElementsByTagName(child);
		if (childs.getLength() > 0) {
			for (int i = 0; i < childs.getLength(); i++) {
				if (nodeToString(childs.item(i))
						.toLowerCase().contains(qualifier.toLowerCase())) {
					return childs.item(i).getTextContent();
				}
			}
		}
		return "";
	}

	/**
	 * get the attribute value of a Node
	 *
	 * @param node: the Node
	 * @param key: the attribute key
	 *
	 * @return
	 */
	public static String attributeGet(Node node, String key) {
		NamedNodeMap attrs = node.getAttributes();
		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				Node attr = attrs.item(i);
				if (attr.getNodeName().equalsIgnoreCase(key)) {
					return attr.getNodeValue().trim();
				}
			}
		}
		return "";
	}

	/**
	 * get the attribute value of a String Node
	 *
	 * @param node: the Node String
	 * @param key: the attribute key name
	 * @return
	 */
	public static String attributeGet(String node, String key) {
		int hstart = node.toLowerCase().indexOf(key.toLowerCase()) + key.toLowerCase().length();
		String sx = node.substring(hstart);
		sx = sx.substring(0, sx.indexOf("\""));
		return sx;
	}

	/**
	 * set the Attribute value for a given Node
	 *
	 * @param node : the given Node
	 * @param key : key String of the Attribute
	 * @param value : the Object value to set, maybe:
	 * <ul>
	 * <li>a String</li>
	 * <li>a Long</li>
	 * <li>an Integer</li>
	 * <li>a Boolean</li>
	 * </ul>
	 */
	public static void attributeSet(Node node, String key, Object value) {
		if (node == null || key == null || key.isEmpty()) {
			return;
		}
		String val;
		if (value instanceof String
				|| value instanceof Long
				|| value instanceof Integer
				|| value instanceof Boolean) {
			val = value.toString();
		} else {
			val = null;
		}
		Element e = (Element) node;
		if (e.getAttribute(key) != null) {
			if (val != null && !val.isEmpty()) {
				e.setAttribute(key, val);
			} else {
				e.removeAttribute(key);
			}
			return;
		}
		if (val == null || val.isEmpty()) {
			return;
		}
		Node nv = node.getAttributes().getNamedItem(key);
		if (nv != null) {
			nv.setNodeValue(val);
		}
	}

	/**
	 * get the String value of a Node
	 *
	 * @param node
	 * @return
	 */
	public static String nodeToString(Node node) {
		StringBuilder b = new StringBuilder();
		getXMLString(node, true, b, true);
		return b.toString();
	}

	/**
	 * get the XML String of a give Node
	 *
	 * @param node
	 * @param withoutNamespaces
	 * @param buff
	 * @param endTag
	 */
	public static void getXMLString(Node node,
			boolean withoutNamespaces,
			StringBuilder buff,
			boolean endTag) {
		buff.append("<").append(node.getNodeName());
		if (node.hasAttributes()) {
			buff.append(" ");
			NamedNodeMap attr = node.getAttributes();
			for (int i = 0; i < attr.getLength(); i++) {
				Node attrItem = attr.item(i);
				String name = attrItem.getNodeName();
				String value = attrItem.getNodeValue();
				buff.append(name).append("=\"").append(value).append("\"");
				if (i < attr.getLength() - 1) {
					buff.append(" ");
				}
			}
		}
		if (node.hasChildNodes()) {
			buff.append(">");
			NodeList children = node.getChildNodes();
			int childrenCount = children.getLength();
			if (childrenCount == 1) {
				Node item = children.item(0);
				int itemType = item.getNodeType();
				if (itemType == Node.TEXT_NODE) {
					if (item.getNodeValue() == null) {
						buff.append("/>");
					} else {
						buff.append(item.getNodeValue());
						buff.append("</")
								.append(node.getNodeName())
								.append(">");
					}
					endTag = false;
				}
			}
			for (int i = 0; i < childrenCount; i++) {
				Node item = children.item(i);
				int itemType = item.getNodeType();
				if (itemType == Node.DOCUMENT_NODE || itemType == Node.ELEMENT_NODE) {
					getXMLString(item, withoutNamespaces, buff, endTag);
				}
			}
		} else {
			if (node.getNodeValue() == null) {
				buff.append("/>");
			} else {
				buff.append(node.getNodeValue());
				buff.append("</").append(node.getNodeName()).append(">");
			}
			endTag = false;
		}
		if (endTag) {
			buff.append("</").append(node.getNodeName()).append(">");
		}
	}

	/**
	 * remove an attribute from the given Node
	 *
	 * @param node
	 * @param key
	 */
	public static void attributeRemove(Node node, String key) {
		Element e = (Element) node;
		if (e == null) {
			return;
		}
		if (e.getAttribute(key) != null) {
			e.removeAttribute(key);
		}
	}

	public Element childCreate(Node parent, String tag, String... attribs) {
		return childCreate(document, parent, tag, attribs);
	}

	public static Element childCreate(Document doc, Node parent, String tag, String... attribs) {
		/*App.trace("XmlUtil" + ".createChild("
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
	 * enclose the given Xml content into the right header/footer
	 *
	 * @param rootName
	 * @param str
	 * @return
	 */
	public static String intoXml(String str, String... rootName) {
		StringBuilder b = new StringBuilder();
		if (!str.startsWith("<?xml")) {
			b.append(HEADER);
		}
		String rn = "TPhotos";
		if (rootName != null && rootName.length > 0) {
			rn = rootName[0];
		}
		if (!str.contains("<" + rn + ">")) {
			b.append("<").append(rn).append(">\n");
		}
		b.append(str);
		if (!str.contains("</" + rn + ">")) {
			b.append("</").append(rn).append(">\n");
		}
		return b.toString();
	}

	public void removeNode(String str) {
		NodeList nodes = document.getElementsByTagName(str);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			rootNode.removeChild(node);
			removeAllChildNodes(node);
		}
	}

	private void removeAllChildNodes(Node node) {
		if (node == null) {
			return;
		}
		NodeList childNodes = node.getChildNodes();
		if (childNodes == null) {
			return;
		}
		int length = childNodes.getLength();
		for (int i = 0; i < length; i++) {
			Node childNode = childNodes.item(i);
			if (childNode instanceof Element) {
				if (childNode.hasChildNodes()) {
					removeAllChildNodes(childNode);
				}
				node.removeChild(childNode);
			}
		}
	}

	public void removeAllNodes(String str) {
		NodeList nodes = document.getElementsByTagName(str);
		int nb = nodes.getLength();
		for (int i = 0; i < nb; i++) {
			Node node = nodes.item(i);
			removeAllChildNodes(node);
			//rootNode.removeChild(node);
		}
		document.normalize();
	}

	public void setTitle(String title) {
		attributeSet(rootNode, "title", title);
	}

	public String getTitle() {
		String title = attributeGet(rootNode, "title");
		if (title == null || title.isEmpty()) {
			return FileUtil.removeExtension(getFile().getName());
		}
		return title;
	}
}
