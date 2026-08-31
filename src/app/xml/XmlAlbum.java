package app.xml;

import app.tools.LOG;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Manage meta data for the Xml Album data base.
 */
public class XmlAlbum {

	private static final String TT = "XmlAlbum.";

	private final Xml xml;
	private String title = "album", prefComment = "";
	private int prefMode = 0, prefTempo = 0;
	private final List<XmlAlbumItem> items = new ArrayList<>();

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public XmlAlbum(Xml xml) {
		this.xml = xml;
		load();
	}

	/**
	 * get comment template
	 *
	 * @return
	 */
	public String getPrefComment() {
		return prefComment;
	}

	/**
	 * set comment template
	 *
	 * @param prefComment
	 */
	public void setPrefComment(String prefComment) {
		this.prefComment = prefComment;
	}

	/**
	 * get pref mode
	 *
	 * @return
	 */
	public int getPrefMode() {
		return prefMode;
	}

	/**
	 * set pref mode
	 *
	 * @param prefMode
	 */
	public void setPrefMode(int prefMode) {
		this.prefMode = prefMode;
	}

	/**
	 * get tempo from preferences
	 *
	 * @return
	 */
	public int getPrefTempo() {
		return prefTempo;
	}

	/**
	 * set tempo to preferences
	 *
	 * @param tempo
	 */
	public void setPrefTempo(int tempo) {
		this.prefTempo = tempo;
	}

	/**
	 * get the title
	 *
	 * @return
	 */
	public String titleGet() {
		return title;
	}

	/**
	 * set the title
	 *
	 * @param title
	 */
	public void titleSet(String title) {
		this.title = title;
	}

	/**
	 * load data from XML nodes
	 */
	public void load() {
		//LOG.trace(TT + "load()");
		title = xml.attributeGet(xml.rootGet(), "title");
		items.clear();
		NodeList nodes = xml.rootGet().getElementsByTagName("item");
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				Element child = (Element) nodes.item(i);
				XmlAlbumItem p = new XmlAlbumItem(child.getAttribute("id"),
						child.getAttribute("file"),
						child.getAttribute("comment"));
				items.add(p);
			}
		}
	}

	/**
	 * get list of items
	 *
	 * @return
	 */
	public List<XmlAlbumItem> itemsGet() {
		return items;
	}

	/**
	 * get the item for given index
	 *
	 * @param i
	 * @return
	 */
	public XmlAlbumItem itemGet(int i) {
		if (i < items.size()) {
			return items.get(i);
		}
		return null;
	}

	/**
	 * get the XML string for the preferences and list of items
	 *
	 * @return
	 */
	public String toXml() {
		//LOG.trace(TT + "toXml() items nb=" + items.size());
		StringBuilder b = new StringBuilder();
		b.append(XmlUtil.INDENT).append("<pref ")
				.append(XmlUtil.attributXml("comment", getPrefComment()))
				.append(XmlUtil.attributXml("mode", getPrefMode()))
				.append(XmlUtil.attributXml("tempo", getPrefTempo()))
				.append("/>\n");
		b.append(XmlUtil.INDENT).append("<list>\n");
		for (XmlAlbumItem p : items) {
			b.append(p.toXml());
		}
		b.append(XmlUtil.INDENT).append("</list>\n");
		return b.toString();
	}

	/**
	 * set the item list
	 *
	 * @param list
	 */
	public void itemsSet(List<XmlAlbumItem> list) {
		//LOG.trace(TT + "itemsSet(items nb=" + list.size() + ")");
		items.clear();
		for (XmlAlbumItem item : list) {
			items.add(item);
		}
	}

	/**
	 * add the given item
	 *
	 * @param item
	 */
	public void itemAdd(XmlAlbumItem item) {
		LOG.trace(TT + "itemAdd(item)");
		items.add(item);
	}

	/**
	 * remove the given item
	 *
	 * @param item
	 */
	public void itemRemove(XmlAlbumItem item) {
		LOG.trace(TT + "itemRemove(item)");
		items.remove(item);
	}

}
