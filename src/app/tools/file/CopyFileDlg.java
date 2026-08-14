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
package app.tools.file;

import api.mig.MIG;
import api.mig.swing.MigLayout;
import app.App;
import app.export.ExportImage;
import app.ui.AbstractFrame;
import app.xml.XmlAlbumItem;
import app.i18n.I18N;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import app.tools.Html;
import app.tools.LOG;
import app.tools.jpeg.Jpeg;
import app.tools.jpeg.Webp;

/**
 * copy files dialog
 *
 * @author favdb
 */
public class CopyFileDlg extends JDialog {

	private static final String TT = "CopyFileDlg.";

	private final File todir;
	private boolean running = true;
	private JLabel lbFile;
	private JProgressBar pbar;
	private boolean autoremove;
	private int sorter;
	private Dimension dim;
	private final List<File> outfiles = new ArrayList<>();
	private boolean status = true;
	public List<XmlAlbumItem> items;
	private boolean withText;
	private float compress = -1f;
	private int number = 0;

	/**
	 * CopyFIlesDlg
	 *
	 * @param parent: parent JFrame
	 * @param items
	 * @param withText
	 * @param todir: destination directory
	 * @param sorter: 0,1,2,3,4 for date subdirectories if needed
	 * @param autoremove: remove file after copy
	 * @param dim: new size for the image, may be null for no resize
	 */
	public CopyFileDlg(AbstractFrame parent,
			List<XmlAlbumItem> items,
			boolean withText,
			File todir,
			int sorter,
			boolean autoremove,
			Dimension dim) {
		super(parent, false);
		this.items = items;
		this.todir = todir;
		this.withText = withText;
		this.sorter = sorter;
		this.autoremove = autoremove;
		this.dim = dim;
		initialize();
	}

	/**
	 * set list of AlbumItem
	 *
	 * @param items
	 */
	public void setItems(List<XmlAlbumItem> items) {
		this.items = items;
	}

	/**
	 * set autorove option
	 */
	public void setAutoremove() {
		autoremove = true;
	}

	/**
	 * set text parameter
	 */
	public void setWithText() {
		withText = true;
	}

	/*
	set dim parameter
	 */
	public void setDim(Dimension dim) {
		this.dim = dim;
	}

	/**
	 * set sorter mode
	 *
	 * @param mode
	 */
	public void setSorter(int mode) {
		this.sorter = mode;
	}

	/**
	 * initialize dialog
	 */
	private void initialize() {
		LOG.trace(TT + "initialize()"
				+ " items nb=" + items.size()
				+ " withText=" + (withText ? "true" : "false")
				+ " todir=" + todir
				+ " sorter=" + sorter
				+ " autoremove=" + (autoremove ? "true" : "false")
				+ " dim=" + (dim == null ? "null" : dim.toString()));
		setTitle(I18N.getMsg("organize.inprogress"));
		setLayout(new MigLayout(MIG.WRAP1));
		addReport(I18N.getMsg("photo.copy", new Object[]{
			items.size(), I18N.getMsg(items.size() > 1 ? "files" : "file")
		}) + "<br>");
		add(new JLabel(/*Html.intoHtml(report.toString())*/));
		add(lbFile = new JLabel());
		int c = App.fontGet().getSize();
		lbFile.setMinimumSize(new Dimension(c * 32, c));
		add(pbar = new JProgressBar(), MIG.GROW);
		pbar.setMaximum(items.size());
		pbar.setMinimumSize(new Dimension(c * 20, c));
		pbar.setStringPainted(true);
		pbar.setString("0/" + items.size());
		pack();
		setLocationRelativeTo(getParent());
	}

	/**
	 * check if in progress
	 *
	 * @return
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * check if OK
	 *
	 * @return
	 */
	public boolean isOK() {
		return status;
	}

	public String getReport() {
		return "";
	}

	/**
	 * add info to report
	 *
	 * @param text
	 */
	private void addReport(String text) {
		((AbstractFrame) getParent()).taInfosAdd(text);
	}

	/**
	 * start copying
	 */
	public void start() {
		//LOG.trace(TT + "start() todir=" + todir.getAbsolutePath());
		running = true;
		status = true;
		setVisible(true);
		File fout = null;
		for (XmlAlbumItem item : items) {
			if (!getOutfile(item.fileGet()).getParentFile().equals(fout)) {
				fout = getOutfile(item.fileGet()).getParentFile();
				if (sorter == 0 || sorter == 2) {
					fout.mkdirs();
				}
			}
		}
		new Thread(new CopyAction(this)).start();
	}

