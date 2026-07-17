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
	private final List<XmlPhoto> photos = new ArrayList<>();

	public XmlAlbum(Xml xml) {
		this.xml = xml;
	}

	public String getTitle() {
		return xml.attributeGet(xml.getRoot(), "title");
	}

	public void setTitle(String title) {
		xml.attributeSet(xml.getRoot(), "title", title);
	}

	public List<XmlPhoto> load() {
		//LOG.trace(TT + "load()");
		photos.clear();
		NodeList nodes = xml.getRoot().getElementsByTagName("item");
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				Element child = (Element) nodes.item(i);
				XmlPhoto p = new XmlPhoto(child.getAttribute("id"),
						App.preferences.photosDirGet() + File.separator + child.getAttribute("file"),
						child.getAttribute("comment"));
				photos.add(p);
			}
		}
		return photos;
	}

	public XmlPhoto getPhoto(int i) {
		if (i < photos.size()) {
			return photos.get(i);
		}
		return null;
	}

}
