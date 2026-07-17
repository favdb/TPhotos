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

import app.xml.XmlKey;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
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
import app.xml.XmlKey.XK;

/**
 * utility class for setting/getting values from Xml
 *
 * @author favdb
 */
public class XmlUtil {

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
	 * set a Xml output String for a boolean value (set only the getId())
	 *
	 * @param tab
	 * @param key
	 * @param value
	 * @return
	 */
	public static String setAttribute(int tab, XmlKey.XK key, boolean value) {
		return setAttribute(tab, key.toString(), (value ? "1" : "0"));
	}

	/**
	 * set a Xml output String for a boolean value
	 *
	 * @param tab
	 * @param key
	 * @param value
	 * @return
	 */
	public static String setAttribute(int tab, String key, boolean value) {
		return setAttribute(tab, key, (value ? "1" : "0"));
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
	 * set a Xml output String for a Long value
	 *
	 * @param tab
	 * @param name
	 * @param value
	 * @return
	 */
	public static String setAttribute(int tab, XmlKey.XK name, Long value) {
		if (value == null) {
			return "";
		}
		return setAttribute(tab, name.toString(), value.toString());
	}

	/**
	 * set a Xml output String for a Long value
	 *
	 * @param tab
	 * @param key
	 * @param value
	 * @return
	 */
	public static String setAttribute(int tab, String key, Long value) {
		if (value == null) {
			return "";
		}
		return setAttribute(tab, key, value.toString());
	}

	/**
	 * set a Xml output String for a Timestamp value
	 *
	 * @param tab
	 * @param name
	 * @param value
	 * @param dateonly
	 *
	 * @return
	 */
	public static String setAttribute(int tab, XK name, Timestamp value, boolean... dateonly) {
		if (value == null) {
			return "";
		}
		if (dateonly != null && dateonly[0]) {
			return setAttribute(tab, name, value.toString().split(" ")[0]);
		}
		return setAttribute(tab, name, value.toString());
	}

	/**
	 * set a Xml output String for a Date value
	 *
	 * @param tab
	 * @param name
	 * @param value
	 * @return
	 */
	public static String setAttribute(int tab, XK name, Date value) {
		if (value == null) {
			return "";
		}
		return setAttribute(tab, name, value.toString());
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
	public static String setChild(int n, String key, String value, boolean html) {
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
	public static String getString(Node node, XmlKey.XK key) {
		return getString(node, key.toString().toLowerCase());
	}

	public static String getString(Node node, String n) {
		Element e = (Element) node;
		return (e.getAttribute(n).trim());
	}

	/**
	 * get a Boolean value of a node attribute, if not exists return false
	 *
	 * @param node
	 * @param key
	 * @return
	 */
	public static boolean getBoolean(Node node, XmlKey.XK key) {
		return getBoolean(node, key.toString().toLowerCase());
	}

	public static boolean getBoolean(Node node, String key) {
		String s = getString(node, key);
		return (s.equals("yes") || s.equals("true") || s.equals("1"));
	}

	/**
	 * get a Long value of a node attribute, if not exists return -1L
	 *
	 * @param node
	 * @param key
	 * @return
	 */
	public static Long getLong(Node node, String key) {
		String x = getString(node, key);
		if (x.isEmpty() || "null".equals(x)) {
			return -1L;
		}
		return Long.valueOf(x);
	}

	public static Long getLong(Node node, XmlKey.XK key) {
		return getLong(node, key.toString().toLowerCase());
	}

	/**
	 * get an Integer value of a node attribute, if not exists return -1
	 *
	 * @param node
	 * @param key
	 * @return
	 */
	public static Integer getInteger(Node node, XmlKey.XK key) {
		return getInteger(node, key.toString().toLowerCase());
	}

	public static Integer getInteger(Node node, String n) {
		String x = getString(node, n);
		if (x == null || x.isEmpty()) {
			return -1;
		}
		return Integer.valueOf(x);
	}

	/**
	 * get the Timestamp value of a node attribute
	 *
	 * @param node
	 * @param key
	 * @return
	 */
	public static Timestamp getTimestamp(Node node, XmlKey.XK key) {
		return XmlUtil.getTimestamp(node, key.toString().toLowerCase());
	}

	public static Timestamp getTimestamp(Node node, String key) {
		String ts = getString(node, key);
		if (!ts.isEmpty()) {
			if (!ts.contains(":")) {
				ts = ts + " 00:00:00";
			}
			return (Timestamp.valueOf(ts));
		}
		return ((Timestamp) null);
	}

	/**
	 * get the String value of a node attribute
	 *
	 * @param node
	 * @param key
	 * @return
	 */
	public static String getText(Node node, XmlKey.XK key) {
		return getText(node, key.toString().toLowerCase());
	}

	public static String getText(Node node, String key) {
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
	 * get a list of String from a node attribute
	 *
	 * @param node
	 * @param key
	 * @return
	 */
	public static List<String> getList(Node node, XmlKey.XK key) {
		return getList(node, key.toString().toLowerCase());
	}

	public static List<String> getList(Node node, String key) {
		NodeList t = node.getChildNodes();
		List<String> list = new ArrayList<>();
		if (t.getLength() > 0) {
			for (int i = 0; i < t.getLength(); i++) {
				if (t.item(i).getNodeName().equals(key)) {
					list.add(t.item(i).getTextContent().trim());
				}
			}
		}
		return (list);
	}

	/**
	 * get a list of Long attribute
	 *
	 * @param node
	 * @param key
	 * @return
	 */
	public static List<Long> getLongList(Node node, XK key) {
		String ns = getString(node, key);
		List<Long> list = new ArrayList<>();
		if (!ns.isEmpty()) {
			String nl[] = ns.split(",");
			for (String x : nl) {
				list.add(Long.valueOf(x.trim()));
			}
		}
		return list;
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

}
