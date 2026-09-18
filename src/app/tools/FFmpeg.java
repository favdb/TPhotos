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
package app.tools;

import app.export.Export;
import app.i18n.I18N;
import app.tools.file.FileUtil;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;

/**
 * check if FFmpeg is installed
 *
 * @author favdb
 */
public class FFmpeg {

	private static final String TT = "FFmpeg.";

	/**
	 * check if FFmpeg is installed
	 *
	 * @return
	 */
	public static boolean isInstalled() {
		try {
			Process process = new ProcessBuilder("ffmpeg", "-version").start();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			String line;
			StringBuilder output = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
			reader.close();
			if (output.toString().toLowerCase().contains("ffmpeg version")) {
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * main for testing if FFmpeg is installed
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		if (isInstalled()) {
			System.out.println("FFmpeg est installé sur ce système.");
		} else {
			System.out.println("FFmpeg n'est pas installé sur ce système.");
		}
	}

	/**
	 * start create a MP4 file with FFmpeg
	 *
	 * @param export
	 * @param dir
	 * @param outfile
	 * @throws IOException
	 */
	public void start(Export export, File dir, String outfile) throws IOException {
		//LOG.trace(TT + "start(export, dir=" + dir.getAbsolutePath() + ", outfile=" + outfile + ")");
		File file = new File(export.dirDest,
				String.format("%04d.jpg", export.copyDlg.getNumber() + 1));
		end(file, I18N.getMsg("export.end"));
		String command = String.format(
				"ffmpeg "
				+ "-framerate 1/" + export.tempo + " "
				+ "-pattern_type glob "
				+ "-i '%s/*.jpg' "
				+ "-c:v libx264 "
				+ "-crf 28 "
				+ "-preset slow "
				+ "-r 15 "
				+ "-vf \""
				+ "scale=1280:720:force_original_aspect_ratio=decrease,"
				+ "pad=1280:720:(ow-iw)/2:(oh-ih)/2"
				+ "\" "
				+ "-pix_fmt yuv420p %s -y",
				export.dirDest.getAbsolutePath(), outfile);
		export.taInfosAdd("<p>" + I18N.getMsg("export.format.mpeg_make") + " ... ");
		ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
		Process process = processBuilder.start();
		try {
			process.waitFor();
			FileUtil.dirDelete(export.dirDest);
			export.taInfosAdd(I18N.getMsg("task.ok") + "</p>");
			export.btExec.setEnabled(true);
		} catch (InterruptedException e) {
			LOG.err(TT + "start(...) process call error\n", e);
			export.taInfosAdd(I18N.getMsg("task.error", e.getLocalizedMessage()) + "</p>");
			export.btExec.setEnabled(true);
		}
	}

	/**
	 * create first image (album title)
	 */
	public static void begin(Export export) {
		try {
			int width = 800, height = 600;
			String text = export.getMainFrame().diapoTitleGet();
			BufferedImage bufferedImage = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = bufferedImage.createGraphics();
			g2d.setColor(Color.BLACK);
			g2d.fillRect(0, 0, width, height);
			g2d.setColor(Color.WHITE);
			g2d.setFont(new Font("Arial", Font.BOLD, 24));
			FontMetrics fontMetrics = g2d.getFontMetrics();
			int textWidth = fontMetrics.stringWidth(text),
					textHeight = fontMetrics.getAscent();
			int x = (width - textWidth) / 2, y = (height + textHeight) / 2;
			g2d.drawString(text, x, y);
			g2d.dispose();
			ImageIO.write(bufferedImage, "jpg", new File(export.dirDest, "0000.jpg"));
		} catch (IOException ex) {
			LOG.err(TT + "begin() error", ex);
		}
	}

	/**
	 * create a "end" image
	 *
	 * @param output
	 * @param text
	 * @throws IOException
	 */
	public void end(File output, String text) throws IOException {
		int width = 800, height = 600;
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bufferedImage.createGraphics();
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, width, height);
		g2d.setColor(Color.WHITE);
		g2d.setFont(new Font("Arial", Font.BOLD, 24));
		FontMetrics fontMetrics = g2d.getFontMetrics();
		int textWidth = fontMetrics.stringWidth(text), textHeight = fontMetrics.getAscent();
		int x = (width - textWidth) / 2, y = (height + textHeight) / 2;
		g2d.drawString(text, x, y);
		g2d.dispose();
		ImageIO.write(bufferedImage, "jpg", output);
	}

}
