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
package app.tools;

import app.App;
import app.resources.icons.ICONS;
import app.resources.icons.IconUtil;
import app.tools.file.EnvUtil;
import app.ui.MainFrame;
import app.ui.album.AlbumTree;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;

/**
 * tools for images
 *
 * @author favdb
 */
public class ImageUtil {

	private static final String TT = "ImageUtil.";
	private static final String CACHE_PATH
			= EnvUtil.getPrefDir() + File.separator + "cache";

	public static ImageIcon createTextImage(String html, int width, int height) {
		if (width <= 0 || height <= 0) {
			width = 100;
			height = 100;
		}
		return createTextImage(html, new Dimension(width, height));
	}

	public static ImageIcon createTextImage(String htmlText, Dimension dim) {
		BufferedImage image = new BufferedImage(dim.height, dim.width,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		try {
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, dim.height, dim.width);
			JEditorPane pane = new JEditorPane();
			pane.setContentType("text/html");
			pane.setText(htmlText);
			pane.setSize(dim.height, dim.width);
			pane.setOpaque(false);
			pane.paint(g2);

		} finally {
			g2.dispose();
		}
		return resizeIcon(new ImageIcon(image), dim);
	}

	public static ImageIcon createTextImage(String htmlText, int width) {
		/*LOG.trace(TT + "createTextImage("
				+ "htmlText=" + htmlText + ", width=" + width + ",  height=" + height + ")");*/
		BufferedImage image = new BufferedImage(width, width,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		try {
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, width, width);
			JEditorPane pane = new JEditorPane();
			pane.setContentType("text/html");
			pane.setText(htmlText);
			pane.setSize(width, width);
			pane.setOpaque(false);
			pane.paint(g2);

		} finally {
			g2.dispose();
		}
		return resizeIcon(new ImageIcon(image), width);
	}

	public static ImageIcon getThumb(File srce, int size) {
		//LOG.trace(TT + "getThumb(srce=" + srce.getAbsolutePath() + ", size=" + size);
		File cacheDir = new File(CACHE_PATH);
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		File thumb = new File(cacheDir, srce.getName());
		if (!thumb.exists() || srce.lastModified() > thumb.lastModified()) {
			createThumb(srce, thumb, size);
		}
		return new ImageIcon(thumb.getAbsolutePath());
	}

	private static void createThumb(File srce, File dest, int size) {
		//LOG.trace(TT + "createThumb(srce=" + srce + ", dest=" + dest + ", size=" + size + ")");
		try {
			BufferedImage srcImg = ImageIO.read(srce);
			if (srcImg == null) {
				return;
			}
			int w = srcImg.getWidth();
			int h = srcImg.getHeight();
			if (w > h) {
				h = (h * size) / w;
				w = size;
			} else {
				w = (w * size) / h;
				h = size;
			}
			BufferedImage thumbImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = thumbImg.createGraphics();
			// Optimisation de la qualité pour Java 8
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.drawImage(srcImg, 0, 0, w, h, null);
			g2.dispose();
			ImageIO.write(thumbImg, "JPG", dest);
		} catch (IOException e) {
			LOG.err("createThumbnail(srce=" + srce
					+ ", dest=" + dest
					+ ", size=" + size
					+ ") error", e);
		}
	}

	/**
	 * Delete orphans thumbnails in cache
	 *
	 * @param srcDir
	 */
	public static void cleanCache(File srcDir) {
		//LOG.trace(TT + "cleanCache(srcDir=" + srcDir + ")");
		File dir = new File(App.pref.photosDirGet());
		File cacheDir = new File(CACHE_PATH);
		if (!cacheDir.exists() || !cacheDir.isDirectory()) {
			return;
		}
		File[] cachedFiles = cacheDir.listFiles();
		if (cachedFiles == null) {
			return;
		}
		for (File thumb : cachedFiles) {
			File original = imgFind(dir, thumb.getName());
			if (original != null && !original.exists()) {
				if (thumb.delete()) {
					LOG.log("thumb : " + thumb.getName() + " deleted.");
				}
			}
		}
	}

	public static File imgFind(File dir, String n) {
		//LOG.trace(TT + "imgFind(srcDir, n=" + n + ")");
		for (int mode = 0; mode <= 2; mode++) {
			String path = AlbumTree.getSubdir(n, mode);
			File f = new File(dir, path + File.separator + n);
			if (f.exists()) {
				return f;
			}
		}
		return null;
	}

	private static File getImageFile(String f) {
		File fx = new File(f);
		if (!fx.isAbsolute()) {
			fx = new File(App.pref.photosDirGet(), f);
		} else if (!fx.exists()) {
			fx = new File(App.pref.photosDirGet(), fx.getPath());
		}
		return fx;
	}

