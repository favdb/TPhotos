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

import api.mig.MIG;
import api.mig.swing.MigLayout;
import app.album.AlbumItem;
import i18n.I18N;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import tools.Html;
import tools.LOG;
import tools.Ui;
import tools.file.CopyFileDlg;
import tools.file.EnvUtil;

/**
 * class to organize the photos folder
 *
 * @author favdb
 */
public class Organise extends AbstractFrame {

	private static final String TT = "Organise.";

	public JTextField tfFolder;
	private JButton btOrganiser;
	public JComboBox cbSorter;
	public JCheckBox ckRemove;
	public static String[] ORGANIZE_BY = {
		I18N.getMsg("organise.by_year"),
		I18N.getMsg("organise.by_month"),
		I18N.getMsg("organise.by_day"),
		I18N.getMsg("organise.by_none")
	};

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Organise() {
		super();
		initialize();
	}

	@Override
	public void initialize() {
		//LOG.trace(TT + "initialize()");
		setLayout(new MigLayout(MIG.FILL));
		setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		Container pane = this.getContentPane();
		pane.add(initTfFolder(), MIG.get(MIG.SPAN, MIG.GROWX));
		taInfosInit("organise.home");
		JScrollPane scroll = new JScrollPane(taInfos);
		scroll.setPreferredSize(new Dimension(1024, 768));
		pane.add(scroll, MIG.get(MIG.SPAN, MIG.GROW, MIG.CENTER));
	}

	@SuppressWarnings("unchecked")
	private JPanel initTfFolder() {
		//LOG.trace(TT + "initTfFolder()");
		JPanel p = new JPanel(new MigLayout(MIG.FILL));
		JPanel p1 = new JPanel(new MigLayout(MIG.FILL));
		p1.add(new JLabel(I18N.getColonMsg("organise.source")), MIG.get(MIG.SPAN, MIG.SPLIT + " 3"));
		tfFolder = new JTextField();
		tfFolder.setColumns(32);
		tfFolder.setEditable(false);
		p1.add(tfFolder);
		JButton bt = Ui.initIconButton("btFolder", ICONS.K.FOLDER, (java.awt.event.ActionEvent evt) -> {
			String dir = tfFolder.getText();
			if (dir.isEmpty()) {
				dir = EnvUtil.getHomeDir().getAbsolutePath();
			}
			JFileChooser chooser = new JFileChooser(dir);
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			if (chooser.showOpenDialog(null) != 0) {
				return;
			}
			File file = chooser.getSelectedFile();
			if (file.isFile()) {
				file = file.getParentFile();
			}
			if (file.exists()) {
				String ndir = file.getAbsolutePath();
				if (tfFolder.getText().equals(ndir)) {
					return;
				}
				tfFolder.setText(ndir);
				int nb = App.jpegCount(file);
				String nbs = I18N.getMsg((nb > 1 ? "files" : "file"));
				taInfosAdd(Html.intoP(String.format("%s : %d %s", file.getAbsolutePath(), nb, nbs)));
			} else {
				tfFolder.setText("");
			}
			btOrganiser.setEnabled(!tfFolder.getText().isEmpty());
		});
		bt.setMargin(new Insets(0, 0, 0, 0));
		p1.add(bt);
		p.add(p1, MIG.SPAN);
		p.add(ckRemove = new JCheckBox(I18N.getMsg("photo.remove")));
		ckRemove.setFont(App.fontGet());
		ckRemove.setSelected(App.preferences.organizeDeleteGet());
		JPanel p2 = new JPanel(new MigLayout(MIG.FILL));
		p2.add(new JLabel(I18N.getColonMsg("organise.by")));
		p2.add(cbSorter = new JComboBox(ORGANIZE_BY));
		cbSorter.setSelectedIndex(App.preferences.organizeTypeGet());
		p2.setBorder(BorderFactory.createEtchedBorder());
		p.add(p2);
		btOrganiser = new JButton(I18N.getMsg("app.organiser"));
		btOrganiser.setIcon(IconUtil.getIconSmall(ICONS.K.COGS));
		btOrganiser.setEnabled(!tfFolder.getText().isEmpty());
		btOrganiser.addActionListener(e -> doCopyBegin());
		p.add(btOrganiser, MIG.get(MIG.SPAN, MIG.RIGHT));
		return p;
	}

