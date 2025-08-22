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
package tools.file;

import api.mig.swing.MigLayout;
import app.AbstractFrame;
import app.App;
import app.album.AlbumItem;
import app.export.ExportImage;
import i18n.I18N;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import tools.Html;
import tools.LOG;
import tools.MIG;
import tools.jpeg.Jpeg;

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
	public List<AlbumItem> items;
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
	 * @param sorter: 0,1,2 date subdirectory, -1 or greater than 2=no subdirectory
	 * @param autoremove: remove file after copy
	 * @param dim: new size for the image, may be null for no resize
	 */
	public CopyFileDlg(AbstractFrame parent,
			List<AlbumItem> items, boolean withText,
			File todir, int sorter, boolean autoremove, Dimension dim) {
		super(parent, false);
		this.items = items;
		this.todir = todir;
		this.withText = withText;
		this.sorter = sorter;
		this.autoremove = autoremove;
		this.dim = dim;
		initialize();
	}

	public void setItems(List<AlbumItem> items) {
		this.items = items;
	}

	public void setAutoremove() {
		autoremove = true;
	}

	public void setWithText() {
		withText = true;
	}

	public void setDim(Dimension dim) {
		this.dim = dim;
	}

	public void setSorter(int mode) {
		this.sorter = mode;
	}

	private void initialize() {
		setTitle("copying files");
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

	public boolean isRunning() {
		return running;
	}

	public boolean isOK() {
		return status;
	}

	public String getReport() {
		//return report.toString();
		return "";
	}

	private void addReport(String text) {
		((AbstractFrame) getParent()).taInfosAdd(text);
	}

	public void start() {
		//LOG.trace(TT + "start() todir=" + todir.getAbsolutePath());
		running = true;
		status = true;
		setVisible(true);
		//create all folders
		File fparent = null;
		for (AlbumItem item : items) {
			if (!getOutfile(item.file).getParentFile().equals(fparent)) {
				fparent = getOutfile(item.file).getParentFile();
				fparent.mkdirs();
			}
		}
		new Thread(new CopyAction(this)).start();
	}

	private void nextFile(int i) {
		//LOG.trace(TT + "nextFile() i=" + i);
		AlbumItem item = items.get(i);
		File infile = item.file;
		lbFile.setText(infile.getAbsolutePath());
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
			}
			if (sorter == 3) {
				outfile = new File(todir, outname);
			}
			if (withText) {
				outfile = ExportImage.writeTo(infile, item.text, todir, outname, compress);
			} else {
				if (compress < 0f) {
					if (!FileUtil.fileCopy(infile, outfile)) {
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
			addReport(Html.intoGreen(I18N.getMsg("photo.copy_ok", new Object[]{
				infile.getName(),
				outfile.getAbsolutePath()})));
		} else {
			addReport(Html.intoRed(I18N.getMsg("photo.copy_error", new Object[]{infile, outfile.getName()})));
		}
		addReport("<br>");
		outfiles.add(outfile);
		if (autoremove) {
			infile.delete();
		}
	}

	public void done() {
		//LOG.trace(TT + "done()");
		addReport(I18N.getMsg("photo.copy_end", outfiles.size()) + "</p>");
		running = false;
		dispose();
		((AbstractFrame) getParent()).doCopyEnd();
	}

	private File getOutfile(File infile) {
		if (sorter > 2) {
			return todir;
		}
		File outdir = new File(todir, infile.getName());
		if (sorter != -1) {
			String date = Jpeg.getDate(infile);
			if (date != null) {
				StringBuilder subdir = new StringBuilder();
				if (sorter < 3) {
					//allways add year
					subdir.append(date.substring(0, 4)).append(File.separator);
					if (sorter > 0) {//add month
						subdir.append(date.substring(4, 6)).append(File.separator);
					}
					if (sorter > 1) {//add day
						subdir.append(date.substring(6, 8)).append(File.separator);
					}
				}
				outdir = new File(todir, subdir.toString() + date + ".jpg");
			}
		}
		return outdir;
	}

	private String getSubdir(File infile) {
		StringBuilder subdir = new StringBuilder();
		if (sorter != -1) {
			String date = Jpeg.getDate(infile);
			if (date != null) {
				if (sorter < 3) {
					//allways add year
					subdir.append(date.substring(0, 4)).append(File.separator);
					if (sorter > 0) {//add month
						subdir.append(date.substring(4, 6)).append(File.separator);
					}
					if (sorter > 1) {//add day
						subdir.append(date.substring(6, 8)).append(File.separator);
					}
				}
			}
		}
		return subdir.toString();
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
