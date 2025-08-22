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
package tools.file;

import i18n.I18N;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import tools.LOG;

/**
 * utilities for ZIP file
 *
 * inspired by
 * http://www.avajava.com/tutorials/lessons/how-do-i-zip-a-directory-and-all-its-contents.html
 *
 * @author favdb
 */
public class ZipUtil {

	private static final String TT = "ZipUtil";

	private ZipUtil() {
		// empty
	}

	/**
	 * deflate the given ZIP file to the given path
	 *
	 * @param zipFile
	 * @param destDir
	 * @return true if deflate is OK
	 *
	 */
	public static boolean deflate(File zipFile, Path destDir) {
		boolean rc = false;
		if (!zipFile.exists()) {
			return rc;
		}
		FileInputStream fis;
		//buffer for read and write data to file
		byte[] buffer = new byte[1024];
		try {
			fis = new FileInputStream(zipFile);
		} catch (FileNotFoundException ex) {
			return rc;
		}
		try (ZipInputStream zis = new ZipInputStream(fis)) {
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				String fileName = ze.getName();
				File newFile = new File(destDir + File.separator + fileName);
				if (ze.isDirectory()) {
					new File(newFile.getParent()).mkdirs();
				} else {
					//create directories for sub directories in zip
					new File(newFile.getParent()).mkdirs();
					try (FileOutputStream fos = new FileOutputStream(newFile)) {
						int len;
						while ((len = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
					} catch (Exception ex) {
						rc = false;
					}
					//close this ZipEntry
					zis.closeEntry();
				}
				ze = zis.getNextEntry();
			}
			//close last ZipEntry
			zis.closeEntry();
			fis.close();
		} catch (Exception ex) {
			rc = false;
		}
		return rc;
	}

	/**
	 * deflate the source name into the given ouput directory
	 *
	 * @param source : may be an URL source or a filename
	 * @param outDir
	 *
	 * @throws IOException
	 */
	public static void deflate(String source, String outDir) throws IOException {
		File folder = new File(outDir);
		new File(folder.getParent()).mkdirs();
		ZipInputStream zis;
		if (source.startsWith("http://") || source.startsWith("https://")) {
			URL url = new URL(source);
			zis = new ZipInputStream(url.openStream());
		} else {
			zis = new ZipInputStream(new FileInputStream(source));
		}
		ZipEntry entry = zis.getNextEntry();
		while (entry != null) {
			String fname = entry.getName();
			File newFile = new File(outDir + File.separator + fname);
			newFile.getParentFile().mkdirs();
			if (entry.isDirectory()) {
				newFile.mkdirs();
			} else {
				ZipUtil.writeFile(zis, newFile);
			}
			entry = zis.getNextEntry();
		}
		zis.closeEntry();
	}

	/**
	 * write a ZIP inputstream to a File
	 *
	 * @param is
	 * @param file
	 * @throws IOException
	 */
	private static void writeFile(ZipInputStream is, File file) throws IOException {
		byte[] buffer = new byte[1024];
		file.createNewFile();
		try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
			int length;
			while ((length = is.read(buffer)) > 0) {
				fileOutputStream.write(buffer, 0, length);
			}
		}
	}

	public static void fromDir(String src, String dest, String... mime) {
		fromDir(src, new File(dest), mime);
	}

	/**
	 * create a ZIP from the given directory to the given File
	 *
	 * @param src: the given directory
	 * @param dest: the ZIP file to create
	 * @param mime : optional, the MIME type
	 */
	public static void fromDir(String src, File dest, String... mime) {
		//LOG.trace("ZipUtil.fromDir(" + src + ", to=" + dest + ")");
		File directoryToZip = new File(src);
		List<File> fileList = FileUtil.dirList(directoryToZip);
		writeFile(dest, fileList, src, mime);
	}

	/**
	 * create a ZIP from the given directory to the given File
	 *
	 * @param src: the source directory
	 * @param dest: the ZIP file to create
	 * @param mime : optional, the MIME type
	 */
	public static void fromDir(File src, File dest, String... mime) {
		List<File> fileList = FileUtil.dirList(src);
		writeFile(dest, fileList, src.getAbsolutePath(), mime);
	}

	private static final String ZIP_ERR = "epub.zip.error";

	/**
	 * write the given list of File into the destination ZIP file
	 *
	 * @param dest : the destination ZIP file
	 * @param fileList : the File list to write
	 * @param srcDir : the source directory of the files
	 * @param mime : optional, MIME type
	 */
	public static void writeFile(File dest, List<File> fileList, String srcDir, String... mime) {
		try {
			FileOutputStream fos = new FileOutputStream(dest.getAbsolutePath());
			ZipOutputStream zos = new ZipOutputStream(fos);
			ZipEntry entry = new ZipEntry("mimetype");
			entry.setMethod(ZipEntry.STORED);
			if (mime != null && mime.length > 0) {
				byte[] bytes = mime[0].getBytes();
				entry.setSize(bytes.length);
				CRC32 crc = new CRC32();
				crc.update(bytes);
				entry.setCrc(crc.getValue());
				zos.putNextEntry(entry);
				zos.write(bytes, 0, bytes.length);
				zos.flush();
				zos.closeEntry();
			}
			for (File file : fileList) {
				if (!file.isDirectory() && !addTo(file, zos, srcDir)) {
					break;
				}
			}
			zos.finish();
		} catch (FileNotFoundException ex) {
			LOG.err(I18N.getMsg(ZIP_ERR), ex);
		} catch (IOException ex) {
			LOG.err(I18N.getMsg(ZIP_ERR), ex);
		}
	}

