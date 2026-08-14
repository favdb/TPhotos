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
package app.ui.dialog;

import api.mig.MIG;
import api.mig.swing.MigLayout;
import api.shef.SHEF;
import api.shef.editor.HTMLEditorPane;
import app.App;
import app.i18n.I18N;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import app.resources.icons.ICONS;
import app.tools.Ui;
import app.tools.file.EnvUtil;

/**
 *
 * @author favdb
 */
public class SherpaDlg extends JDialog {

	private static final String TT = "SherpaDialog.";
	private String text;
	private HTMLEditorPane editor;
	private boolean validate = false;
	private final String[] stitle;

	public SherpaDlg(Frame parentFrame, String text, String... title) {
		super(parentFrame, true);
		this.text = text;
		this.stitle = title;
		initialize();
	}

	private void initialize() {
		SHEF.scaleAuto();
		this.setFont(App.fontGet());
		setLayout(new MigLayout(MIG.get(MIG.FILL, MIG.WRAP1)));
		this.setTitle(I18N.getMsg("print.text_edit"));
		if (stitle != null && stitle.length > 0) {
			this.setTitle(I18N.getMsg(stitle[0]));
		}
		this.setPreferredSize(new Dimension(940, 480));
		add(editor = new HTMLEditorPane(), MIG.GROW);
		editor.setPreferredSize(new Dimension(1024, 480));
		editor.setFont(App.fontGet());
		editor.setText(mediasToImg(text));
		editor.setButtonsVisible("link, image, table", false);
		JPanel pok = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.INS0)));
		pok.add(Ui.initButton("ask.ok", ICONS.K.OK, e -> doOK()));
		pok.add(Ui.initButton("ask.cancel", ICONS.K.CANCEL, e -> {
			dispose();
		}));
		add(pok, MIG.get(MIG.SPAN, MIG.RIGHT));
		this.pack();
		for (Component c : editor.getComponents()) {
			c.setFont(App.fontGet());
		}
		this.setLocationRelativeTo(getParent());
		this.setVisible(true);
	}

	public void setHtmlContent(String text) {
		//LOG.trace(TT + "setHtmlContent(text=" + text + ")");
		editor.setText(mediasToImg(text));
	}

	public boolean isSaved() {
		//LOG.trace(TT + "isSaved()");
		return true;
	}

	public String getHtmlContent() {
		//LOG.trace(TT + "getHtmlContent()");
		//todo convert/copy image to local /.TPhotos/medias
		return imgToMedias(editor.getText());
	}

	private void doOK() {
		validate = true;
		dispose();
	}

	public String imgToMedias(String html) {
		if (html == null || html.trim().isEmpty()) {
			return html;
		}
		Document doc = Jsoup.parse(html);
		Elements imgs = doc.select("img");
		if (imgs.isEmpty()) {
			return html;
		}
		File mediasDir = new File(EnvUtil.getPrefDir(), "medias");
		if (!mediasDir.exists()) {
			mediasDir.mkdirs();
		}
		for (Element img : imgs) {
			String src = img.attr("src");
			if (src == null || src.isEmpty() || src.contains(".Tphotos/medias")) {
				continue;
			}
			try {
				String originalName = nomFichierGet(src);
				String jpegName = nomJpegGet(originalName);
				File targetFile = new File(mediasDir, jpegName);
				BufferedImage originalImage = null;
				if (src.startsWith("http://") || src.startsWith("https://")) {
					try (InputStream in = new URL(src).openStream()) {
						originalImage = ImageIO.read(in);
					}
				} else {
					String cleanPath = src.replace("file://", "").replace("file:", "");
					File sourceFile = new File(cleanPath);
					if (sourceFile.exists()) {
						originalImage = ImageIO.read(sourceFile);
					}
				}
				if (originalImage != null) {
					imageToJpegSave(originalImage, targetFile, 128, 128);
					img.attr("src", targetFile.toURI().toString());
				}

			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
		return doc.body().html();
	}

	/**
	 * Extrait le nom du fichier depuis un chemin ou une URL.
	 */
	private String nomFichierGet(String path) {
		String cleanPath = path.contains("?")
				? path.substring(0, path.indexOf("?")) : path;
		return Paths.get(cleanPath).getFileName().toString();
	}

	/**
	 * Remplace l'extension du nom de fichier par .jpg
	 */
	private String nomJpegGet(String fileName) {
		int lastDot = fileName.lastIndexOf('.');
		if (lastDot != -1) {
			return fileName.substring(0, lastDot) + ".jpg";
		}
		return fileName + ".jpg";
	}

	/**
	 * Convertit et sauvegarde une BufferedImage au format JPEG (fond blanc si
	 * transparence).
	 */
	private void imageToJpegSave(BufferedImage srcImage,
			File targetFile, int maxWidth, int maxHeight) throws Exception {
		int origWidth = srcImage.getWidth();
		int origHeight = srcImage.getHeight();

		// Calcul du facteur de redimensionnement (conservation du ratio)
		double widthRatio = (double) maxWidth / origWidth;
		double heightRatio = (double) maxHeight / origHeight;
		double scaleFactor = Math.max(widthRatio, heightRatio);

		int targetWidth = origWidth;
		int targetHeight = origHeight;

		// On ne redimensionne que si l'image dépasse la case du Grid
		if (scaleFactor < 1.0) {
			targetWidth = (int) Math.round(origWidth * scaleFactor);
			targetHeight = (int) Math.round(origHeight * scaleFactor);
		}

		// Création de l'image de destination en RGB (fond blanc pour la transparence)
		BufferedImage jpegImage = new BufferedImage(targetWidth, targetHeight,
				BufferedImage.TYPE_INT_RGB);

		Graphics2D g = jpegImage.createGraphics();
		// Activation de l'anticrénelage et du lissage pour un redimensionnement propre
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, targetWidth, targetHeight);
		g.drawImage(srcImage, 0, 0, targetWidth, targetHeight, null);
		g.dispose();

		ImageIO.write(jpegImage, "jpg", targetFile);
	}

	public String mediasToImg(String html, String... todir) {
		if (html == null || html.trim().isEmpty()) {
			return html;
		}
		Document doc = Jsoup.parse(html);
		Elements imgs = doc.select("img");
		if (imgs.isEmpty()) {
			return html;
		}
		String targetDir = (todir != null && todir.length > 0) ? todir[0] : null;
		for (Element img : imgs) {
			String src = img.attr("src");
			if (targetDir != null && src.contains(".Tphotos/medias/")) {
				String fileName = Paths.get(src.replace("file://", "")
						.replace("file:", "")).getFileName().toString();
				File sourceFile = new File(System.getProperty("user.home"),
						".Tphotos/medias/" + fileName);
				File destFile = new File(targetDir, fileName);
				try {
					if (sourceFile.exists()) {
						Files.copy(sourceFile.toPath(), destFile.toPath(),
								StandardCopyOption.REPLACE_EXISTING);
						img.attr("src", destFile.toURI().toString());
					}
				} catch (IOException e) {
					e.printStackTrace(System.err);
				}
			}
		}
		return doc.body().html();
	}

}
