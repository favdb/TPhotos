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
package tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * check if FFmpeg is installed
 *
 * @author favdb
 */
public class FFmpeg {

	/**
	 * check if FFmpeg is installed
	 *
	 * @return
	 */
	public static boolean isInstalled() {
		try {
			Process process = new ProcessBuilder("ffmpeg", "-version").start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
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
	 * main for testing
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

}
