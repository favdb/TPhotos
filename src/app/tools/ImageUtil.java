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
import app.album.AlbumTree;
import app.media.Jpeg;
import app.resources.icons.ICONS;
import app.resources.icons.IconUtil;
import app.tools.file.EnvUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;

/**
 * tools for images
 *
 * @author favdb
 */
public class ImageUtil {

	private static final String TT = "ImageUtil.";
	private static final String CACHE_PATH
			= EnvUtil.getPrefDir() + File.separator + "cache";

	/**
	 * get an ImageIcon for the given String with given width and height
	 *
	 * @param htmlText
	 * @param width
	 * @param height
	 * @return
	 */
	public static ImageIcon createTextImage(String html, int width, int height) {
		if (width <= 0 || height <= 0) {
			width = 100;
			height = 100;
		}
		return createTextImage(html, new Dimension(width, height));
	}

	/**
	 * get an ImageIcon for the given String with given Dimension
	 *
	 * @param htmlText
	 * @param dim
	 * @return
	 */
	public static ImageIcon createTextImage(String htmlText, Dimension dim) {
		// Correction : inversion largeur/hauteur rectifiée
		BufferedImage image = new BufferedImage(dim.width, dim.height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		try {
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, dim.width, dim.height);
			JEditorPane pane = new JEditorPane();
			pane.setContentType("text/html");
			pane.setText(htmlText);
			pane.setSize(dim.width, dim.height);
			pane.setOpaque(false);
			pane.paint(g2);
		} finally {
			g2.dispose();
		}
		return resizeIcon(new ImageIcon(image), dim);
	}

	/**
	 * get an ImageIcon for the given String with given width
	 *
	 * @param htmlText
	 * @param width
	 * @return
	 */
	public static ImageIcon createTextImage(String htmlText, int width) {
		return createTextImage(htmlText, new Dimension(width, width));
	}

	public static ImageIcon getThumb(File srce, int size) {
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
		try {
			BufferedImage srcImg = ImageIO.read(srce);
			if (srcImg == null) {
				return;
			}
			try {
				Jpeg jpeg = new Jpeg(srce);
				if (jpeg.exif != null) {
					srcImg = orientedImage(srcImg, jpeg.exif.getOrientation());
				}
			} catch (Exception e) {
				// Ignore si pas d'EXIF
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
			// Correction : sécurisation de la libération des ressources de g2
			try {
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING,
						RenderingHints.VALUE_RENDER_QUALITY);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2.drawImage(srcImg, 0, 0, w, h, null);
			} finally {
				g2.dispose();
			}
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
			if (original == null || !original.exists()) {
				if (thumb.delete()) {
					//LOG.log("thumb : " + thumb.getName() + " deleted.");
				}
			}
		}
	}

	/**
	 * find the given image File in the given directory
	 *
	 * @param dir
	 * @param n
	 * @return
	 */
	public static File imgFind(File dir, String n) {
		for (int mode = 0; mode <= 2; mode++) {
			String path = AlbumTree.getSubdir(n, mode);
			File f = new File(dir, path + File.separator + n);
			if (f.exists()) {
				return f;
			}
		}
		return null;
	}

	/**
	 * get the File form the given String
	 *
	 * @param f
	 * @return
	 */
	private static File getImageFile(String f) {
		File fx = new File(f);
		if (!fx.isAbsolute()) {
			fx = new File(App.pref.photosDirGet(), f);
		} else if (!fx.exists()) {
			fx = new File(App.pref.photosDirGet(), fx.getPath());
		}
		return fx;
	}

	/**
	 * get the File located in photos directory
	 *
	 * @param f
	 * @return
	 */
	private static File getImageFile(File f) {
		if (f.exists()) {
			return f;
		}
		return new File(App.pref.photosDirGet(), f.getPath());
	}

	/**
	 * get an ImageIcon for the given filename, adjusted size and optional zoom type
	 *
	 * @param f
	 * @param size
	 * @param zoom
	 * @return
	 */
	public static ImageIcon getImage(String f, int size, int... zoom) {
		File ff = getImageFile(f);
		return getImage(ff, size, zoom);
	}

	/**
	 * get an ImageIcon for the given filename, adjusted Dimension and optional zoom type
	 *
	 * @param f
	 * @param size
	 * @param zoom
	 * @return
	 */
	public static ImageIcon getImage(String f, Dimension size, int... zoom) {
		File ff = getImageFile(f);
		return getImage(ff, size, zoom);
	}

	/**
	 * get an ImageIcon for the given File, adjusted size and optional zoom type
	 *
	 * @param f
	 * @param size
	 * @param zoom
	 * @return
	 */
	public static ImageIcon getImage(File f, int size, int... zoom) {
		return getImage(f, new Dimension(size, size), zoom);
	}

