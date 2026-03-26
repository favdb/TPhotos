/*
 * Copyright (C) 2020 favdb
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
package resources.icons;

import app.App;
import i18n.I18N;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import resources.MainResources;
import tools.LOG;
import tools.file.EnvUtil;

/**
 * icons utilities
 *
 * @author favdb
 */
public class IconUtil {

	/**
	 * get the image of the given icon, with the give size
	 *
	 * @param k
	 * @param sz
	 * @return
	 */
	public static Image getImage(ICONS.K k, int sz) {
		Icon icon = getIconLarge(k, sz);
		int w = icon.getIconWidth();
		int h = icon.getIconHeight();
		GraphicsEnvironment ge
				= GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferedImage image = gc.createCompatibleImage(w, h);
		Graphics2D g = image.createGraphics();
		icon.paintIcon(null, g, 0, 0);
		g.dispose();
		return image;
	}

	/**
	 * utility class
	 */
	private IconUtil() {
		// empty
	}

	private static final String DIR = "icons/",
			MINI = "mini/",
			SMALL = "png/",
			LARGE = "png/";

	public enum STATE {
		EMPTY(SMALL + "empty"),
		ERROR(SMALL + "error"),
		OK(SMALL + "ok"),
		UNKNOWN(SMALL + "unknown"),
		WARNING(SMALL + "warning"),
		INFO(SMALL + "info");
		final private Icon icon;

		private STATE(String iconKey) {
			this.icon = getImageIcon(iconKey, new Dimension(getDefSize(), getDefSize()));
		}

		public Icon getIcon() {
			return icon;
		}

	}

	private static int defSize = 16;

	/**
	 * set the default icon size
	 */
	public static void setDefSize() {
		defSize = (int) (App.fontGet().getSize() * 1.5);
	}

	/**
	 * get the default icon size
	 *
	 * @return
	 */
	public static int getDefSize() {
		return defSize;
	}

	/**
	 * get the default icon dimension
	 *
	 * @return
	 */
	public static Dimension getDefDim() {
		return new Dimension(defSize, defSize);
	}

	/**
	 * trace for a null icon
	 *
	 * @param path
	 */
	private static void iconNull(String path) {
		LOG.err("icon " + path + " not found");
	}

	/**
	 * get an mini Icon
	 *
	 * @param key
	 * @return
	 */
	public static Icon getIconMini(String key) {
		return getIcon(MINI + key);
	}

	/**
	 * get a standard small Icon for the given icon KEY
	 *
	 * @param key
	 * @return
	 */
	public static Icon getIconSmall(ICONS.K key) {
		return getIcon(SMALL + key.toString(), getDefSize());
	}

	/**
	 * get a standard small Icon for the given icon name
	 *
	 * @param key
	 * @return
	 */
	public static Icon getIconSmall(String key) {
		return getIcon(SMALL + key, defSize);
	}

	/**
	 * get a standard large Icon for the given icon name
	 *
	 * @param key
	 * @return
	 */
	public static Icon getIconLarge(String subpath, String key) {
		String path = subpath + key.toLowerCase().replace(".", "/") + ".png";
		String p2 = (EnvUtil.getUserDir()
				+ File.separator + "resources" + File.separator + path).replace("/", File.separator);
		File file = new File(p2);
		if (file.exists()) {
			return (new ImageIcon(p2));
		}
		Class<?> x = MainResources.class;
		if (x == null) {
			LOG.err("MainResources.class is null");
			return STATE.UNKNOWN.icon;
		}
		p2 = ("resources" + "/" + subpath + "/" + key + ".png");
		ImageIcon icon = createImageIcon(x, p2);
		if (icon == null) {
			iconNull(path);
			return STATE.UNKNOWN.icon;
		}
		return icon;
	}

	/**
	 * get a standard large Icon for the given icon KEY
	 *
	 * @param key
	 * @return
	 */
	public static Icon getIconLarge(ICONS.K key) {
		return getIcon(LARGE + key.toString(), getDefSize() * 2);
	}

	/**
	 * get a standard large Icon for the given icon KEY and size
	 *
	 * @param key
	 * @return
	 */
	public static Icon getIconLarge(ICONS.K key, int size) {
		Icon ic = getIcon(LARGE + key.toString());
		return resizeIcon((ImageIcon) ic, size);
	}

