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
package tools.jpeg;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author favdb
 */
public class Webp {

	public byte[] exifData;
	public JpegExif exif;

	public Webp(File f) throws Exception {
		try (BufferedInputStream buff = new BufferedInputStream(new FileInputStream(f))) {
			byte[] header = new byte[12];
			if (buff.read(header) < 12) {
				throw new Exception("File too short for WEBP header");
			}

			// Vérification du Magic Number RIFF .... WEBP
			if (header[0] != 'R' || header[1] != 'I' || header[2] != 'F' || header[3] != 'F'
					|| header[8] != 'W' || header[9] != 'E' || header[10] != 'B' || header[11] != 'P') {
				throw new Exception(f.getName() + " is not a valid WEBP file");
			}

			// Parcours des Chunks pour trouver le chunk EXIF
			while (buff.available() > 0) {
				byte[] chunkType = new byte[4];
				byte[] chunkSizeData = new byte[4];

				if (buff.read(chunkType) < 4 || buff.read(chunkSizeData) < 4) {
					break;
				}

				// RIFF utilise le Little Endian pour les tailles !
				int chunkSize = Endian.Little.getInt32(chunkSizeData);

				// Si le chunk est "EXIF"
				if (chunkType[0] == 'E' && chunkType[1] == 'X' && chunkType[2] == 'I' && chunkType[3] == 'F') {
					exifData = new byte[chunkSize];
					buff.read(exifData);

					// Initialisation de votre parseur Exif existant
					this.exif = new JpegExif(exifData);
					break; // On a trouvé ce qu'on cherchait
				} else {
					// Sauter le chunk si ce n'est pas EXIF
					// WEBP demande d'ajouter un octet de bourrage si la taille est impaire
					long toSkip = chunkSize + (chunkSize % 2);
					long skipped = 0;
					while (skipped < toSkip) {
						long s = buff.skip(toSkip - skipped);
						if (s <= 0) {
							break;
						}
						skipped += s;
					}
				}
			}
		}
	}

	public static String getDate(File file) {
		try {
			if (!file.exists()) {
				return null;
			}
			String str = null;
			Webp webp = new Webp(file);
			if (webp.exif != null && webp.exif.getIfd0() != null) {
				for (JpegExifEntry entry : webp.exif.getIfd0()) {
					if (entry.getTagNumberAsString().equals(JpegExifTag.TAG_CHANGEDATETIME)) {
						str = Jpeg.safeChar((String) entry.getValue()).replace(" ", "_");
						return str;
					}
				}
			}
			// Fallback sur la date de création du fichier
			FileTime ct = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
			if (ct != null) {
				LocalDateTime lt = ct.toInstant()
						.atZone(ZoneId.systemDefault()).toLocalDateTime();
				return lt.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
			}
		} catch (Exception ex) {
			// Remplacer par votre logger LOG.err si disponible
			System.err.println("Webp.getDate() error: " + ex.getMessage());
		}
		return null;
	}
}
