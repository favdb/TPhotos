/*
 * Copyright (C) 2024 favdb
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
package app.export;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 *
 * @author favdb
 */
public class ExportImage {

	private static final String TT = "ExportImage.";

	/**
	 * write an Image to a destination folder
	 *
	 * @param src: source File
	 * @param text: text to insert in the image, may be null or empty
	 * @param dirDest: destination folder
	 * @param outName: name of the out File to produce
	 * @param compression: compress the image
	 *
	 * @return: the writed out File
	 *
	 * @throws Exception
	 */
	public static File writeTo(File src,
			String text,
			File dirDest,
			String outName,
			float compression) throws Exception {
		BufferedImage originalImage = ImageIO.read(src);
		Dimension newDim = new Dimension(originalImage.getWidth(), originalImage.getHeight());
		if (text != null && !text.isEmpty()) {
			// redimension standard en 1280x720
			newDim = getNewDim(newDim, new Dimension(1280, 720));
		}
		Image scaledImage = originalImage.getScaledInstance(newDim.width, newDim.height, Image.SCALE_SMOOTH);
		BufferedImage resizedImage = new BufferedImage(newDim.width, newDim.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = resizedImage.createGraphics();
		g2d.drawImage(scaledImage, 0, 0, null);
		insertText(text, g2d, newDim);
		g2d.dispose();
		File outputFile = new File(dirDest, outName);
		outputFile.delete();
		compressImage(resizedImage, outputFile, compression);
		return outputFile;
	}

	/**
	 * get the new dimension for the image
	 *
	 * @param imgSz: original size of the image
	 * @param newSz: new size
	 *
	 * @return
	 */
	private static Dimension getNewDim(Dimension imgSz, Dimension newSz) {
		double width = newSz.getWidth() / imgSz.getWidth();
		double height = newSz.getHeight() / imgSz.getHeight();
		double ratio = Math.min(width, height);
		return new Dimension((int) (imgSz.width * ratio), (int) (imgSz.height * ratio));
	}

	/**
	 * insert text into the image
	 *
	 * @param text: String text to insert
	 * @param g2d: graphics of the destination image
	 * @param dim: dimension of the image
	 */
	private static void insertText(String text, Graphics2D g2d, Dimension dim) {
		if (text != null && !text.isEmpty()) {
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setFont(new Font("Arial", Font.PLAIN, 20));
			FontMetrics fontMetrics = g2d.getFontMetrics();
			int textW = fontMetrics.stringWidth(text), textH = fontMetrics.getHeight();
			int padding = 10, rectHeight = textH + padding * 2;
			g2d.setColor(Color.BLACK);
			g2d.fillRect(0, dim.height - rectHeight, dim.width, rectHeight);
			g2d.setColor(Color.WHITE);
			int x = (dim.width - textW) / 2,
					y = dim.height - padding - fontMetrics.getDescent();
			g2d.drawString(text, x, y);
		}
	}

	/**
	 * compress and wite the image
	 *
	 * @param image: buffered image to compress
	 * @param outfile: destination File
	 * @param compress: ratio of the compression, between 0 and 1
	 *
	 * @throws IOException
	 */
	private static void compressImage(BufferedImage image, File outfile, float compress) throws IOException {
		//LOG.trace(TT + "compressImage(image, outfile, " + String.format("%f", compress) + ")");
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
		ImageWriter writer = writers.next();
		try (ImageOutputStream outputStream = ImageIO.createImageOutputStream(outfile)) {
			writer.setOutput(outputStream);
			ImageWriteParam params = writer.getDefaultWriteParam();
			if (compress > 0f) {
				params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				params.setCompressionQuality(compress); // Compression entre 0.5 (forte) et 0.75 (faible)
			}
			writer.write(null, new IIOImage(image, null, null), params);
		}
		writer.dispose();
	}
}