	/**
	 * get a standard Icon for the given icon name
	 *
	 * @param key
	 * @return
	 */
	public static Icon getIcon(String key) {
		String path = DIR + key.toLowerCase().replace(".", "/") + ".png";
		if (key.endsWith(".jpg") || key.endsWith(".jpeg") || key.endsWith(".gif")) {
			path = DIR + key.toLowerCase();
		}
		String p2 = (EnvUtil.getUserDir()
				+ File.separator + "resources" + File.separator + path).replace("/", File.separator);
		File file = new File(p2);
		if (file.exists()) {
			return (new ImageIcon(p2));
		}
		Class<?> x = MainResources.class;
		if (x == null) {
			LOG.err("MainResources.class is null");
			return STATE.UNKNOWN.icon;
		}
		ImageIcon icon = createImageIcon(x, path);
		if (icon == null) {
			iconNull(path);
			return STATE.UNKNOWN.icon;
		}
		return icon;
	}

	/**
	 * get a standard Icon for the given icon KEY and size
	 *
	 * @param key
	 * @return
	 */
	public static Icon getIcon(ICONS.K key, int size) {
		if (size == 0) {
			return (getIcon(key.toString()));
		}
		return (getIcon(key.toString(), size, size));
	}

	/**
	 * get a standard Icon for the given icon name and size
	 *
	 * @param key
	 * @return
	 */
	public static Icon getIcon(String key, int size) {
		Icon ic = getIcon(key);
		if (size == 0) {
			return ic;
		}
		ImageIcon img = getImageIcon(key);
		return resizeIcon(img, size);
	}

	/**
	 * get a standard Icon for the given icon name and dimension
	 *
	 * @param key
	 * @return
	 */
	public static Icon getIcon(String key, Dimension size) {
		return (getIcon(key, size.width, size.height));
	}

	/**
	 * get a standard Icon for the given icon name and width and height
	 *
	 * @param key
	 * @return
	 */
	public static Icon getIcon(String key, int width, int height) {
		Class<?> x = MainResources.class;
		String path = DIR + key.toLowerCase().replace(".", "/") + ".png";
		if (x == null) {
			return STATE.UNKNOWN.icon;
		}
		ImageIcon icon = createImageIcon(x, path);
		if (icon == null) {
			iconNull(path);
			return STATE.UNKNOWN.icon;
		}
		return (resizeIcon(icon, height, width));
	}

	/**
	 * get a standard Icon link for the given icon KEY and size
	 *
	 * @param key
	 * @return
	 */
	public static String getIconLink(ICONS.K key) {
		Class<?> x = MainResources.class;
		String path = x.getCanonicalName() + DIR + key.toString().replace(".", "/") + ".png";
		File f = new File(path);
		if (!f.exists()) {
			path = DIR + "unknown.png";
		}
		return path;
	}

	public static Icon getJpegIcon(String subpath, String key) {
		String path = subpath + "/" + key.toLowerCase().replace(".", "/") + ".jpeg";
		return getJpeg(path, defSize * 4);
	}

	public static Icon getJpegIcon(String key) {
		String path = DIR + key.toLowerCase().replace(".", "/") + ".jpeg";
		return getJpeg(path, defSize * 4);
	}

	public static Icon getJpeg(String path, int z) {
		Class<?> x = MainResources.class;
		if (x == null) {
			return STATE.UNKNOWN.icon;
		}
		ImageIcon icon = createImageIcon(x, path);
		icon = resizeIcon(icon, z);
		if (icon == null) {
			LOG.err("icon " + path + " not found");
			return STATE.EMPTY.icon;
		}
		return icon;
	}

	public static Dimension getDimension(String key) {
		Icon icon = getIcon(key);
		Dimension dim = new Dimension(icon.getIconWidth(), icon.getIconHeight());
		return (dim);
	}

	public static Dimension getDimension(ICONS.K key) {
		Icon icon = getIcon("png/" + key.toString());
		Dimension dim = new Dimension(icon.getIconWidth(), icon.getIconHeight());
		return (dim);
	}

	public static ImageIcon getImageIcon(String resourceKey) {
		return (getImageIcon(resourceKey, null));
	}

