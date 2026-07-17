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
package app.export;

import api.mig.MIG;
import api.mig.swing.MigLayout;
import app.album.AlbumItem;
import app.album.AlbumTable;
import app.print.Print;
import app.ui.AbstractFrame;
import app.ui.MainFrame;
import app.xml.Xml;
import i18n.I18N;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import tools.FFmpeg;
import tools.Html;
import tools.LOG;
import tools.Ui;
import tools.file.CopyFileDlg;
import tools.file.EnvUtil;
import tools.file.FileUtil;

/**
 * JDialog to copy multiple images to a destination folder
 *
 * @author favdb
 */
public class Export extends AbstractFrame {

	private static final String TT = "Export.";

	private final AlbumTable table;
	private JTextField tfFolder;
	private JButton btExec;
	private JComboBox cbFormat, cbCompress;
	private File dirDest;
	private final MainFrame mainFrame;
	private Container pane;
	private CopyFileDlg copyDlg;
	private List<AlbumItem> items;
	public static final String FORMAT_SIMPLE = I18N.getMsg("export.format.simple"),
			FORMAT_HTML = I18N.getMsg("export.format.html"),
			FORMAT_EPUB = I18N.getMsg("export.format.epub"),
			FORMAT_MPEG = I18N.getMsg("export.format.mpeg"),
			FORMAT_CONTACT = I18N.getMsg("export.format.contact");
	private static final String FORMAT[] = {
		FORMAT_SIMPLE, FORMAT_HTML, FORMAT_EPUB, FORMAT_MPEG, FORMAT_CONTACT
	};
	public static final int COMPRESS_NONE = 0, COMPRESS_MINI = 1, COMPRESS_MAXI = 0;
	private static final String COMPRESS[] = {
		I18N.getMsg("export.compress.none"),
		I18N.getMsg("export.compress.mini"),
		I18N.getMsg("export.compress.maxi")
	};
	private int tempo = 5;
	private JTextField tfTempo;
	private JCheckBox ckGeneric;
	private JPanel pTempo, pFolder, pCompress;
	private Print print;

	/**
	 * call the function
	 *
	 * @param mainFrame
	 */
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Export(MainFrame mainFrame) {
		super();
		this.mainFrame = mainFrame;
		this.table = mainFrame.albumGet().getTable();
		initialize();
	}

	public MainFrame getMainFrame() {
		return mainFrame;
	}

	/**
	 * initialize the JDialog
	 */
	@Override
	public void initialize() {
		//LOG.trace(TT + "initialize()");
		this.setLayout(new MigLayout(MIG.FILL));
		Dimension sz = Toolkit.getDefaultToolkit().getScreenSize();
		this.setMaximumSize(sz);
		pane = this.getContentPane();
		pane.add(initTop(), MIG.get(MIG.SPAN, MIG.GROWX));
		taInfos = new JTextPane();
		taInfos.setContentType("text/html");
		initInfos(I18N.getMsg("export.home"));
		JScrollPane scroll = new JScrollPane(taInfos);
		scroll.setPreferredSize(new Dimension(1024, 768));
		pane.add(scroll, MIG.get(MIG.SPAN, MIG.GROW, MIG.CENTER));
		cbFormat.addItemListener((ItemEvent e) -> {
			pTempo.setVisible(true);
			pFolder.setVisible(true);
			pCompress.setVisible(true);
			String n = (String) cbFormat.getSelectedItem();
			pTempo.setVisible(n.equals(FORMAT_MPEG));
			if (n.equals(FORMAT_CONTACT)) {
				pTempo.setVisible(false);
				pFolder.setVisible(false);
				pCompress.setVisible(false);
				btExec.setEnabled(true);
			} else {
				btExec.setEnabled(!tfFolder.getText().isEmpty());
			}
		});

	}

