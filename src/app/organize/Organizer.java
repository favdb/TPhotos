package app.organize;

import app.AbstractFrame;
import app.App;
import app.MainFrame;
import app.i18n.I18N;
import app.media.Media;
import app.resources.icons.ICONS;
import app.tools.GBC;
import app.tools.Html;
import app.tools.LOG;
import app.tools.Ui;
import app.tools.file.EnvUtil;
import app.tools.file.FileUtil;
import app.xml.XmlAlbumItem;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Class to organize the photos/videos folder into a fixed AAAA/MM/JJ structure
 *
 * @author favdb
 */
public class Organizer extends AbstractFrame {

	private static final String TT = "Organizer.";

	private final MainFrame mainFrame;
	public JTextField tfFolder;
	private JButton btStart;
	public JCheckBox ckRemove, ckVideo;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Organizer(MainFrame mainFrame) {
		super();
		this.mainFrame = mainFrame;
		initialize();
	}

	@Override
	public void initialize() {
		setLayout(new GridBagLayout());
		setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
		Container pane = this.getContentPane();
		pane.add(initTop(), new GBC("0,0, growx, wx 1.0, ins 5"));
		taInfosInit("init");
		taInfos.setText(FileUtil.readHtml("Organizer"));
		JScrollPane scroll = new JScrollPane(taInfos);
		scroll.setPreferredSize(new Dimension(1024, 768));
		pane.add(scroll, new GBC("1,0, grow, wx 1.0, wy 1.0, ins 5"));
	}

	/**
	 * initialize top part
	 *
	 * @return
	 */
	private JPanel initTop() {
		JPanel p = new JPanel(new GridBagLayout());
		// source folder
		p.add(new JLabel(I18N.getColonMsg("organize.source")),
				new GBC("0,0, left, ins 2"));
		tfFolder = new JTextField();
		tfFolder.setEditable(false);
		p.add(tfFolder, new GBC("0,1, growx, wx 1.0, ins 2"));
		p.add(Ui.initIconButton("directory.select", ICONS.K.FOLDER,
				e -> selectFolder()), new GBC("0,2, ins 2"));
		JPanel r = new JPanel(new GridBagLayout());
		// option to include videos
		ckVideo = new JCheckBox(I18N.getMsg("organize.video"));
		ckVideo.setSelected(App.pref.organizeVideoGet());
		r.add(ckVideo, new GBC("0,0, right, ins 2"));
		// option to remove original files (photos or videos)
		ckRemove = new JCheckBox(I18N.getMsg("organize.remove"));
		ckRemove.setSelected(App.pref.organizeDeleteGet());
		r.add(ckRemove, new GBC("0,1, right, ins 2"));
		// execute button
		btStart = Ui.initButton("organize.start", ICONS.K.COGS, e -> copyBegin());
		btStart.setEnabled(!tfFolder.getText().isEmpty());
		r.add(btStart, new GBC("0,2, right, ins 2"));
		p.add(r, new GBC("1,0, gw 3, right, ins 2"));
		return p;
	}

	/**
	 * select the source folder
	 */
	private void selectFolder() {
		String srcDir = tfFolder.getText();
		if (srcDir.isEmpty()) {
			srcDir = EnvUtil.getHomeDir().getAbsolutePath();
		}
		JFileChooser chooser = new JFileChooser(srcDir);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		if (chooser.showOpenDialog(null) != 0) {
			return;
		}
		File file = chooser.getSelectedFile();
		if (file.isFile()) {
			file = file.getParentFile();
		}
		if (file.exists()) {
			tfFolder.setText(file.getAbsolutePath());
			refreshFiles();
		} else {
			tfFolder.setText("");
		}
		btStart.setEnabled(!tfFolder.getText().isEmpty());
	}

	/**
	 * refresh files to count number of files (photos or videos)
	 */
	public void refreshFiles() {
		if (tfFolder == null || tfFolder.getText().isEmpty()) {
			return;
		}
		File dir = new File(tfFolder.getText());
		Media.MediaCount mc = Media.count(dir);
		taInfosAdd(Html.intoP(String.format("%s : %d photos, %d vidéos",
				dir.getAbsolutePath(), mc.jpegCount, mc.mp4Count)));
		btStart.setEnabled(mc.totalGet() > 0);
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

		final int count;

		ProgressInfo(String currentDir, int count) {
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
		btStart.setEnabled(false);
		setWaitingCursor();
		taInfosAdd(Html.intoP("<b>" + I18N.getMsg("organize.scan") + " : "
				+ dir.getAbsolutePath() + " ...</b>"));
		final boolean includeVideo = ckVideo.isSelected();
		new SwingWorker<List<File>, ProgressInfo>() {

			@Override
			protected List<File> doInBackground() throws Exception {
				List<File> allFiles = new ArrayList<>();
				scanRecursive(dir, allFiles, includeVideo);
				return allFiles;
			}

			private void scanRecursive(File currentDir, List<File> allFiles, boolean includeVideo) {
				if (currentDir.exists() && currentDir.isDirectory()) {
					publish(new ProgressInfo(currentDir.getAbsolutePath(), allFiles.size()));
					File[] fls = currentDir.listFiles();
					if (fls == null) {
						return;
					}
					for (File f : fls) {
						if (f.isDirectory()) {
							scanRecursive(f, allFiles, includeVideo);
						} else if (f.isFile()) {
							if (Media.jpegIs(f) || (includeVideo && Media.mp4Is(f))) {
								allFiles.add(f);
							}
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
						btStart.setEnabled(true);
						return;
					}
					continueToOrganize(files, dir);
				} catch (InterruptedException | ExecutionException e) {
					LOG.err(I18N.getMsg("organize.scan_error"), e);
					setNormalCursor();
					btStart.setEnabled(true);
				}
			}
		}.execute();
	}

	/**
	 * step 2 - copy given list of files to the target folder structure
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
		Collections.sort(files, (File f1, File f2)
				-> f1.getAbsolutePath().compareTo(f2.getAbsolutePath()));
		List<XmlAlbumItem> ls = new ArrayList<>();
		int id = 1;
		for (File f : files) {
			ls.add(new XmlAlbumItem("" + (id++), f.getAbsolutePath(), ""));
		}
		File destDir = new File(App.pref.photosDirGet());
		taInfosAdd(Html.intoP(I18N.getMsg("organize.inprogress")));
		setWaitingCursor();
		SwingUtilities.invokeLater(() -> {
			OrganizerCopyDlg cpf = new OrganizerCopyDlg(this, ls, false, destDir,
					0, ckRemove.isSelected(), null);
			cpf.start();
		});
		btStart.setEnabled(false);
	}

	/**
	 * end copying
	 */
	@Override
	public void copyEnd() {
		mainFrame.albumGet().refreshAll();
		setNormalCursor();
	}

}