	/**
	 * get an ImageIcon for the given File, adjusted Dimension and optional zoom type
	 *
	 * @param f
	 * @param size
	 * @param zoom : optional zoom type (0=none, 1=adjusted, 2=zoomed)
	 * @return
	 */
	public static ImageIcon getImage(File f, Dimension size, int... zoom) {
		File fx = getImageFile(f);
		ImageIcon img = null;

		if (fx.exists()) {
			try {
				BufferedImage bImg = ImageIO.read(fx);
				if (bImg != null) {
					int orientation = 1;
					try {
						Jpeg jpeg = new Jpeg(fx);
						if (jpeg.exif != null) {
							orientation = jpeg.exif.getOrientation();
						}
					} catch (Exception e) {
						// Ignoré si pas d'EXIF
					}
					bImg = orientedImage(bImg, orientation);
					img = new ImageIcon(bImg);
				}
			} catch (IOException e) {
				LOG.err(TT + "getImage error reading " + fx.getAbsolutePath(), e);
			}
		}
		if (img == null) {
			img = IconUtil.getImageIcon(ICONS.K.UNKNOWN, size.width);
			LOG.err(TT + "getImage(" + f.getAbsolutePath() + ", size=" + size
					+ ") " + fx.getAbsolutePath() + " not exists or unreadable");
		}
		return resizeIcon(img, size, zoom);
	}

	/**
	 * resize the given ImageIcon to the give size with optional zoom adjustement
	 *
	 * @param icon
	 * @param size
	 * @param zoom : optional zoom type (0=none, 1=adjusted, 2=zoomed)
	 * @return
	 */
	public static ImageIcon resizeIcon(ImageIcon icon, int size, int... zoom) {
		return resizeIcon(icon, new Dimension(size, size), zoom);
	}

	/**
	 * resize the given ImageIcon to the give Dimension with optional zoom adjustement
	 *
	 * @param icon
	 * @param dim the target Dimension
	 * @param zoom : optional zoom type (0=none, 1=adjusted, 2=zoomed)
	 * @return
	 */
	public static ImageIcon resizeIcon(ImageIcon icon, Dimension dim, int... zoom) {
		if (icon == null || dim == null || dim.width <= 0 || dim.height <= 0) {
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
		if (zoomMode == 0) {
			return icon;
		}
		double wRatio = (double) dim.width / oWidth;
		double hRatio = (double) dim.height / oHeight;
		double ratio;
		if (zoomMode == 1) {
			ratio = Math.min(wRatio, hRatio);
		} else if (zoomMode == 2) {
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
		int canvasW = (zoomMode == 1) ? drawW : dim.width;
		int canvasH = (zoomMode == 1) ? drawH : dim.height;
		int drawX = (canvasW - drawW) / 2;
		int drawY = (canvasH - drawH) / 2;
		BufferedImage resultImage = new BufferedImage(canvasW, canvasH,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resultImage.createGraphics();
		try {
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.drawImage(icon.getImage(), drawX, drawY, drawW, drawH, null);
		} finally {
			g2.dispose();
		}
		return new ImageIcon(resultImage);
	}

	/**
	 * get the necessary rotation from EXIF tag (1 à 8).
	 */
	public static BufferedImage orientedImage(BufferedImage src, int orientationExif) {
		switch (orientationExif) {
			case 6:
				return rotate(src, 90);
			case 3:
				return rotate(src, 180);
			case 8:
				return rotate(src, 270);
			case 1:
			default:
				return src; // Aucune modification
		}
	}

	/**
	 * rotate the given BufferedImage.
	 */
	public static BufferedImage rotate(BufferedImage src, int angle) {
		int w = src.getWidth();
		int h = src.getHeight();
		boolean swapDims = (angle == 90 || angle == 270);
		int newW = swapDims ? h : w;
		int newH = swapDims ? w : h;
		int type = (src.getType() == BufferedImage.TYPE_CUSTOM)
				? BufferedImage.TYPE_INT_ARGB : src.getType();
		BufferedImage dest = new BufferedImage(newW, newH, type);
		Graphics2D g2d = dest.createGraphics();
		try {
			AffineTransform at = new AffineTransform();
			at.translate(newW / 2.0, newH / 2.0);
			at.rotate(Math.toRadians(angle));
			at.translate(-w / 2.0, -h / 2.0);
			g2d.setTransform(at);
			g2d.drawImage(src, 0, 0, null);
		} finally {
			g2d.dispose();
		}
		return dest;
	}

}