	/**
	 * copy next file
	 *
	 * @param i
	 */
	private void nextFile(int i) {
		//LOG.trace(TT + "nextFile() i=" + i);
		XmlAlbumItem item = items.get(i);
		File infile = item.fileGet();
		if (!infile.exists()) {
			infile = FileUtil.getPhotoFile(infile);
		}
		lbFile.setText(infile.getName());
		pbar.setValue(i + 1);
		pbar.setString(i + 1 + "/" + items.size());
		pack();
		setLocationRelativeTo(getParent());
		File outfile = getOutfile(infile);
		String outname = infile.getName();
		boolean rc = false;
		try {
			if (sorter == 4) {
				outname = String.format("%04d.jpg", i + 1);
				outfile = new File(todir, outname);
			}
			if (sorter == 1 || sorter == 3) {
				outfile = new File(todir, outname);
			}
			if (withText) {
				outfile = ExportImage.writeTo(infile, item.commentGet(), todir, outname, compress);
			} else {
				if (compress < 0f) {
					if (!FileUtil.fileCopy(infile, outfile)) {
						LOG.err(TT + "nextFile() infile=" + infile
								+ ", outfile=" + outfile
								+ " copy error");
						rc = false;
						status = false;
						throw new Exception();
					}
				} else {
					outfile = ExportImage.writeTo(infile, "", todir, outname, compress);
				}
			}
			number++;
			rc = true;
		} catch (Exception ex) {
			addReport(Html.intoRed("*** image copy error ***") + "<br>");
			LOG.err("CopyFileDlg.nextFile() error", ex);
			status = false;
			done();
		}
		if (rc) {
			/*addReport(Html.intoGreen(I18N.getMsg("photo.copy_ok",
					new Object[]{
						infile.getName(),
						outfile.getAbsolutePath()})));*/
		} else {
			addReport(Html.intoRed(I18N.getMsg("photo.copy_error",
					new Object[]{infile, outfile.getName()})));
			addReport("<br>");
		}
		outfiles.add(outfile);
		if (autoremove) {
			infile.delete();
		}
	}

	/**
	 * copy done
	 */
	public void done() {
		//LOG.trace(TT + "done()");
		addReport(I18N.getMsg("photo.copy_end", outfiles.size()) + "</p>");
		running = false;
		dispose();
		((AbstractFrame) getParent()).copyEnd();
	}

	/**
	 * Validate and extract normalized date (YYYYMMDD_hhmmss).
	 *
	 * @param name file name without extent
	 * @return a formated String YYYYMMDD_hhmmss if valide, else null
	 */
	private String parseDateFromName(String name) {
		if (name == null) {
			return null;
		}
		String formatted = name;
		if (name.matches("^\\d{6}_\\d{6}$")) {
			formatted = "20" + name;
		} else if (!name.matches("^\\d{8}_\\d{6}$")) {
			return null;
		}
		try {
			String[] parts = formatted.split("_");
			int mm = Integer.parseInt(parts[0].substring(4, 6));
			int dd = Integer.parseInt(parts[0].substring(6, 8));
			if (mm < 1 || mm > 12 || dd < 1 || dd > 31) {
				return null;
			}

			int hh = Integer.parseInt(parts[1].substring(0, 2));
			int min = Integer.parseInt(parts[1].substring(2, 4));
			int ss = Integer.parseInt(parts[1].substring(4, 6));
			if (hh < 0 || hh > 23 || min < 0 || min > 59 || ss < 0 || ss > 59) {
				return null;
			}
			return formatted;
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * get target file
	 *
	 * @param infile
	 * @return
	 */
	private File getOutfile(File infile) {
		String nameWithoutExt = FileUtil.getFileNameWithoutExt(infile);
		String date = parseDateFromName(nameWithoutExt);
		if (date == null) {
			String extension = FileUtil.getExtension(infile).toLowerCase();
			if ("webp".equals(extension)) {
				date = Webp.getDate(infile);
			} else {
				date = Jpeg.getDate(infile);
			}
		}
		if (sorter < 3) {
			if (date != null && date.length() >= 8) {
				String year = date.substring(0, 4);
				String month = date.substring(4, 6);
				String day = date.substring(6, 8);
				String relativePath = year + File.separator + month + File.separator + day;
				boolean isNameValid = nameWithoutExt.matches("^(\\d{6}|\\d{8})_\\d{6}$");
				String targetName = isNameValid ? infile.getName() : date + ".jpg";
				return new File(todir, relativePath + File.separator + targetName);
			}
		}
		return new File(todir, infile.getName());
	}

	public List<File> getOutfiles() {
		return outfiles;
	}

	public void setCompress(int value) {
		switch (value) {
			case 1:
				compress = 0.75f;
				break;
			case 2:
				compress = 0.5f;
				break;
			default:
				compress = -1f;
				break;
		}
	}

	public int getNumber() {
		return number;
	}

	public static class CopyAction implements Runnable {

		private final CopyFileDlg dlg;

		public CopyAction(CopyFileDlg dlg) {
			this.dlg = dlg;
		}

		@Override
		@SuppressWarnings("SleepWhileInLoop")
		public void run() {
			try {
				for (int i = 0; i < dlg.items.size(); i++) {
					dlg.nextFile(i);
					dlg.repaint();
					Thread.sleep(1);
				}
			} catch (InterruptedException ex) {
				LOG.err(TT + "run() error", ex);
			}
			dlg.done();
		}
	}
}
