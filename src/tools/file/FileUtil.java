/*
 Storybook: Open Source software for novelists and authors.
 Copyright (C) 2008 - 2012 Martin Mustun

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a fileCopy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tools.file;

import app.App;
import i18n.I18N;
import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import tools.LOG;

public class FileUtil {

	private static final String TT = "FileUtil.";

	/**
	 * ask confirmation (yes or no)
	 *
	 * @param parent
	 * @param title
	 * @param msg
	 * @param filename
	 * @return
	 */
	public static boolean askInfo(Component parent, String title, String msg, String filename) {
		int n = JOptionPane.showConfirmDialog(parent,
				I18N.getMsg(msg, filename),
				I18N.getMsg(title),
				JOptionPane.YES_NO_OPTION);
		return n == JOptionPane.YES_OPTION;
	}

	/**
	 * ask to replace an existing file
	 *
	 * @param comp
	 * @param f
	 * @return
	 */
	public static boolean askReplace(Component comp, File f) {
		if (!f.exists()) {
			return true;
		}
		return askInfo(comp, "export", "export.replace", f.getAbsolutePath());
	}

	/**
	 * cleanup a file name
	 *
	 * @param name
	 * @return
	 */
	public static String filenameCleanup(String name) {
		return name.replaceAll("[\\/:*?\"<>|,]", "").replace("\\\\", "");
	}

	/**
	 * read a file as a String
	 *
	 * @param fileName
	 * @return
	 */
	public static String fileReadAsString(File fileName) {
		return fileReadAsString(fileName.getAbsolutePath());
	}

	/**
	 * read a file as a String
	 *
	 * @param filePath
	 * @return
	 */
	public static String fileReadAsString(String filePath) {
		//LOG.printInfos("IOUtil.fileReadAsString(filePath=" + filePath + ")");
		byte[] buffer = new byte[(int) new File(filePath).length()];
		BufferedInputStream f = null;
		try {
			f = new BufferedInputStream(new FileInputStream(filePath));
			f.read(buffer);
		} catch (FileNotFoundException ex) {
			return ("");
		} catch (IOException ex) {
			return ("");
		} finally {
			if (f != null) {
				try {
					f.close();
				} catch (IOException e) {
					LOG.err("IOUtil.readFileAsString(" + filePath + ")", e);
				}
			}
		}
		return (new String(buffer));
	}

	public static void fileAddString(String file, String str) {
		try {
			try (BufferedWriter out = new BufferedWriter(new FileWriter(file, true))) {
				out.write(str);
			}
		} catch (IOException ex) {
		}
	}

	public static void fileCleanEmptyLines(File infile) {
		File temp = new File(infile.getAbsolutePath() + ".temp");
		try (PrintWriter outfile = new PrintWriter(new FileWriter(temp))) {
			Scanner scanner = new Scanner(infile);
			while (scanner.hasNextLine()) {
				String currentLine = scanner.nextLine();
				if (!currentLine.trim().isEmpty()) {
					outfile.println(currentLine);
				}
			}
			infile.delete();
			temp.renameTo(infile);
		} catch (FileNotFoundException ex) {
			LOG.err(TT + "fileCleanEmptyLines error", ex);
		} catch (IOException ex) {
			LOG.err(TT + "fileCleanEmptyLines error", ex);
		}
	}

	/**
	 * write a String to a file name
	 *
	 * @param file
	 * @param str
	 */
	public static void fileWriteString(String file, String str) {
		fileWriteString(new File(file), str);
	}

	/**
	 * write a String to a File
	 *
	 * @param file
	 * @param str
	 * @return true if OK
	 */
	public static boolean fileWriteString(File file, String str) {
		//LOG.printInfos(TT+".fileWriteString(file=" + file.getAbsolutePath() + ",str)");
		try {
			file.createNewFile();
			try (BufferedWriter f = new BufferedWriter(new FileWriter(file))) {
				f.write(str);
				f.flush();
			}
			return true;
		} catch (IOException e) {
			LOG.err(TT + ".fileWriteString(" + file.getAbsolutePath()
					+ ",str len=" + str.length() + ")", e);
		}
		return false;
	}

	/**
	 * convert a String path with absolute "file://" protocol to a path
	 *
	 * @param str
	 * @param path
	 * @return
	 */
	public static String convertToRelativePath(String str, String path) {
		String rc = str.replace("file://" + path + File.pathSeparator, "");
		return (rc);
	}

	/**
	 * select a directory
	 *
	 * @param parent
	 * @param f
	 * @return
	 */
	public static File directorySelect(JDialog parent, String f) {
		JFileChooser chooser = new JFileChooser(f);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle(I18N.getMsg("directory.select"));
		String dirsel = "directory.select_select";
		chooser.setApproveButtonText(I18N.getMsg(dirsel));
		int i = chooser.showOpenDialog(parent);
		if (i != 0) {
			return null;
		}
		return chooser.getSelectedFile();
	}

	/**
	 * select a File
	 *
	 * @param parent component to locate the chooser dialog
	 * @param fileName initial file name, may be empty
	 * @param ext extension type
	 * @param desc description of the type of file, may be empty
	 * @param title title for the chooser dialog
	 * @return
	 */
	public static File fileSelect(Component parent,
			String fileName,
			String ext,
			String desc,
			String title) {
		/*LOG.printInfos("IOUtil.fileSelect(parent"
				+ ", fileName=\"" + fileName + "\""
				+ ", ext=" + ext
				+ ", desc=\"" + desc + "\""
				+ ", title=\"" + title + "\""
				+ ")");*/
		JFileChooser chooser = new JFileChooser(fileName);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		FileFilter fileFilter = new FileFilter(ext);
		if (!desc.isEmpty()) {
			fileFilter.setDescription(desc);
		}
		chooser.addChoosableFileFilter(fileFilter);
		chooser.setFileFilter(fileFilter);
		if (!title.isEmpty()) {
			chooser.setDialogTitle(I18N.getMsg(title));
			chooser.setApproveButtonText(I18N.getMsg(title));
		}
		if (fileName != null && !fileName.isEmpty()) {
			chooser.setSelectedFile(new File(fileName));
		}
		int i = chooser.showOpenDialog(parent);
		if (i != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		return chooser.getSelectedFile();
	}

	/**
	 * copy a File
	 *
	 * @param infile: file to copy
	 * @param outfile: destination file
	 * @return true if copy is OK
	 */
	public static boolean fileCopy(File infile, File outfile) {
		//LOG.trace(TT + "fileCopy(inFile='" + infile.getAbsolutePath()
		//		+ "', outFile='" + outfile.getAbsolutePath() + "')");
		try {
			try {
				Paths.get(infile.getAbsolutePath());
			} catch (InvalidPathException | NullPointerException ex) {
				LOG.err(String.format("Invalid path for infile '%s'",
						outfile.getAbsolutePath()));
				return false;
			}
			try {
				Paths.get(outfile.getAbsolutePath());
			} catch (InvalidPathException | NullPointerException ex) {
				LOG.err(String.format("Invalid path for outfile '%s'",
						outfile.getAbsolutePath()));
				return false;
			}
			outfile.getParentFile().mkdirs();
			FileOutputStream os;
			try (FileInputStream is = new FileInputStream(infile)) {
				os = new FileOutputStream(outfile);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
				os.close();
			}
		} catch (IOException ex) {
			LOG.err(String.format("Unable copy %s to %s\n%s",
					infile.getAbsolutePath(),
					outfile.getAbsolutePath(),
					ex.getLocalizedMessage()));
			return false;
		}
		return true;
	}

	/**
	 * delete a File
	 *
	 * @param file
	 */
	public static void fileDelete(File file) {
		if (file.exists()) {
			try {
				Files.delete(file.toPath());
			} catch (IOException ex) {
			}
		}
	}

	/**
	 * move a file
	 *
	 * @param infile
	 * @param outfile
	 */
	public static void fileMove(File infile, File outfile) {
		fileCopy(infile, outfile);
		fileDelete(infile);
	}

	/**
	 * return a list of Files for the given directory
	 *
	 * @param dir
	 * @return
	 */
	public static List<File> computeList(File dir) {
		List<File> ls = new ArrayList<>();
		if (dir.exists()) {
			File[] fls = dir.listFiles();
			for (File f : fls) {
				if (f.isDirectory()) {
					ls.addAll(computeList(f));
				} else if (f.isFile()) {
					if (App.jpegIs(f)) {
						ls.add(f);
					}
				}
			}
		}
		return ls;
	}

	/**
	 * get number of element into the given directory
	 *
	 * @param file
	 * @return
	 */
	public static int getNbElement(File file) {
		List<File> ls = computeList(file);
		return ls.size();
	}

	public boolean fileMove(String sourcePath, String targetPath) {
		boolean rc = true;
		try {
			Files.move(Paths.get(sourcePath), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ex) {
			rc = false;
			LOG.err(TT + "fileMove(...)", ex);
		}
		return rc;
	}

	public static void fileSort(File file) {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		List<String> lines = new ArrayList<>();
		try {
			reader = new BufferedReader(new FileReader(file));
			String currentLine = reader.readLine();
			while (currentLine != null) {
				lines.add(currentLine);
				currentLine = reader.readLine();
			}
			Collections.sort(lines);
			writer = new BufferedWriter(new FileWriter(file));
			for (String line : lines) {
				writer.write(line);
				writer.newLine();
			}
		} catch (IOException e) {
			LOG.err(TT + ".fileSort(file) " + e.getLocalizedMessage());
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				LOG.err(TT + ".fileSort(file) " + e.getLocalizedMessage());
			}
		}
	}

	/**
	 * check if the file content is an image
	 *
	 * @param file
	 * @return
	 */
	public static boolean fileIsImage(File file) {
		String mimeType = URLConnection.guessContentTypeFromName(file.getName());
		return mimeType.contains("image");
	}

	/**
	 * read a zip file as a String
	 *
	 * @param zipFile
	 * @param entry
	 * @return
	 */
	public static String readZipfileAsString(ZipFile zipFile, ZipEntry entry) {
		StringBuilder buf = new StringBuilder();
		try {
			InputStreamReader isr = new InputStreamReader(zipFile.getInputStream(entry));
			char[] buffer = new char[1024];
			while (isr.read(buffer, 0, buffer.length) != -1) {
				buf.append(new String(buffer));
			}
		} catch (IOException ex) {
			LOG.err("IOUtil.readZipAsString error");
			return "";
		}
		return buf.toString();
	}

	/**
	 * remove the extension part
	 *
	 * @param path
	 * @return
	 */
	public static String removeExtension(String path) {
		int n = path.lastIndexOf(".");
		return (n < 0 ? path : path.substring(0, n));
	}

	/**
	 * copy a directory
	 *
	 * @param srcDir
	 * @param destDir
	 *
	 * @throws IOException
	 */
	public static void dirCopy(File srcDir, File destDir) throws IOException {
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		for (String f : srcDir.list()) {
			dirCopyCompatibityMode(new File(srcDir, f), new File(destDir, f));
		}
	}

	/**
	 * create a directory in compatibility mode
	 *
	 * @param source
	 * @param destination
	 * @throws IOException
	 */
	public static void dirCopyCompatibityMode(File source, File destination) throws IOException {
		if (source.isDirectory()) {
			dirCopy(source, destination);
		} else {
			fileCopy(source, destination);
		}
	}

	/**
	 * create a directory
	 *
	 * @param path
	 * @param name
	 * @return
	 */
	public static String dirCreate(String path, String name) {
		//LOG.printInfos(TT+".dirCreate(path="+path+",name="+ name+")");
		File f = new File(path + File.separator + name);
		String fname = f.getAbsolutePath();
		if (!f.exists()) {
			try {
				Files.createDirectories(f.toPath());
			} catch (IOException ex) {
				LOG.err(I18N.getMsg("directory.create_unable", name) + " " + fname);
				return "";
			}
		}
		return fname;
	}

	public static boolean dirCreate(File f) {
		//LOG.trace(TT + "dirCreate(file=" + f.getAbsolutePath() + ")");
		try {
			if (!f.exists()) {
				f.mkdir();
			}
		} catch (Exception e) {
			LOG.err(I18N.getMsg("directory.creation_failed", f.getAbsolutePath()), e);
			return false;
		}
		LOG.log(I18N.getMsg("directory.creation", f.getAbsolutePath()));
		return true;
	}

	/**
	 * replace a path by a relative one
	 *
	 * @param path
	 * @param name
	 * @return
	 */
	public static String fileToRelative(String path, String name) {
		if (name.startsWith(path)) {
			return name.replace(path, ".");
		} else {
			return name;
		}
	}

	/**
	 * replace a path by an absolute one
	 *
	 * @param path
	 * @param name
	 * @return
	 */
	public static String fileToAbsolute(String path, String name) {
		if (name.startsWith(".")) {
			return name.replace(".", path);
		} else {
			return name;
		}
	}

	/**
	 * check if a File content is an image
	 *
	 * @param file
	 * @return
	 */
	public static boolean isImage(File file) {
		boolean valid = false;
		try {
			Image image = ImageIO.read(file);
			valid = (image != null);
		} catch (IOException ex) {
			// nothing
		}
		return valid;
	}

	/**
	 * get the extension from a File
	 *
	 * @param file
	 * @return
	 */
	public static String getExtension(File file) {
		String fileName = file.getAbsolutePath();
		String fe = "";
		int i = fileName.lastIndexOf('.');
		if (i > 0) {
			fe = fileName.substring(i + 1);
		}
		return fe;
	}

	/**
	 * cleanup a file name
	 *
	 * @param name
	 * @return
	 */
	public static String cleanupFilename(String name) {
		String str = name.replaceAll("[\\/:*?\"<>|]", "");
		str = str.replace("\\\\", "");
		return str;
	}

	/**
	 * check if a directory exists
	 *
	 * @param dir
	 * @return
	 */
	public static boolean dirExists(String dir) {
		File file = new File(dir);
		return file.exists() && file.isDirectory();
	}

	/**
	 * delete a directory content
	 *
	 * @param dir
	 * @return
	 */
	public static boolean dirDelete(File dir) {
		//LOG.printInfos("IOUtil.dirDelete(dir=" + dir.getAbsolutePath() + ")");
		for (File subfile : dir.listFiles()) {
			if (subfile.isDirectory()) {
				dirDelete(subfile);
			}
			subfile.delete();
		}
		return dir.delete();
	}

	/**
	 * get the list of files from the given directory
	 *
	 * @param src
	 * @return
	 */
	public static List<File> dirList(File src) {
		List<File> list = new ArrayList<>();
		for (File file : src.listFiles()) {
			list.add(file);
			if (file.isDirectory()) {
				dirList(file, list);
			}
		}
		list.sort(Comparator.reverseOrder());
		return list;
	}

	/**
	 * get the list of files from a given directory into the given list
	 *
	 * @param src
	 * @param fileList
	 */
	public static void dirList(File src, List<File> fileList) {
		for (File file : src.listFiles()) {
			fileList.add(file);
			if (file.isDirectory()) {
				dirList(file, fileList);
			}
		}
		fileList.sort(Comparator.reverseOrder());
	}

	public static String resourceRead(String in, Class fromClass) {
		//LOG.printInfos("IOUtil.resourceRead(in=" + in + ", fromClass)");
		InputStream res;
		res = fromClass.getResourceAsStream(in);
		if (res == null) {
			LOG.err(TT + "resourceRead(in=\""
					+ in
					+ "\", fromClass="
					+ fromClass.getName()
					+ " resource not found");
			return "";
		}
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(
							fromClass.getResourceAsStream(in), "UTF-8"));
			for (int c = br.read(); c != -1; c = br.read()) {
				sb.append((char) c);
			}
		} catch (IOException ex) {
			return "";
		}
		return sb.toString();

	}

	/**
	 * convert a PNG image to a compressed (30%) JPEG
	 *
	 * @param inFile
	 *
	 * @return : true if copy is OK
	 */
	public static boolean png2jpeg(String inFile) {
		try {
			File inputFile = new File(inFile);
			BufferedImage inputImage = ImageIO.read(inputFile);
			ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
			ImageWriteParam param = writer.getDefaultWriteParam();
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(0.30f);
			String outFile = inFile.substring(0, inFile.lastIndexOf(".")) + ".jpeg";
			ImageOutputStream outStream = ImageIO.createImageOutputStream(outFile);
			writer.setOutput(outStream);
			IIOImage outputImage = new IIOImage(inputImage, null, null);
			writer.write(null, outputImage, param);
			writer.dispose();
		} catch (IOException ex) {
			LOG.err("png2jpeg(...) error", ex);
			return false;
		}
		return true;
	}

	/**
	 * replace the extension of the given file name
	 *
	 * @param infile
	 * @param ext
	 *
	 * @return the modified file name
	 */
	public static String changeExt(String infile, String ext) {
		String zext = !ext.startsWith(".") ? "." + ext : ext;
		int i = infile.lastIndexOf('.');
		if (i < 0) {
			// simply add the extension
			return infile + zext;
		}
		// change the extension
		return infile.substring(0, i) + zext;
	}

	public static JPanel initDirSelect(String label, JTextField tfFolder, JButton btFolder) {
		JPanel p = new JPanel();
		p.add(new JLabel(" "));
		p.add(new JLabel(I18N.getColonMsg(label)));
		tfFolder.setColumns(32);
		//tfFolder.setEditable(false);
		p.add(tfFolder);
		p.add(btFolder);
		return p;
	}

	public static int getMD5(File file) {
		byte[] data;
		try {
			data = Files.readAllBytes(file.toPath());
			byte[] hash = MessageDigest.getInstance("MD5").digest(data);
			return new BigInteger(1, hash).toString(16).hashCode();
		} catch (IOException | NoSuchAlgorithmException ex) {
			LOG.err(TT + "getMD5(file) error", ex);
		}
		return -1;
	}

}