	/**
	 * write a ZIP file from the given list of Files
	 *
	 * @param dest
	 * @param fileList
	 */
	public static void writeList(File dest, List<File> fileList) {
		try {
			try (FileOutputStream fos = new FileOutputStream(dest.getAbsolutePath()); ZipOutputStream zos = new ZipOutputStream(fos)) {
				for (File file : fileList) {
					if (!file.isDirectory() && !ZipUtil.addTo(file, zos)) {
						break;
					}
				}
				zos.finish();
			}
		} catch (FileNotFoundException ex) {
			LOG.err(I18N.getMsg(ZIP_ERR), ex);
		} catch (IOException ex) {
			LOG.err(I18N.getMsg(ZIP_ERR), ex);
		}
	}

	/**
	 * write a ZIP file from the given list of Files
	 *
	 * @param dest : the ZIP destination file name
	 * @param entry : entry name for the zipped file
	 * @param str : the String value to write
	 */
	public static void writeString(String dest, String entry, String str) {
		try (FileOutputStream fos = new FileOutputStream(dest)) {
			try (ZipOutputStream zos = new ZipOutputStream(fos)) {
				ZipEntry zipEntry = new ZipEntry(entry);
				zos.putNextEntry(zipEntry);
				byte[] bytes = str.getBytes();
				zos.write(bytes, 0, bytes.length);
				zos.flush();
				zos.closeEntry();
				zos.finish();
			}
			fos.close();
		} catch (IOException ex) {
			LOG.err(I18N.getMsg(ZIP_ERR), ex);
		}
	}

	/**
	 * add a File to the given ZIP file
	 *
	 * @param file : to add
	 * @param zos : ZIP destination file
	 *
	 * @return
	 */
	public static boolean addTo(File file, ZipOutputStream zos) {
		//LOG.trace(TT + ".addTo(file=" + file.getAbsolutePath() + ", zos)");
		try (FileInputStream fis = new FileInputStream(file)) {
			String n = file.getAbsolutePath();
			String zipFilePath = file.getAbsolutePath().substring(n.lastIndexOf("epub") + 5);
			ZipEntry zipEntry = new ZipEntry(zipFilePath);
			zos.putNextEntry(zipEntry);
			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zos.write(bytes, 0, length);
			}
			zos.flush();
			zos.closeEntry();
		} catch (FileNotFoundException ex) {
			LOG.err(I18N.getMsg(ZIP_ERR), ex);
			return (false);
		} catch (IOException ex) {
			LOG.err(I18N.getMsg(ZIP_ERR), ex);
			return (false);
		}
		return (true);
	}

	/**
	 * add the given File to a ZIP file
	 *
	 * @param file
	 * @param zos
	 * @param src : the source directory
	 * @return
	 */
	public static boolean addTo(File file, ZipOutputStream zos, String src) {
		//LOG.trace(TT + ".addTo(file=" + file.getAbsolutePath() + ", zos, src=" + src + ")");
		try (FileInputStream fis = new FileInputStream(file)) {
			String n = file.getAbsolutePath();
			String zipFilePath = file.getAbsolutePath().substring(n.lastIndexOf(src) + src.length() + 1);
			ZipEntry zipEntry = new ZipEntry(zipFilePath);
			if (zipEntry.getName().equals("mimetype")) {
				return true;
			}
			zos.putNextEntry(zipEntry);
			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zos.write(bytes, 0, length);
			}
			zos.flush();
			zos.closeEntry();
		} catch (FileNotFoundException ex) {
			LOG.err(I18N.getMsg(ZIP_ERR), ex);
			return (false);
		} catch (IOException ex) {
			LOG.err(I18N.getMsg(ZIP_ERR), ex);
			return (false);
		}
		return (true);
	}

	/**
	 * add a directory content to a ZIP file
	 *
	 * @param file
	 * @param zos
	 * @param path
	 * @return
	 */
	public static boolean addPathTo(File file, ZipOutputStream zos, String path) {
		//LOG.trace("ZipUtil.addTo(src="+src+", file="+file.getAbsolutePath()+", zos");
		try (FileInputStream fis = new FileInputStream(file)) {
			String n = file.getAbsolutePath();
			String zipFilePath = path + file.getAbsolutePath().substring(n.lastIndexOf("epub") + 5);
			ZipEntry zipEntry = new ZipEntry(zipFilePath);
			zos.putNextEntry(zipEntry);
			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zos.write(bytes, 0, length);
			}
			zos.flush();
			zos.closeEntry();
		} catch (FileNotFoundException ex) {
			LOG.err(I18N.getMsg(ZIP_ERR), ex);
			return (false);
		} catch (IOException ex) {
			LOG.err(I18N.getMsg(ZIP_ERR), ex);
			return (false);
		}
		return (true);
	}

	public static List<String> listEntries(String zip) {
		List<String> list = new ArrayList<>();
		try {
			ZipFile zipFile = new ZipFile(zip);
			Enumeration<? extends ZipEntry> e = zipFile.entries();
			while (e.hasMoreElements()) {
				list.add(e.nextElement().getName());
			}

		} catch (IOException ex) {

		}
		return list;
	}

	/**
	 * get the content of the given entry Zip file into a list of String (each is a line)
	 *
	 * @param zip : the ZIP File
	 * @param entryName
	 *
	 * @return
	 */
	public static List<String> getContent(File zip, String entryName) {
		try (ZipFile zipFile = new ZipFile(zip)) {
			ZipEntry entry = zipFile.getEntry(entryName);
			if (entry != null) {
				try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8))) {
					List<String> list = new ArrayList<>();
					String line;
					while ((line = reader.readLine()) != null) {
						list.add(line);
					}
					return list;
				} catch (IOException e) {
				}
			} else {
			}
		} catch (IOException e) {
		}
		return new ArrayList<>();
	}

}
