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
package tools;

import app.App;
import app.album.AlbumTree;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import tools.file.EnvUtil;

/**
 *
 * @author favdb
 */
public class ImageUtil {

	private static final String TT = "ImageUtil.";
	private static final String CACHE_PATH = EnvUtil.getPrefDir() + File.separator + "cache";

	public static ImageIcon getThumb(File sourceFile, int size) {
		File cacheDir = new File(CACHE_PATH);
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		File thumbFile = new File(cacheDir, sourceFile.getName());
		if (!thumbFile.exists() || sourceFile.lastModified() > thumbFile.lastModified()) {
			createThumb(sourceFile, thumbFile, size);
		}
		return new ImageIcon(thumbFile.getAbsolutePath());
	}

	private static void createThumb(File srce, File dest, int size) {
		//LOG.trace(TT + "createThumb(srce=" + srce + ", dest=" + dest + ", size=" + size + ")");
		try {
			BufferedImage srcImg = ImageIO.read(srce);
			if (srcImg == null) {
				return;
			}
			// Calcul des dimensions proportionnelles
			int width = srcImg.getWidth();
			int height = srcImg.getHeight();
			if (width > height) {
				height = (height * size) / width;
				width = size;
			} else {
				width = (width * size) / height;
				height = size;
			}
			BufferedImage thumbImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = thumbImg.createGraphics();
			// Optimisation de la qualité pour Java 8
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.drawImage(srcImg, 0, 0, width, height, null);
			g2.dispose();
			ImageIO.write(thumbImg, "JPG", dest);
		} catch (IOException e) {
			LOG.err("createThumbnail(srce=" + srce + ", dest=" + dest + ", size=" + size + ") error", e);
		}
	}

	/**
	 * Delete orphans thumbnails in cache
	 *
	 * @param srcDir
	 */
	public static void cleanCache(File srcDir) {
		//LOG.trace(TT + "cleanCache(srcDir=" + srcDir + ")");
		File dir = new File(App.preferences.photosDirGet());
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
}
