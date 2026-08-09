package app.xml;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import tools.LOG;

/**
 * Gère les métadonnées de base de la balise racine <album>.
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

	public String getPrefComment() {
		return prefComment;
	}

	public void setPrefComment(String prefComment) {
		this.prefComment = prefComment;
	}

	public int getPrefMode() {
		return prefMode;
	}

	public void setPrefMode(int prefMode) {
		this.prefMode = prefMode;
	}

	public int getPrefTempo() {
		return prefTempo;
	}

	public void setPrefTempo(int tempo) {
		this.prefTempo = tempo;
	}

	public String titleGet() {
		return title;
	}

	public void titleSet(String title) {
		this.title = title;
	}

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

	public List<XmlAlbumItem> itemsGet() {
		return items;
	}

	public XmlAlbumItem itemGet(int i) {
		if (i < items.size()) {
			return items.get(i);
		}
		return null;
	}

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

	public void itemsSet(List<XmlAlbumItem> list) {
		//LOG.trace(TT + "itemsSet(items nb=" + list.size() + ")");
		items.clear();
		for (XmlAlbumItem item : list) {
			items.add(item);
		}
	}

	public void itemAdd(XmlAlbumItem item) {
		LOG.trace(TT + "itemAdd(item)");
		items.add(item);
	}

	public void itemRemove(XmlAlbumItem item) {
		LOG.trace(TT + "itemRemove(item)");
		items.remove(item);
	}

}
