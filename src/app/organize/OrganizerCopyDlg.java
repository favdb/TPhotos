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
package app.organize;

import app.AbstractFrame;
import app.App;
import app.export.ExportImage;
import app.i18n.I18N;
import app.media.Jpeg;
import app.media.MP4;
import app.media.Media;
import app.tools.GBC;
import app.tools.Html;
import app.tools.LOG;
import app.tools.file.FileUtil;
import app.xml.XmlAlbumItem;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * copy files dialog
 *
 * @author favdb
 */
public class OrganizerCopyDlg extends JDialog {

	private static final String TT = "CopyFileDlg.";

	private final File todir;
	private boolean running = true;
	private JLabel lbFile;
	private JProgressBar pbar;
	private boolean autoremove;
	private final Dimension dim;
	private final List<File> outfiles = new ArrayList<>();
	private boolean status = true;
	public List<XmlAlbumItem> items;
	private boolean withText;
	private float compress = -1f;
	private int number = 0;

	/**
	 * OrganizeCopyFileDlg
	 *
	 * @param parent: parent JFrame
	 * @param items
	 * @param withText
	 * @param todir: destination directory
	 * @param autoremove: remove file after copy
	 * @param dim: new size for the image, may be null for no resize
	 */
	public OrganizerCopyDlg(AbstractFrame parent,
			List<XmlAlbumItem> items,
			boolean withText,
			File todir,
			boolean autoremove,
			Dimension dim) {
		super(parent, false);
		this.items = items;
		this.todir = todir;
		this.withText = withText;
		this.autoremove = autoremove;
		this.dim = dim;
		initialize();
	}

	/**
	 * set list of AlbumItem
	 *
	 * @param items
	 */
	public void itemsSet(List<XmlAlbumItem> items) {
		this.items = items;
	}

	/**
	 * set autoremove option
	 */
	public void autoremoveSet() {
		autoremove = true;
	}

	/**
	 * set withText parameter
	 */
	public void withTextSet() {
		withText = true;
	}

	/**
	 * initialize dialog
	 */
	private void initialize() {
		setTitle(I18N.getMsg("organize.inprogress"));
		setLayout(new GridBagLayout());

		add(new JLabel(), new GBC("0, 0, left, ins 2"));

		lbFile = new JLabel();
		int c = App.fontGet().getSize();
		lbFile.setMinimumSize(new Dimension(c * 32, c));
		add(lbFile, new GBC("1, 0, left, ins 2"));

		pbar = new JProgressBar();
		pbar.setMaximum(items.size());
		pbar.setMinimumSize(new Dimension(c * 20, c));
		pbar.setStringPainted(true);
		pbar.setString("0/" + items.size());
		add(pbar, new GBC("2, 0, growx, wx 1.0, ins 5"));

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
		running = true;
		status = true;
		setVisible(true);
		for (XmlAlbumItem item : items) {
			OrganizerPath target = getOutfile(item.fileGet());
			File parentFolder = target.getDestination().getParentFile();
			if (parentFolder != null && !parentFolder.exists()) {
				parentFolder.mkdirs();
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
		OrganizerPath target = getOutfile(infile);
		File outfile = target.getDestination();
		String outname = outfile.getName();
		boolean rc = false;
		try {
			if (withText) {
				outfile = ExportImage.writeTo(infile,
						item.commentGet(),
						outfile.getParentFile(),
						outname,
						compress);
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
					outfile = ExportImage.writeTo(infile, "",
							outfile.getParentFile(),
							outname,
							compress);
				}
			}
			number++;
			rc = true;
		} catch (Exception ex) {
			addReport(Html.intoRed("*** file copy error ***") + "<br>");
			LOG.err(TT + "nextFile() error", ex);
			status = false;
			done();
		}
		if (!rc) {
			addReport(Html.intoRed(I18N.getMsg("photo.copy_error",
					new Object[]{infile, outfile.getName()})));
			addReport("<br>");
		}
		outfiles.add(outfile);
		if (autoremove && rc) {
			infile.delete();
		}
	}

	/**
	 * copy done
	 */
	public void done() {
		addReport(I18N.getMsg("photo.copy_end", outfiles.size()) + "</p>");
		running = false;
		dispose();
		((AbstractFrame) getParent()).copyEnd();
	}

	/**
	 * Validate and extract normalized date (YYYYMMDD_hhmmss).
	 *
	 * @param name file name without extension
	 * @return a formated String YYYYMMDD_hhmmss if valid, else null
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
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	/**
	 * Build path object containing source, target file, and triage status.
	 *
	 * @param infile
	 * @return OrganizerPath object
	 */
	private OrganizerPath getOutfile(File infile) {
		String nameWithoutExt = FileUtil.getFileNameWithoutExt(infile);
		String ext = FileUtil.getExtension(infile);
		String extSuffix = ext.isEmpty() ? "" : "." + ext;
		if (Media.jpegIs(infile)) {
			extSuffix = ".jpg";
		}
		String dateFromName = parseDateFromName(nameWithoutExt);
		boolean isNameValid = (dateFromName != null);
		String internalDate = null;
		if (Jpeg.hasEXIF(infile)) {
			try {
				Jpeg jpeg = new Jpeg(infile);
				if (jpeg.exif != null) {
					internalDate = jpeg.exif.getDate();
				}
			} catch (Exception ex) {
				LOG.err(TT + "getOutfile(infile=" + infile.toString() + ")", ex);
			}
		} else if (MP4.hasMVHD(infile)) {
			internalDate = MP4.getDate(infile);
		}
		String validDate = isNameValid ? dateFromName : internalDate;
		boolean triage = true;
		File triageDir = new File(todir, "Triage");
		File destFile = new File(triageDir, infile.getName());
		if (validDate != null && validDate.length() >= 8) {
			String year = validDate.substring(0, 4);
			String month = validDate.substring(4, 6);
			String day = validDate.substring(6, 8);
			String relativePath = year + File.separator + month + File.separator + day;
			String targetName = isNameValid ? infile.getName() : validDate + extSuffix;
			destFile = new File(todir, relativePath + File.separator + targetName);
			triage = false;
		}
		return new OrganizerPath(infile, destFile, triage);
	}

	/**
	 * get the outfiles
	 *
	 * @return
	 */
	public List<File> getOutfiles() {
		return outfiles;
	}

	/**
	 * set compress value
	 *
	 * @param value
	 */
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

	/**
	 * get number
	 *
	 * @return
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * CopyAction class
	 */
	public static class CopyAction implements Runnable {

		private final OrganizerCopyDlg dlg;

		public CopyAction(OrganizerCopyDlg dlg) {
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