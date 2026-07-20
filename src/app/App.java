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
package app;

import app.ui.AboutDlg;
import app.ui.MainFrame;
import app.ui.PrefDlg;
import app.xml.Xml;
import i18n.I18N;
import java.awt.Font;
import java.io.File;
import java.util.Enumeration;
import java.util.Locale;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;
import resources.icons.IconUtil;
import tools.LOG;
import tools.LaF;
import tools.file.EnvUtil;
import tools.file.FileUtil;

/**
 * application main class
 *
 * @author favdb
 */
public class App {

	private static final String TT = "App.";

	public static Pref preferences;
	private static Font fontDef;
	public static MainFrame mainFrame;
	private static boolean dev;
	private static String lang = "";//to force the language

	/**
	 * the main methode to create the class
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		initialize(args, "App");
		SwingUtilities.invokeLater(() -> {
			mainFrame = new MainFrame();
			sorterDo();
			mainFrame.setVisible(true);
		});
	}

	/**
	 * check if dev is activated
	 *
	 * @return
	 */
	public static boolean isDev() {
		return dev;
	}

	/**
	 * initialize the App
	 *
	 * @param args
	 * @param app
	 */
	public static void initialize(String[] args, String app) {
		String vargs = String.join(" ", args);
		if (args.length == 0) {
			LOG.log(Const.getFullName() + " starting with no option");
		} else {
			LOG.log(Const.getFullName() + " starting with: " + vargs);
			if (args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					String arg = args[i];
					switch (arg.toLowerCase()) {
						case "--trace":
							LOG.setTrace();
							break;
						case "--dev":
							dev = true;
							break;
						case "--lang":
							lang = args[i + 1];
							break;
						default:
							break;
					}
				}
			}
		}
		prefInit();
		fontInit();
		LaF.init();
		if (lang.isEmpty()) {
			I18N.initMessages(Locale.getDefault());
		} else {
			I18N.initMessages(new Locale(lang));
		}
		IconUtil.setDefSize();
	}

	/**
	 * initialize the font
	 */
	public static void fontInit() {
		int sz = preferences.getInteger(Pref.KEY.FONT_SIZE);
		fontDef = new Font("dialog", Font.PLAIN, sz);
		UIDefaults defaults = UIManager.getDefaults();
		Enumeration newKeys = defaults.keys();
		while (newKeys.hasMoreElements()) {
			Object obj = newKeys.nextElement();
			Object current = UIManager.get(obj);
			if (current instanceof FontUIResource) {
				UIManager.put(obj, new FontUIResource(fontGet()));
			} else if (current instanceof Font) {
				UIManager.put(obj, fontGet());
			}
		}
	}

	/**
	 * get current default font
	 *
	 * @return
	 */
	public static Font fontGet() {
		return fontDef;
	}

	/**
	 * init the current Font
	 *
	 * @param font
	 */
	public static void fontSet(Font font) {
		preferences.setInteger(Pref.KEY.FONT_SIZE, font.getSize());
		int sz = preferences.getInteger(Pref.KEY.FONT_SIZE);
		fontDef = new Font("dialog", Font.PLAIN, sz);
		if (mainFrame != null) {
			SwingUtilities.updateComponentTreeUI(mainFrame);
		}
	}

	/**
	 * initialize preferences
	 */
	private static void prefInit() {
		//LOG.trace(TT + "prefInit()");
		preferences = new Pref();
	}

	/**
	 * exit application
	 */
	public static void exit() {
		mainFrame.albumGet().save();
		preferences.save();
		System.exit(0);
	}

	/**
	 * close the App
	 */
	public static void close() {
		//empty
	}

	/**
	 * check if given file is JPEG
	 *
	 * @param f
	 * @return
	 */
	public static boolean jpegIs(File f) {
		if (!f.isFile()) {
			return false;
		}
		String str = f.getName().toLowerCase();
		return (str.endsWith(".jpg") || str.endsWith(".jpeg") || str.endsWith(".webp"));
	}

	/**
	 * count the number of JPEG files
	 *
	 * @param dir
	 * @return
	 */
	public static int jpegCount(File dir) {
		//LOG.trace(TT + "countJPEG(dir=" + dir.getAbsolutePath() + ")");
		File[] files = dir.listFiles();
		if (files == null || files.length == 0) {
			return 0;
		}
		int nb = 0;
		for (File f : files) {
			if (f.isDirectory()) {
				nb += jpegCount(f);
			} else {
				if (App.jpegIs(f)) {
					nb++;
				}
			}
		}
		return nb;
	}

	/**
	 * select a photos folder
	 */
	public static void photosDirSelect() {
		String dir = App.preferences.photosDirGet();
		if (dir.isEmpty()) {
			dir = EnvUtil.getPhotosDir().getAbsolutePath();
		}
		JFileChooser chooser = new JFileChooser(dir);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		String newdir = chooser.getSelectedFile().getAbsolutePath();
		preferences.photosDirSet(newdir);
		mainFrame.photosDirSet(new File(newdir));
		mainFrame.fileSet(new File(newdir + File.separator + "Album.xml"));
	}

	/**
	 * init the album File
	 *
	 * @param file
	 */
	private static void albumFileSet(File file) {
		preferences.albumLastSet(file.getName());
		mainFrame.fileSet(file);
	}

	/**
	 * create a new album file
	 */
	public static void albumFileNew() {
		FileNameExtensionFilter xmlfilter = new FileNameExtensionFilter("xml files (*.xml)", "xml");
		String base = preferences.photosDirGet() + File.separator + "Album";
		File inf = new File(base + ".xml");
		int i = 1;
		while (inf.exists()) {
			inf = new File(String.format("%s%02d.xml", base, i++));
			if (i > 20) {
				break;
			}
		}
		if (i > 20) {
			JOptionPane.showMessageDialog(mainFrame,
					I18N.getMsg("album.error_toomuch", inf),
					"album",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		JFileChooser chooser = new JFileChooser(inf);
		chooser.setSelectedFile(inf);
		chooser.setFileFilter(xmlfilter);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		String str = chooser.getSelectedFile().getAbsolutePath();
		if (!str.endsWith(".xml")) {
			str += ".xml";
		}
		File file = new File(str);
		if (!file.exists()) {
			//create an empty Album file
			FileUtil.fileWriteString(file,
					Xml.getHeader()
					+ String.format("<album title=\"%s\">", FileUtil.removeExtension(file.getName()))
					+ "<pref mode=\"0\" tempo=\"0\" comment=\"{JJ/MM/AA}\" />"
					+ "<list/>"
					+ "</album>"
			);
			mainFrame.fileSet(file);
		} else {
			JOptionPane.showMessageDialog(mainFrame,
					I18N.getMsg("album.error_exists", file),
					I18N.getMsg("app.album"),
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * open an album file
	 */
	public static void albumFileOpen() {
		FileNameExtensionFilter xmlfilter = new FileNameExtensionFilter("xml files (*.xml)", "xml");
		JFileChooser chooser = new JFileChooser(preferences.photosDirGet());
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(xmlfilter);
		int i = chooser.showOpenDialog(null);
		if (i != JFileChooser.APPROVE_OPTION) {
			return;
		}
		mainFrame.fileSet(chooser.getSelectedFile());
	}

	/**
	 * do the about
	 */
	public static void aboutDo() {
		AboutDlg dlg = new AboutDlg(mainFrame);
		dlg.setVisible(true);
	}

	/**
	 * do the sorter
	 */
	public static void sorterDo() {
		mainFrame.doSorter();
	}

	/**
	 * do the album
	 */
	public static void albumDo() {
		mainFrame.doDiapo();
	}

	/**
	 * refresh the album
	 */
	public static void albumRefresh() {
		mainFrame.albumRefresh();
	}

	/**
	 * update the title
	 */
	public static void updateTitle() {
		if (mainFrame != null) {
			mainFrame.titleUpdate();
			diaporamaEnable();
		}
	}

	/**
	 * do th diaporama
	 */
	public static void diapoDo() {
		mainFrame.doDiaporama();
	}

	/**
	 * enable the diaporama
	 */
	public static void diaporamaEnable() {
		if (mainFrame != null) {
			boolean b = mainFrame.albumGet().getTable().getRowCount() > 0;
			if (mainFrame.appMenu.btDiapo != null) {
				mainFrame.appMenu.btDiapo.setVisible(b);
				mainFrame.appMenu.btExport.setVisible(b);
			}
		}
	}

	/**
	 * do the export
	 */
	public static void exportDo() {
		//LOG.trace(TT+"doExport()");
		mainFrame.doExport();
	}

	/**
	 * changing the zoom value
	 */
	public static void zoom() {
		PrefDlg dlg = new PrefDlg(mainFrame);
		dlg.setVisible(true);
	}

	/**
	 * setting the LaF, not used
	 */
	public static void setLaf() {
		//LOG.trace(TT + "setLaf()");
		try {
			if (mainFrame != null) {
				SwingUtilities.updateComponentTreeUI(mainFrame);
			}
		} catch (Exception exc) {
			LOG.err("Nimbus: Unsupported Look and feel!");
		}
	}

	public static String getLang() {
		return lang;
	}

	public static void printDo() {
		mainFrame.printDo();
	}

}