	private static File getImageFile(File f) {
		if (f.exists()) {
			return f;
		}
		return new File(App.pref.photosDirGet(), f.getPath());
	}

	public static ImageIcon getImage(String f, int size, int... zoom) {
		File ff = getImageFile(f);
		return getImage(ff, size, zoom);
	}

	public static ImageIcon getImage(String f, Dimension size, int... zoom) {
		//LOG.trace(TT + "getImage(file=" + f + ", size dim=" + size.toString() + ")");
		File ff = getImageFile(f);
		return getImage(ff, size, zoom);
	}

	public static ImageIcon getImage(File f, int size, int... zoom) {
		//LOG.trace(TT + "getImage(file=" + f.getAbsolutePath() + ", size=" + size + ")");
		File fx = getImageFile(f);
		ImageIcon img;
		if (fx.exists()) {
			img = new ImageIcon(fx.getAbsolutePath());
		} else {
			img = IconUtil.getImageIcon(ICONS.K.UNKNOWN, size);
			LOG.err(TT + "getImage(" + f.getAbsolutePath() + ", size=" + size
					+ ") " + fx.getAbsolutePath() + " not exists");
		}
		return resizeIcon(img, size, zoom);
	}

	public static ImageIcon getImage(File f, Dimension size, int... zoom) {
		//LOG.trace(TT + "getImage(file=" + f.getAbsolutePath() + ", size=" + size.toString() + ")");
		File fx = getImageFile(f);
		ImageIcon img;
		if (fx.exists()) {
			img = new ImageIcon(f.getAbsolutePath());
		} else {
			img = IconUtil.getImageIcon(ICONS.K.UNKNOWN, size.width);
			LOG.err(TT + "getImage(" + f.getAbsolutePath() + ", size=" + size.toString()
					+ ") " + fx.getAbsolutePath() + " not exists");
		}
		return resizeIcon(img, size, zoom);
	}

	public static ImageIcon resizeIcon(ImageIcon icon, int size, int... zoom) {
		return resizeIcon(icon, new Dimension(size, size), zoom);
	}

	public static ImageIcon resizeIcon(ImageIcon icon, Dimension target, int... zoom) {
		if (icon == null || target == null || target.width <= 0 || target.height <= 0) {
			return icon;
		}
		if (icon.getImageLoadStatus() == MediaTracker.LOADING || icon.getIconWidth() == -1) {
			icon.setImage(icon.getImage());
		}

		int oWidth = icon.getIconWidth();
		int oHeight = icon.getIconHeight();
		if (oWidth <= 0 || oHeight <= 0) {
			return icon;
		}

		int zoomMode = (zoom != null && zoom.length > 0) ? zoom[0] : 0;

		// Mode 0 : Aucune adaptation (image rendue telle quelle)
		if (zoomMode == 0) {
			return icon;
		}

		double wRatio = (double) target.width / oWidth;
		double hRatio = (double) target.height / oHeight;
		double ratio;

		if (zoomMode == 1) {
			// Mode 1 : Ajustement à l'intérieur (contain) - l'image occupe le max sans être tronquée
			ratio = Math.min(wRatio, hRatio);
		} else if (zoomMode == 2) {
			// Mode 2 : Remplissage complet (cover) - agrandissement max, peut être tronquée
			ratio = Math.max(wRatio, hRatio);
		} else {
			return icon;
		}

		int drawW = (int) Math.round(oWidth * ratio);
		int drawH = (int) Math.round(oHeight * ratio);

		if (drawW <= 0) {
			drawW = 1;
		}
		if (drawH <= 0) {
			drawH = 1;
		}

		// Pour le mode 1, le canvas correspond à la taille redimensionnée de l'image
		// Pour le mode 2, le canvas est fixe (target) pour rogner le surplus
		int canvasW = (zoomMode == 1) ? drawW : target.width;
		int canvasH = (zoomMode == 1) ? drawH : target.height;

		int drawX = (canvasW - drawW) / 2;
		int drawY = (canvasH - drawH) / 2;

		BufferedImage resultImage = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resultImage.createGraphics();
		try {
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2.drawImage(icon.getImage(), drawX, drawY, drawW, drawH, null);
		} finally {
			g2.dispose();
		}

		return new ImageIcon(resultImage);
	}

	public static void showPhoto(MainFrame parent, File file) {
		//LOG.trace(TT + "showPhoto(parent, file=" + file + ")");
		try {
			ImageIcon img = getImage(file, App.mainFrame.getSize().height);
			JDialog dlg = new JDialog(parent,
					"Aperçu de la photo " + getImageFile(file));
			dlg.setModal(true);
			dlg.add(new JLabel(img));
			dlg.pack();
			dlg.setLocationRelativeTo(parent);
			dlg.setVisible(true);
			//todo ajout la possibilité de "naviguer" (next/previux)
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
