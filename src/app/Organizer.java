package app;

import api.mig.MIG;
import api.mig.swing.MigLayout;
import app.ui.AbstractFrame;
import app.ui.MainFrame;
import app.xml.XmlAlbumItem;
import i18n.I18N;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import resources.icons.ICONS;
import tools.Html;
import tools.LOG;
import tools.Ui;
import tools.file.CopyFileDlg;
import tools.file.EnvUtil;

/**
 * class to organize the photos folder into a fixed AAAA/MM/JJ structure
 *
 * @author favdb
 */
public class Organizer extends AbstractFrame {

	private static final String TT = "Organizer.";

	public JTextField tfFolder;
	private JButton btOrganizer;
	public JCheckBox ckRemove;
	private final MainFrame mainFrame;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Organizer(MainFrame mainFrame) {
		super();
		this.mainFrame = mainFrame;
		initialize();
	}

	@Override
	public void initialize() {
		setLayout(new MigLayout(MIG.FILL));
		setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		Container pane = this.getContentPane();
		pane.add(initTfFolder(), MIG.get(MIG.SPAN, MIG.GROWX));
		taInfosInit("organize.home");
		JScrollPane scroll = new JScrollPane(taInfos);
		scroll.setPreferredSize(new Dimension(1024, 768));
		pane.add(scroll, MIG.get(MIG.SPAN, MIG.GROW, MIG.CENTER));
	}

	private JPanel initTfFolder() {
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.INS1, MIG.WRAP), "[][grow][]"));
		//source folder
		p.add(new JLabel(I18N.getColonMsg("organize.source")));
		p.add(tfFolder = new JTextField(), MIG.GROW);
		tfFolder.setEditable(false);
		p.add(Ui.initIconButton("btFolder", ICONS.K.FOLDER, e -> selectDest()));
		//option and execute
		JPanel r = new JPanel(new MigLayout(MIG.INS0));
		r.add(ckRemove = new JCheckBox(I18N.getMsg("photo.remove")), MIG.RIGHT);
		ckRemove.setSelected(App.preferences.organizeDeleteGet());
		r.add(btOrganizer = Ui.initButton("app.organizer", ICONS.K.COGS, e -> copyBegin()));
		btOrganizer.setEnabled(!tfFolder.getText().isEmpty());
		p.add(r, MIG.get(MIG.SPAN, MIG.RIGHT));
		return p;
	}

	private void selectDest() {
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
			taInfosAdd(Html.intoP(String.format("%s : %d %s",
					file.getAbsolutePath(), nb, nbs)));
		} else {
			tfFolder.setText("");
		}
		btOrganizer.setEnabled(!tfFolder.getText().isEmpty());
	}

	public void refreshFiles() {
		if (tfFolder == null || tfFolder.getText().isEmpty()) {
			return;
		}
		File dir = new File(tfFolder.getText());
		int nb = App.jpegCount(dir);
		String line = I18N.getMsg("dir.contains",
				new String[]{dir.getAbsolutePath(), nb + ""});
		if (nb == 0) {
			line = Html.intoRed(I18N.getMsg("dir.contains_nophoto",
					dir.getAbsolutePath()));
		}
		taInfosAdd(Html.intoP(line));
		btOrganizer.setEnabled(nb > 0);
	}

	//****************************************
	//** copying process                    **
	//** - step 1: collect files to process **
	//** - step 2: process files            **
	//****************************************
	/**
	 * Structural class to publish progress from doInBackground to process
	 */
	private static class ProgressInfo {

		final String currentDir;
		final int count;

		ProgressInfo(String currentDir, int count) {
			this.currentDir = currentDir;
			this.count = count;
		}
	}

	/**
	 * begin copying, step 1 collect files
	 */
	@Override
	public void copyBegin() {
		if (tfFolder == null || tfFolder.getText().isEmpty()) {
			return;
		}
		File dir = new File(tfFolder.getText());
		btOrganizer.setEnabled(false);
		setWaitingCursor();
		taInfosAdd(Html.intoP("<b>" + I18N.getMsg("organize.scan") + " : "
				+ dir.getAbsolutePath() + " ...</b>"));
		new SwingWorker<List<File>, ProgressInfo>() {

			@Override
			protected List<File> doInBackground() throws Exception {
				List<File> allFiles = new ArrayList<>();
				scanRecursive(dir, allFiles);
				return allFiles;
			}

			private void scanRecursive(File currentDir, List<File> allFiles) {
				if (currentDir.exists() && currentDir.isDirectory()) {
					publish(new ProgressInfo(currentDir.getAbsolutePath(), allFiles.size()));
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
			protected void process(List<ProgressInfo> chunks) {
				ProgressInfo last = chunks.get(chunks.size() - 1);
				taInfosAdd(Html.intoP("<i>" + tfFolder.getText()
						+ " (" + last.count + " "
						+ I18N.getMsg(last.count > 1 ? "files" : "file")
						+ ")</i>"));
			}

			@Override
			protected void done() {
				try {
					List<File> files = get();
					taInfosAdd(Html.intoP("<b>" + I18N.getMsg("organize.scan")
							+ " "
							+ I18N.getColonMsg("organize.scan_end") + files.size()
							+ " "
							+ I18N.getMsg(files.size() > 1 ? "files" : "file")
							+ " "
							+ I18N.getMsg("organize.scan_find")
							+ "</b>"));
					if (files.isEmpty()) {
						setNormalCursor();
						btOrganizer.setEnabled(true);
					}
					continueToOrganize(files, dir);
				} catch (Exception e) {
					LOG.err(I18N.getMsg("organize.scan_error"), e);
					setNormalCursor();
					btOrganizer.setEnabled(true);
				}
			}
		}.execute();
	}

	/**
	 * copy given list of files to the given folder
	 *
	 * @param files
	 * @param dir
	 */
	private void continueToOrganize(List<File> files, File dir) {
		if (files.isEmpty()) {
			taInfosAdd(Html.intoP(Html.intoRed(I18N.getMsg("photo.empty",
					dir.getAbsolutePath()))));
			return;
		}
		//sort files by absolute path
		Collections.sort(files, (File f1, File f2)
				-> f1.getAbsolutePath().compareTo(f2.getAbsolutePath()));
		List<XmlAlbumItem> ls = new ArrayList<>();
		int id = 1;
		for (File f : files) {
			ls.add(new XmlAlbumItem("" + (id++), f.getAbsolutePath(), ""));
		}

		File destDir = new File(App.preferences.photosDirGet());
		taInfosAdd(Html.intoP(I18N.getMsg("organize.inprogress")));
		setWaitingCursor();
		Collections.sort(ls, (XmlAlbumItem f1, XmlAlbumItem f2)
				-> f1.fileGet().getAbsolutePath().compareTo(f2.fileGet().getAbsolutePath()));
		SwingUtilities.invokeLater(() -> {
			// Mode 2 passe la structure cible en AAAA/MM/JJ obligatoire
			CopyFileDlg cpf = new CopyFileDlg(this, ls, false, destDir,
					0, ckRemove.isSelected(), null);
			cpf.start();
		});
		btOrganizer.setEnabled(false);
	}

	/**
	 * end copying
	 */
	@Override
	public void copyEnd() {
		mainFrame.albumGet().refreshAll();
		setNormalCursor();
	}

	/**
	 * get the autoremove status
	 *
	 * @return
	 */
	public boolean autoremoveGet() {
		return ckRemove.isSelected();
	}

}
