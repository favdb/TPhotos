package app.xml;

import app.App;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Gère les métadonnées de base de la balise racine <album>.
 */
public class XmlAlbum {

	private final Xml xml;
	private String title = "album", prefComment = "";
	private int prefMode = 0, prefTempo = 0;
	private List<XmlAlbumPhoto> photos = new ArrayList<>();

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
		photos.clear();
		NodeList nodes = xml.rootGet().getElementsByTagName("item");
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				Element child = (Element) nodes.item(i);
				XmlAlbumPhoto p = new XmlAlbumPhoto(child.getAttribute("id"),
						App.preferences.photosDirGet() + File.separator + child.getAttribute("file"),
						child.getAttribute("comment"));
				photos.add(p);
			}
		}
	}

	public List<XmlAlbumPhoto> photosAllGet() {
		return photos;
	}

	public XmlAlbumPhoto getPhoto(int i) {
		if (i < photos.size()) {
			return photos.get(i);
		}
		return null;
	}

	public String toXml() {
		StringBuilder b = new StringBuilder();
		b.append(XmlUtil.INDENT).append("<pref ")
				.append(XmlUtil.attributXml("comment", getPrefComment()))
				.append(XmlUtil.attributXml("mode", getPrefMode()))
				.append(XmlUtil.attributXml("tempo", getPrefTempo()))
				.append("/>\n");
		b.append(XmlUtil.INDENT).append("<list>\n");
		for (XmlAlbumPhoto p : photos) {
			b.append(p.toXml());
		}
		b.append(XmlUtil.INDENT).append("</list>\n");
		return b.toString();
	}

}