	public static ImageIcon getImageIcon(String key, Dimension size) {
		if (size != null) {
			return ((ImageIcon) getIcon(key, size.width, size.height));
		}
		return ((ImageIcon) getIcon(key));
	}

	public static ImageIcon resizeIcon(ImageIcon icon, int height, int width) {
		Dimension size = new Dimension(height, width);
		return (resizeIcon(icon, size));
	}

	public static ImageIcon resizeIcon(ImageIcon icon, Dimension size) {
		ImageIcon rs = icon;
		if (size.height == 0) {
			return (icon);
		}
		if ((icon.getIconHeight() != size.height) || (icon.getIconWidth() != size.width)) {
			double imageHeight = icon.getIconHeight();
			double imageWidth = icon.getIconWidth();
			int sw = size.width, sh = size.height;

			if (imageHeight / size.height > imageWidth / size.width) {
				sw = (int) (sh * imageWidth / imageHeight);
			} else {
				sh = (int) (sw * imageHeight / imageWidth);
			}
			Image imageicon = icon.getImage().getScaledInstance(sw, sh, Image.SCALE_DEFAULT);
			rs = new ImageIcon(imageicon);
		}
		return (rs);
	}

	public static ImageIcon resizeIcon(ImageIcon icon, int size) {
		ImageIcon rs = icon;
		if (size == 0) {
			return (icon);
		}
		if (icon.getIconHeight() != size) {
			double imageHeight = icon.getIconHeight();
			double imageWidth = icon.getIconWidth();
			Double sh = imageWidth * (size / imageHeight);
			Image imageicon = icon.getImage().getScaledInstance(sh.intValue(), size, Image.SCALE_DEFAULT);
			rs = new ImageIcon(imageicon);
		}
		return rs;
	}

	public static ImageIcon createImageIcon(Class<?> c, String path) {
		//App.trace("createImageIcon from " + path);
		URL imgURL = c.getResource(path);
		if (imgURL != null) {
			return (new ImageIcon(imgURL));
		} else {
			LOG.err("Couldn't find file: " + path);
			return (ImageIcon) STATE.UNKNOWN.icon;
		}
	}

	public static Image getIconImage(String key) {
		return ((ImageIcon) getIcon(key)).getImage();
	}

	public static Image getIconImageSmall(String key) {
		return ((ImageIcon) getIcon(SMALL + key)).getImage();
	}

	public static Image getIconImageSmall(ICONS.K key) {
		return ((ImageIcon) getIcon(SMALL + (key.toString()))).getImage();
	}

	public static Icon getIconExternal(String filename, Dimension size) {
		ImageIcon img = new ImageIcon(filename);
		img = resizeIcon(img, size);
		return img;
	}

	public static Dimension getImageDimension(File file) {
		try {
			BufferedImage bimg = ImageIO.read(file);
			return new Dimension(bimg.getWidth(), bimg.getHeight());
		} catch (IOException ex) {
			return null;
		}
	}

	public static ImageIcon getImageIcon(ICONS.K k, int zoom) {
		ImageIcon img = getImageIcon(LARGE + k.toString());
		return resizeIcon(img, zoom);
	}

	public static Icon getJpegIconFromFile(File file, int sz) {
		ImageIcon img = getJpegIconFromFile(file, new Dimension(0, 0));
		return resizeIcon(img, sz);
	}

	public static ImageIcon getJpegIconFromFile(File file, Dimension sz) {
		ImageIcon icon = getImageIcon(ICONS.K.UNKNOWN, sz.width);
		try {
			BufferedImage img = ImageIO.read(file);
			if (img != null) {
				icon = new ImageIcon(img);
			}
		} catch (IOException ex) {
			LOG.err("icon " + file.getAbsolutePath() + " not found");
		}
		return resizeIcon(icon, sz);
	}

	public static JButton getIconButton(ICONS.K k, String... toolstips) {
		JButton btn = new JButton(getIconSmall(k));
		btn.setName("bt" + k.toString());
		btn.setPreferredSize(getDefDim());
		if (toolstips != null && toolstips.length > 0) {
			btn.setToolTipText(I18N.getMsg(toolstips[0]));
		}
		return btn;
	}

}
