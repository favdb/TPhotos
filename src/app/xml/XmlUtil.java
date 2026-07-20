/*
 * Copyright (C) 2023 favdb
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
package app.xml;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import tools.LOG;

/**
 * utility class for setting/getting values from Xml
 *
 * @author favdb
 */
public class XmlUtil {

	public static final String INDENT = "   ";

	public static String attributXml(String key, String value) {
		return key + "=\"" + value + "\" ";
	}

	public static String attributXml(String key, Integer value) {
		return key + "=\"" + value.toString() + "\" ";
	}

	public static String indent(int n) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < n; i++) {
			b.append(INDENT);
		}
		return b.toString();
	}

	public XmlUtil() {

	}

	/**
	 * set a Xml output String for a String value
	 *
	 * @param tab
	 * @param key
	 * @param value
	 * @return
	 */
	public static String setAttribute(int tab, String key, String value) {
		if (value == null || value.isEmpty()) {
			return "";
		}
		String b = "";
		if (tab > 0) {
			b = "\n";
			for (int i = 0; i < tab; i++) {
				b += " ";
			}
		}
		return b + key + "=\"" + value.replace("\"", "''") + "\" ";
	}

	/**
	 * set a Xml output String for a String value
	 *
	 * @param tab
	 * @param key
	 * @param value
	 * @return
	 */
	public static String setAttribute(int tab, XmlKey.XK key, String value) {
		return setAttribute(tab, key.toString().toLowerCase(), value);
	}

	/**
	 * set a Xml output String for an Integer value
	 *
	 * @param tab
	 * @param key
	 * @param value
	 * @return
	 */
	public static String setAttribute(int tab, XmlKey.XK key, Integer value) {
		if (value == null) {
			return "";
		}
		return setAttribute(tab, key.toString(), value.toString());
	}

	/**
	 * set a Xml output String for an Integer value
	 *
	 * @param tab
	 * @param key
	 * @param value
	 * @return
	 */
	public static String setAttribute(int tab, String key, Integer value) {
		if (value == null) {
			return "";
		}
		return setAttribute(tab, key, value.toString());
	}

	/**
	 * get a String for a child node
	 *
	 * @param n: level indent
	 * @param key: key name of the node
	 * @param value: value to set
	 * @param html: true if value is HTML
	 *
	 * @return
	 */
	public static String childSet(int n, String key, String value, boolean html) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < n; i++) {
			b.append("    ");
		}
		b.append("<").append(key.toLowerCase()).append(">");
		if ((value != null) && !value.isEmpty()) {
			if (html) {
				if (!value.replace("<p>", "").replace("</p>", "").replace("\n", "").trim().isEmpty()) {
					b.append("<![CDATA[").append(value).append("]]>");
				}
			} else {
				b.append(value);
			}
		}
		b.append("</").append(key.toLowerCase()).append(">\n");
		return b.toString();
	}

	/**
	 * get a String value of a node attribute, if not exists return ""
	 *
	 * @param node
	 * @param key
	 * @return
	 */
	public static String stringGet(Node node, XmlKey.XK key) {
		return stringGet(node, key.toString().toLowerCase());
	}

	public static String stringGet(Node node, String n) {
		Element e = (Element) node;
		return (e.getAttribute(n).trim());
	}

	/**
	 * get an Integer value of a node attribute, if not exists return -1
	 *
	 * @param node
	 * @param key
	 * @return
	 */
	public static Integer integerGet(Node node, XmlKey.XK key) {
		return integerGet(node, key.toString().toLowerCase());
	}

	public static Integer integerGet(Node node, String n) {
		String x = stringGet(node, n);
		if (x == null || x.isEmpty()) {
			return -1;
		}
		return Integer.valueOf(x);
	}

	/**
	 * get the String value of a node attribute
	 *
	 * @param node
	 * @param key
	 * @return
	 */
	public static String textGet(Node node, XmlKey.XK key) {
		return textGet(node, key.toString().toLowerCase());
	}

	public static String textGet(Node node, String key) {
		NodeList t = node.getChildNodes();
		if (t.getLength() > 0) {
			for (int i = 0; i < t.getLength(); i++) {
				if (t.item(i).getNodeName().equals(key)) {
					return (t.item(i).getTextContent().trim());
				}
			}
		}
		return ("");
	}

	/**
	 * get a beautify Xml code
	 *
	 * @param xmlString : the Xml String to beautify
	 *
	 * @return
	 */
	public static String beautify(String xmlString) {
		try {
			InputSource src = new InputSource(new StringReader(xmlString));
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(src);

			TransformerFactory factory = TransformerFactory.newInstance();
			factory.setAttribute("indent-number", 3);
			Transformer trans = factory.newTransformer();
			trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			Writer out = new StringWriter();
			trans.transform(new DOMSource(document), new StreamResult(out));
			return out.toString();
		} catch (IOException
				| IllegalArgumentException
				| ParserConfigurationException
				| TransformerException
				| SAXException e) {
			LOG.err("Error occurs when pretty-printing xml:\n" + xmlString, e);
			return xmlString;
		}
	}

	public static String elementToString(Element element) {
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(element), new StreamResult(writer));
			return writer.toString();
		} catch (TransformerConfigurationException ex) {
			LOG.err("XmlUtil...", ex);
		} catch (TransformerException ex) {
			LOG.err("XmlUtil...", ex);
		}
		return "err";
	}
}
