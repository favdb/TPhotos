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

import api.mig.swing.MigLayout;
import app.AbstractFrame;
import app.MainFrame;
import app.album.AlbumItem;
import app.album.AlbumTable;
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
import tools.MIG;
import tools.Ui;
import tools.file.CopyFileDlg;
import tools.file.EnvUtil;
import tools.file.FileUtil;
import tools.xml.XML;

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
	public static final int FORMAT_SIMPLE = 0, FORMAT_HTML = 1, FORMAT_EPUB = 2, FORMAT_MP4 = 3;
	private static final String FORMAT[] = {
		I18N.getMsg("export.format.simple"),
		I18N.getMsg("export.format.html"),
		I18N.getMsg("export.format.epub"),
		I18N.getMsg("export.format.mpeg")
	};
	public static final int COMPRESS_NONE = 0, COMPRESS_MINI = 1, COMPRESS_MAXI = 0;
	private static final String COMPRESS[] = {
		I18N.getMsg("export.compress.none"),
		I18N.getMsg("export.compress.mini"),
		I18N.getMsg("export.compress.maxi")
	};
	private int tempo = 5;
	private JTextField tfTempo;
	private JPanel pTempo;
	private JCheckBox ckGeneric;

	/**
	 * call the function
	 *
	 * @param mainFrame
	 */
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Export(MainFrame mainFrame) {
		super();
		this.mainFrame = mainFrame;
		this.table = mainFrame.getAlbumPanel().getTable();
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
		this.setLayout(new MigLayout(MIG.FILL));
		Dimension sz = Toolkit.getDefaultToolkit().getScreenSize();
		this.setMaximumSize(sz);
		pane = this.getContentPane();
		pane.add(initUpper(), MIG.get(MIG.SPAN, MIG.GROWX));
		taInfos = new JTextPane();
		taInfos.setContentType("text/html");
		initInfos(I18N.getMsg("export.home"));
		JScrollPane scroll = new JScrollPane(taInfos);
		scroll.setPreferredSize(new Dimension(1024, 768));
		pane.add(scroll, MIG.get(MIG.SPAN, MIG.GROW, MIG.CENTER));
	}

	/**
	 * initialize the upper JPanel
	 *
	 * @return
	 */
	private JPanel initUpper() {
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.HIDEMODE3, MIG.FILL)));
		p.add(initFormat(), MIG.get(MIG.SPAN, MIG.SPLIT2));
		p.add(initCompress(), MIG.get(MIG.SPAN));
		p.add(initTempo(), MIG.SPAN);
		p.add(initTfFolder());
		btExec = new JButton(I18N.getMsg("export"));
		btExec.setIcon(IconUtil.getIconSmall(ICONS.K.COGS));
		btExec.setEnabled(!tfFolder.getText().isEmpty());
		btExec.addActionListener(e -> doCopyBegin());
		p.add(btExec, MIG.get(MIG.SPAN, MIG.RIGHT));
		return p;
	}

	/**
	 * initialize the selection folder
	 *
	 * @return
	 */
	private JPanel initTfFolder() {
		//LOG.trace(TT + "initTfFolder()");
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
		JPanel p0 = new JPanel(new MigLayout(MIG.HIDEMODE3));
		p0.add(new JLabel(I18N.getColonMsg("export.format")));
		p0.add(cbFormat = new JComboBox(FORMAT));
		if (!FFmpeg.isInstalled()) {
			cbFormat.removeItemAt(3);
		}
		cbFormat.addItemListener((ItemEvent e) -> {
			pTempo.setVisible(cbFormat.getSelectedIndex() == 3);
		});
		return p0;
	}

	private JPanel initTempo() {
		pTempo = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0)));
		pTempo.add(new JLabel(I18N.getColonMsg("export.format.mpeg_tempo")));
		JButton btminus;
		pTempo.add(btminus = Ui.initButton("minus", ICONS.K.NONE, e -> addTempo(-1)));
		btminus.setText("▼");
		pTempo.add(tfTempo = new JTextField());
		tfTempo.setColumns(2);
		tfTempo.setHorizontalAlignment(JTextField.CENTER);
		JButton btplus;
		pTempo.add(btplus = Ui.initButton("plus", ICONS.K.NONE, e -> addTempo(1)));
		btplus.setText("▲");
		ckGeneric = new JCheckBox(I18N.getMsg("export.format.mpeg_generic"));
		pTempo.add(ckGeneric);
		addTempo(0);
		pTempo.setVisible(false);
		return pTempo;
	}

	/**
	 * for a mpeg export select the tempo
	 *
	 * @param value
	 */
	private void addTempo(int value) {
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
		JPanel p = new JPanel(new MigLayout());
		//p.setBorder(BorderFactory.createEtchedBorder());
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
		//LOG.trace(TT + "setInfos(txt=" + txt + ")");
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
	public void doCopyBegin() {
		//LOG.trace(TT + "doExec()");
		dirDest = new File(tfFolder.getText());
		if (cbFormat.getSelectedIndex() == FORMAT_EPUB) {
			dirDest = new File(dirDest, File.separator + "EPUB" + File.separator + "OEBPS");
		}
		if (cbFormat.getSelectedIndex() == FORMAT_MP4) {
			dirDest = new File(dirDest, File.separator + "images");
		}
		dirDest.mkdirs();
		if (cbFormat.getSelectedIndex() == FORMAT_MP4 && ckGeneric.isSelected()) {
			makeFFmpegBegin();
		}
		items = new ArrayList<>();
		for (int i = 0; i < table.getRowCount(); i++) {
			File src = (File) table.getValueAt(i, 1);
			String text = (String) table.getValueAt(i, 2);
			items.add(new AlbumItem(i + 1, text, src));
		}
		Dimension dim = null;
		boolean istext = (cbFormat.getSelectedIndex() == FORMAT_MP4), isremove = false;
		btExec.setEnabled(false);
		copyDlg = new CopyFileDlg(this, items, istext, dirDest, -1, isremove, null);
		copyDlg.setCompress(cbCompress.getSelectedIndex());
		if (cbFormat.getSelectedIndex() == FORMAT_MP4) {
			copyDlg.setSorter(4);
		}
		copyDlg.start();
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
	}

	/**
	 * end te copy process
	 */
	@Override
	public void doCopyEnd() {
		//LOG.trace(TT + "doExecSuite() format=" + cbFormat.getSelectedIndex());
		if (!copyDlg.isOK()) {
			return;
		}
		initInfos(getInfos() + copyDlg.getReport());
		Integer format = cbFormat.getSelectedIndex();
		switch (format) {
			case FORMAT_SIMPLE:// simply add an album XML file
				makeSimple();
				break;
			case FORMAT_HTML:// HTML format
				makeHTML();
				break;
			case FORMAT_EPUB:// EPUB format
				makeEPUB();
				break;
			case FORMAT_MP4:// MP4 format with FFmpeg
				try {
					String fx = FileUtil.removeExtension(mainFrame.getAlbumPanel().albumNameGet());
					File mp4 = new File(dirDest.getParentFile(), fx + ".mp4");
					makeFFmpeg(dirDest.getParentFile(), mp4.getAbsolutePath());
				} catch (IOException ex) {
					LOG.err(TT + "getName() makeFFmpeg error", ex);
				}
				break;
		}
	}

	/**
	 * make an EPUB file
	 */
	private void makeEPUB() {
		//LOG.trace(TT + "makeEPUB()");
		addInfos("<br>" + I18N.getMsg("export.format.epub_make"));
		dirDest = new File(tfFolder.getText());
		String fx = FileUtil.removeExtension(mainFrame.getAlbumPanel().albumNameGet());
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
	 * write an 800x600 image with text
	 *
	 */
	public void makeFFmpegBegin() {
		try {
			int width = 800, height = 600;
			String text = mainFrame.albumTitleGet();
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
	 * write an 800x600 image with text
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
		String fx = FileUtil.removeExtension(mainFrame.getAlbumPanel().albumNameGet());
		File outfile = new File(dirDest, fx + ".xml");
		StringBuilder b = new StringBuilder(XML.getHeader())
				.append("<album>\n")
				.append("   <list>\n");
		for (AlbumItem item : items) {
			b.append(itemToXml(item));
		}
		b.append("   </list>\n").append("</album>");
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
		return "      <item "
				+ "id=\"" + String.format("%03d", item.id + 1) + "\" "
				+ "file=\"" + file + "\" "
				+ "comment=\"" + item.text + "\" "
				+ " />\n";
	}

}
