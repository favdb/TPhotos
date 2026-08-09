package app.diapo;

import app.xml.Xml;
import app.xml.XmlUtil;
import i18n.I18N;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import tools.LOG;
import tools.file.FileUtil;

public class DiapoParam {

	private String name = "Album", comment = "{JJ/MM/AAAA}";
	private Integer mode = 0, tempo = 0;

	public DiapoParam() {
	}

	public DiapoParam(Xml xml) {
		Node n = xml.nodeGet("pref");
		if (n != null) {
			mode = XmlUtil.integerGet(n, "mode");
			tempo = XmlUtil.integerGet(n, "tempo");
			comment = XmlUtil.stringGet(n, "comment");
		} else {
			xml.childCreate(xml.rootGet(), "pref");
		}
	}

	public void setMode(int value) {
		this.mode = value;
	}

	public Integer getMode() {
		if (mode < 0) {
			return 0;
		}
		return mode;
	}

	public void setTempo(int value) {
		this.tempo = value;
	}

	public Integer getTempo() {
		if (tempo < 0) {
			return 0;
		}
		return tempo;
	}

	public void setComment(String value) {
		this.comment = value;
	}

	public String getComment() {
		return comment;
	}

	public String getComment(File file) {
		return getComment(file, this.comment);
	}

	/**
	 * Formate un modèle de commentaire pour un fichier donné
	 *
	 * @param file Fichier photo
	 * @param template Modèle de texte contenant des balises entre accolades
	 * @return Commentaire transformé
	 */
	public String getComment(File file, String template) {
		if (file == null
				|| template == null
				|| !template.contains("{")
				|| !template.contains("}")) {
			return template;
		}

		String fileName = FileUtil.removeExtension(file.getName());
		Date date = FileUtil.parseDateFromFilename(fileName);
		if (date == null) {
			return template;
		}

		String str = template;

		// 1. Remplacement des clés I18N insérées via la liste déroulante
		Map<String, String> df = new HashMap<>();
		df.put("album.param.comment.day", "dd/MM/yyyy");
		df.put("album.param.comment.month", "MM/yyyy");
		df.put("album.param.comment.year", "yyyy");
		df.put("album.param.comment.month_long", "MMMM yyyy");
		df.put("album.param.comment.full", "dd MMMM yyyy");
		df.put("album.param.comment.full_hour", "dd MMMM yyyy HH:mm:ss");

		for (Map.Entry<String, String> entry : df.entrySet()) {
			String motif = "{" + I18N.getMsg(entry.getKey()) + "}";
			if (str.contains(motif)) {
				SimpleDateFormat sdf = new SimpleDateFormat(entry.getValue(), Locale.getDefault());
				str = str.replace(motif, sdf.format(date));
			}
		}

		// 2. Traitement dynamique des motifs anglais personnalisés entre accolades { ... }
		while (str.contains("{") && str.contains("}")) {
			int start = str.indexOf("{");
			int end = str.indexOf("}", start);
			if (start == -1 || end == -1) {
				break;
			}

			String rawPattern = str.substring(start + 1, end);
			// Harmonisation vers la syntaxe exacte de SimpleDateFormat (ex: DD -> dd, YYYY -> yyyy)
			String javaPattern = rawPattern.replace("DD", "dd")
					.replace("dd", "dd")
					.replace("YYYY", "yyyy")
					.replace("yyyy", "yyyy");

			try {
				SimpleDateFormat sdf = new SimpleDateFormat(javaPattern, Locale.getDefault());
				String formatted = sdf.format(date);
				str = str.substring(0, start) + formatted + str.substring(end + 1);
			} catch (Exception e) {
				LOG.err("DiapoParam.getComment() - Format invalide : " + rawPattern, e);
				str = str.substring(0, start) + rawPattern + str.substring(end + 1);
			}
		}

		return str;
	}

	public void updateXml(Xml xml) {
		Element n = (Element) xml.nodeGet("pref");
		if (n == null) {
			n = xml.getDocument().createElement("pref");
			xml.rootGet().appendChild(n);
		}
		mode = Math.max(mode, 0);
		tempo = Math.max(tempo, 0);
		n.setAttribute("mode", mode.toString());
		n.setAttribute("tempo", tempo.toString());
		n.setAttribute("comment", comment);
	}

	public String toXml() {
		StringBuilder b = new StringBuilder("   <pref ");
		b.append("comment=\"").append(comment).append("\" ");
		b.append("mode=\"").append(mode.toString()).append("\" ");
		b.append("tempo=\"").append(tempo.toString()).append("\" ");
		b.append("/>\n");
		return b.toString();
	}

}