	/**
	 * initialize the upper JPanel
	 *
	 * @return
	 */
	private JPanel initTop() {
		//LOG.trace(TT + "initTop()");
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.HIDEMODE3, MIG.FILL)));
		p.add(initFormat(), MIG.get(MIG.SPAN, MIG.SPLIT2));
		p.add(pCompress = initCompress(), MIG.get(MIG.SPAN));
		p.add(pTempo = initTempo(), MIG.SPAN);
		p.add(pFolder = initFolder());
		btExec = new JButton(I18N.getMsg("export"));
		btExec.setIcon(IconUtil.getIconSmall(ICONS.K.COGS));
		btExec.setEnabled(!tfFolder.getText().isEmpty());
		btExec.addActionListener(e -> copyBegin());
		p.add(btExec, MIG.get(MIG.SPAN, MIG.RIGHT));
		return p;
	}

	/**
	 * initialize the selection folder
	 *
	 * @return
	 */
	private JPanel initFolder() {
		//LOG.trace(TT + "initFolder()");
		JPanel p2 = new JPanel(new MigLayout());
		p2.add(new JLabel(I18N.getColonMsg("export.dest")), MIG.get(MIG.SPAN, MIG.SPLIT + " 3"));
		tfFolder = new JTextField();
		tfFolder.setColumns(32);
		tfFolder.setEditable(false);
		p2.add(tfFolder);
		JButton bt = Ui.initIconButton("btFolder", ICONS.K.FOLDER, (java.awt.event.ActionEvent evt) -> {
			String dir = tfFolder.getText();
			if (dir.isEmpty()) {
				dir = EnvUtil.getHomeDir().getAbsolutePath();
			}
			JFileChooser chooser = new JFileChooser(dir);
			chooser.setFileSelectionMode(1);
			if (chooser.showOpenDialog(null) != 0) {
				return;
			}
			File file = chooser.getSelectedFile();
			if (file.exists()) {
				tfFolder.setText(file.getAbsolutePath());
			} else {
				tfFolder.setText("");
			}
			btExec.setEnabled(!tfFolder.getText().isEmpty());
		});
		bt.setMargin(new Insets(0, 0, 0, 0));
		p2.add(bt);
		return p2;
	}

	/**
	 * initialize the export format
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private JPanel initFormat() {
		//LOG.trace(TT + "initFormat()");
		JPanel p0 = new JPanel(new MigLayout(MIG.HIDEMODE3));
		p0.add(new JLabel(I18N.getColonMsg("export.format")));
		p0.add(cbFormat = new JComboBox(FORMAT));
		if (!FFmpeg.isInstalled()) {
			cbFormat.removeItemAt(3);
		}
		return p0;
	}

	private JPanel initTempo() {
		//LOG.trace(TT + "initTempo()");
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0)));
		p.add(new JLabel(I18N.getColonMsg("export.format.mpeg_tempo")));
		JButton btminus;
		p.add(btminus = Ui.initButton("minus", ICONS.K.NONE, e -> addTempo(-1)));
		btminus.setText("▼");
		p.add(tfTempo = new JTextField());
		tfTempo.setColumns(2);
		tfTempo.setHorizontalAlignment(JTextField.CENTER);
		JButton btplus;
		p.add(btplus = Ui.initButton("plus", ICONS.K.NONE, e -> addTempo(1)));
		btplus.setText("▲");
		ckGeneric = new JCheckBox(I18N.getMsg("export.format.mpeg_generic"));
		p.add(ckGeneric);
		addTempo(0);
		p.setVisible(false);
		return p;
	}

	/**
	 * for a mpeg export select the tempo
	 *
	 * @param value
	 */
	private void addTempo(int value) {
		//LOG.trace(TT + "addTempo()");
		tempo += value;
		if (tempo < 1) {
			tempo = 1;
		}
		tfTempo.setText("" + tempo);
	}

	/**
	 * initialize the compress level selection
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private JPanel initCompress() {
		//LOG.trace(TT + "initCompress()");
		JPanel p = new JPanel(new MigLayout());
		p.add(new JLabel(I18N.getColonMsg("export.compress")));
		p.add(cbCompress = new JComboBox(COMPRESS));
		return p;
	}

	/**
	 * set infos text
	 *
	 * @param txt
	 */
	public void initInfos(String txt) {
		//LOG.trace(TT + "initInfos(txt)");
		taInfos.setText(Html.intoHtml(txt));
		taInfos.setCaretPosition(taInfos.getDocument().getLength());
	}

	/**
	 * add text to infos
	 *
	 * @param text
	 */
	public void addInfos(String text) {
		initInfos(getInfos() + text);
		taInfos.revalidate();
	}

	/**
	 * get the infos body
	 *
	 * @return
	 */
	public String getInfos() {
		return Html.getBody(taInfos.getText());
	}

	/**
	 * get the list of AlbumItem
	 *
	 * @return
	 */
	public List<AlbumItem> getItems() {
		return items;
	}

	/**
	 * begin the copy process
	 */
	@Override
	public void copyBegin() {
		//LOG.trace(TT + "doExec()");
		String format = (String) cbFormat.getSelectedItem();
		dirDest = new File(tfFolder.getText());
		if (format.equals(FORMAT_EPUB)) {
			dirDest = new File(dirDest, File.separator + "EPUB" + File.separator + "OEBPS");
		} else if (format.equals(FORMAT_MPEG)) {
			dirDest = new File(dirDest, File.separator + "images");
		}
		dirDest.mkdirs();
		if (format.equals(FORMAT_MPEG) && ckGeneric.isSelected()) {
			makeFFmpegBegin();
		}
		items = new ArrayList<>();
		for (int i = 0; i < table.getRowCount(); i++) {
			File src = (File) table.getValueAt(i, 1);
			String text = (String) table.getValueAt(i, 2);
			items.add(new AlbumItem(i + 1, text, src));
		}
		Dimension dim = null;
		boolean istext = (cbFormat.getSelectedItem().equals(FORMAT_MPEG)), isremove = false;
		btExec.setEnabled(false);
		copyDlg = new CopyFileDlg(this, items, istext, dirDest, -1, isremove, null);
		copyDlg.setCompress(cbCompress.getSelectedIndex());
		if (cbFormat.getSelectedItem().equals(FORMAT_MPEG)) {
			copyDlg.setSorter(4);
		}
		copyDlg.start();
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
	}

	/**
	 * end te copy process
	 */
	@Override
	public void copyEnd() {
		//LOG.trace(TT + "doExecSuite() format=" + cbFormat.getSelectedIndex());
		if (!copyDlg.isOK()) {
			return;
		}
		initInfos(getInfos() + copyDlg.getReport());
		String format = (String) cbFormat.getSelectedItem();
		if (FORMAT_SIMPLE.equals(format)) {
			makeSimple();
		} else if (FORMAT_HTML.equals(format)) {
			makeHTML();
		} else if (FORMAT_EPUB.equals(format)) {
			makeEPUB();
		} else if (FORMAT_MPEG.equals(format)) {
			try {
				String fx = FileUtil.removeExtension(mainFrame.albumGet().diapoNameGet());
				File mp4 = new File(dirDest.getParentFile(), fx + ".mp4");
				makeFFmpeg(dirDest.getParentFile(), mp4.getAbsolutePath());
			} catch (IOException ex) {
				LOG.err(TT + "getName() makeFFmpeg error", ex);
			}
		} else if (FORMAT_CONTACT.equals(format)) {
			//nothing
		}
	}

	/**
	 * make an EPUB file
	 */
	private void makeEPUB() {
		//LOG.trace(TT + "makeEPUB()");
		addInfos("<br>" + I18N.getMsg("export.format.epub_make"));
		dirDest = new File(tfFolder.getText());
		String fx = FileUtil.removeExtension(mainFrame.albumGet().diapoNameGet());
		ExportEPUB.create(this, dirDest);
		addInfos(" " + I18N.getMsg("task.ok") + "</p>");
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		btExec.setEnabled(true);
	}

	/**
	 * make a MP4 file
	 *
	 * @param dir
	 * @param outfile
	 * @throws IOException
	 */
	public void makeFFmpeg(File dir, String outfile) throws IOException {
		//LOG.trace(TT + "makeFFmpeg(dir=" + dir.getAbsolutePath() + ", outfile=" + outfile + ")");
		File file = new File(dirDest, String.format("%04d.jpg", copyDlg.getNumber() + 1));
		makeFFmpegEnd(file, I18N.getMsg("export.end"));
		String command = String.format(
				"ffmpeg "
				+ "-framerate 1/" + tempo + " "
				+ "-pattern_type glob "
				+ "-i '%s/*.jpg' "
				+ "-c:v libx264 "
				+ "-r 30 "
				+ "-vf \""
				+ "scale=1280:720:force_original_aspect_ratio=decrease,"
				+ " pad=1280:720:(ow-iw)/2:(oh-ih)/2"
				+ "\" "
				+ "-pix_fmt yuv420p %s -y",
				dirDest.getAbsolutePath(), outfile);
		addInfos("<p>" + I18N.getMsg("export.format.mpeg_make") /*+ ":<br>" + command*/ + " ... ");
		ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
		Process process = processBuilder.start();
		try {
			process.waitFor();
			FileUtil.dirDelete(dirDest);
			addInfos(I18N.getMsg("task.ok") + "</p>");
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			btExec.setEnabled(true);
		} catch (InterruptedException e) {
			LOG.err(TT + "makeFFmpeg(...) call error\n", e);
			addInfos(I18N.getMsg("task.error", e.getLocalizedMessage()) + "</p>");
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			btExec.setEnabled(true);
		}
	}

	/**
	 * write an 800x600 MPEG4 with FFMpeg
	 *
	 */
	public void makeFFmpegBegin() {
		try {
			int width = 800, height = 600;
			String text = mainFrame.diapoTitleGet();
			BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = bufferedImage.createGraphics();
			g2d.setColor(Color.BLACK);
			g2d.fillRect(0, 0, width, height);
			g2d.setColor(Color.WHITE);
			g2d.setFont(new Font("Arial", Font.BOLD, 24));
			FontMetrics fontMetrics = g2d.getFontMetrics();
			int textWidth = fontMetrics.stringWidth(text), textHeight = fontMetrics.getAscent();
			int x = (width - textWidth) / 2, y = (height + textHeight) / 2;
			g2d.drawString(text, x, y);
			g2d.dispose();
			ImageIO.write(bufferedImage, "jpg", new File(dirDest, "0000.jpg"));
		} catch (IOException ex) {
			LOG.err(TT + "makeFFmpegBegin() error", ex);
		}
	}

	/**
	 * end writing a FFMpeg
	 *
	 * @param output
	 * @param text
	 * @throws IOException
	 */
	public void makeFFmpegEnd(File output, String text) throws IOException {
		int width = 800, height = 600;
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bufferedImage.createGraphics();
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, width, height);
		g2d.setColor(Color.WHITE);
		g2d.setFont(new Font("Arial", Font.BOLD, 24));
		FontMetrics fontMetrics = g2d.getFontMetrics();
		int textWidth = fontMetrics.stringWidth(text), textHeight = fontMetrics.getAscent();
		int x = (width - textWidth) / 2, y = (height + textHeight) / 2;
		g2d.drawString(text, x, y);
		g2d.dispose();
		ImageIO.write(bufferedImage, "jpg", output);
	}

	/**
	 * make a HTML caroussel
	 */
	private void makeHTML() {
		//LOG.trace(TT + "makeHTML()");
		addInfos("<br>" + I18N.getMsg("export.format.html_make"));
		ExportHTML html = new ExportHTML(this, dirDest);
		html.begin(items);
		addInfos(I18N.getMsg("task.ok") + "</p>");
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		btExec.setEnabled(true);
	}

	/**
	 * add an album with XML file
	 */
	private void makeSimple() {
		//LOG.trace(TT + "makeSimple()");
		addInfos("<br>" + I18N.getMsg("export.format.simple_make"));
		String fx = FileUtil.removeExtension(mainFrame.albumGet().diapoNameGet());
		File outfile = new File(dirDest, fx + ".xml");
		String existingPrint = "";
		if (outfile != null && outfile.exists()) {
			try {
				String content = FileUtil.fileReadAsString(outfile);
				int start = content.indexOf("<print>");
				int end = content.indexOf("</print>");
				if (start != -1 && end != -1) {
					existingPrint = content.substring(start, end + 9) + "\n";
				}
			} catch (Exception e) {
				LOG.err(TT + "save() Erreur lecture sauvegarde <prints>", e);
			}
		}
		StringBuilder b = new StringBuilder(Xml.getHeader())
				.append("<album>\n")
				.append(Xml.indent(1, "<list>\n"));
		for (AlbumItem item : items) {
			b.append(itemToXml(item));
		}
		b.append(Xml.indent(1, "</list>\n"));
		if (!existingPrint.isEmpty()) {
			b.append(Xml.indent(1, existingPrint));
		}
		b.append("</album>");
		FileUtil.fileWriteString(outfile, b.toString());
		addInfos(" " + I18N.getMsg("task.ok") + "</p>");
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		btExec.setEnabled(true);
	}

	/**
	 * get a XML item from an AlbumItem
	 *
	 * @param item
	 * @return
	 */
	private String itemToXml(AlbumItem item) {
		String file = item.file.getName();
		return Xml.indent(3, "<item "
				+ "id=\"" + String.format("%03d", item.id + 1) + "\" "
				+ "file=\"" + file + "\" "
				+ "comment=\"" + item.text + "\" "
				+ " />\n");
	}

}
