package app.print;

import app.App;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import tools.LOG;

/**
 * Handles rendering of individual cells onto a Graphics2D surface.
 *
 * @author favdb
 */
public class GridCellPrint {

	private static final String TT = "GridCellPrint.";

	/**
	 * dtaw the given PrintCell
	 *
	 * @param g2d
	 * @param cell
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public static void drawCell(Graphics2D g2d,
			PrintCell cell, int x, int y, int w, int h) {
		if (cell == null || cell.isEmpty()) {
			return;
		}
		Shape oldClip = g2d.getClip();
		g2d.setClip(x, y, w, h);
		try {
			if (cell.isPhoto()) {
				drawPhoto(g2d, cell, x, y, w, h);
			} else if (cell.isText()) {
				drawText(g2d, cell, x, y, w, h);
			}
		} catch (Exception e) {
			LOG.err(TT + "Error rendering cell " + cell.idGet(), e);
		} finally {
			g2d.setClip(oldClip);
		}
	}

	/**
	 * draw the given PrintCell
	 *
	 * @param g2d
	 * @param cell
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	private static void drawPhoto(Graphics2D g2d,
			PrintCell cell, int x, int y, int w, int h) {
		String filePath = cell.photoFileGet();
		if (filePath == null || filePath.isEmpty()) {
			return;
		}
		File imgFile = new File(App.preferences.photosDirGet(), filePath);
		if (!imgFile.exists()) {
			imgFile = new File(filePath);
		}
		if (!imgFile.exists()) {
			return;
		}
		try {
			BufferedImage img = ImageIO.read(imgFile);
			if (img == null) {
				return;
			}
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			double imgW = img.getWidth();
			double imgH = img.getHeight();
			double scale = Math.max((double) w / imgW, (double) h / imgH);
			int drawW = (int) (imgW * scale);
			int drawH = (int) (imgH * scale);
			int drawX = x + (w - drawW) / 2;
			int drawY = y + (h - drawH) / 2;
			g2d.drawImage(img, drawX, drawY, drawW, drawH, null);
		} catch (IOException e) {
			LOG.err(TT + "Error loading photo: " + filePath, e);
		}
	}

	/**
	 * draw the give PrintCell as HTML text
	 *
	 * @param g2d
	 * @param cell
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	private static void drawText(Graphics2D g2d,
			PrintCell cell, int x, int y, int w, int h) {
		String textContent = cell.textGet();
		if (textContent == null || textContent.isEmpty()) {
			return;
		}
		JLabel label = new JLabel("<html><body>" + textContent + "</body></html>");
		label.setSize(w, h);
		label.setOpaque(false);
		Graphics2D g2dSub = (Graphics2D) g2d.create(x, y, w, h);
		label.paint(g2dSub);
		g2dSub.dispose();
	}

}