	public void refreshFiles() {
		//LOG.trace(TT + "refreshFiles()");
		if (tfFolder == null || tfFolder.getText().isEmpty()) {
			return;
		}
		String infos = taInfosGet();
		File dir = new File(tfFolder.getText());
		int nb = App.jpegCount(dir);
		String line = I18N.getMsg("dir.contains", new String[]{dir.getAbsolutePath(), nb + ""});
		if (nb == 0) {
			line = Html.intoRed(I18N.getMsg("dir.contains_nophoto", dir.getAbsolutePath()));
		}
		taInfosAdd(Html.intoP("<p>" + line + "</p>"));
		btOrganiser.setEnabled(nb > 0);
	}

	/**
	 * begining task for copying files
	 */
	@Override
	public void doCopyBegin() {
		if (tfFolder == null || tfFolder.getText().isEmpty()) {
			return;
		}
		File dir = new File(tfFolder.getText());

		btOrganiser.setEnabled(false);
		setCursor(new Cursor(Cursor.WAIT_CURSOR));

		new SwingWorker<List<File>, String>() {

			@Override
			protected List<File> doInBackground() throws Exception {
				List<File> allFiles = new ArrayList<>();
				scanRecursive(dir, allFiles);
				return allFiles;
			}

			private void scanRecursive(File currentDir, List<File> allFiles) {
				if (currentDir.exists() && currentDir.isDirectory()) {
					publish(currentDir.getAbsolutePath());

					File[] fls = currentDir.listFiles();
					if (fls == null) {
						return;
					}

					for (File f : fls) {
						if (f.isDirectory()) {
							scanRecursive(f, allFiles);
						} else if (f.isFile() && App.jpegIs(f)) {
							allFiles.add(f);
						}
					}
				}
			}

			@Override
			protected void process(List<String> chunks) {
				String lastDir = chunks.get(chunks.size() - 1);
				taInfosAdd(Html.intoP("<i>" + I18N.getMsg("organise.scan") + " : " + lastDir + "</i>"));
			}

			@Override
			protected void done() {
				try {
					List<File> files = get();
					continueCopyProcess(files, dir);
				} catch (Exception e) {
					LOG.err(I18N.getMsg("organise.scan_error"), e);
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					btOrganiser.setEnabled(true);
				}
			}
		}.execute();
	}

	/**
	 * Suite logique du traitement après le scan des fichiers
	 */
	private void continueCopyProcess(List<File> files, File dir) {
		if (files.isEmpty()) {
			taInfosAdd(Html.intoP(Html.intoRed(I18N.getMsg("photo.empty", dir.getAbsolutePath()))));
			return;
		}
		Collections.sort(files, (File f1, File f2)
				-> f1.getAbsolutePath().compareTo(f2.getAbsolutePath()));
		List<AlbumItem> ls = new ArrayList<>();
		int id = 1;
		for (File f : files) {
			ls.add(new AlbumItem(id++, "", f));
		}
		//second step: copy the files to destination
		File destDir = new File(App.preferences.photosDirGet());
		taInfosAdd(Html.intoP(I18N.getMsg("organise.inprogress")));
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		Collections.sort(ls, (AlbumItem f1, AlbumItem f2)
				-> f1.file.getAbsolutePath().compareTo(f2.file.getAbsolutePath()));
		SwingUtilities.invokeLater(() -> {
			CopyFileDlg cpf = new CopyFileDlg(this, ls, false, destDir,
					cbSorter.getSelectedIndex(), ckRemove.isSelected(), null);
			cpf.start();
		});
		btOrganiser.setEnabled(false);
	}

	@Override
	public void doCopyEnd() {
		App.albumRefresh();
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public boolean autoremoveGet() {
		return ckRemove.isSelected();
	}

}
