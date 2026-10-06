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

import app.AbstractFrame;
import app.App;
import app.MainFrame;
import app.album.AlbumTable;
import app.i18n.I18N;
import app.resources.icons.ICONS;
import app.resources.icons.IconUtil;
import app.tools.FFmpeg;
import app.tools.GBC;
import app.tools.LOG;
import app.tools.Ui;
import app.tools.file.EnvUtil;
import app.tools.file.FileUtil;
import app.xml.Xml;
import app.xml.XmlAlbumItem;
import app.xml.XmlUtil;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
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

/**
 * JDialog to copy multiple images to a destination folder
 *
 * @author favdb
 */
public class Export extends AbstractFrame {

	private static final String TT = "Export.";

	private final AlbumTable table;
	private JTextField tfFolder;
	public JButton btExec;
	private JComboBox cbFormat, cbCompress;
	public File dirDest;
	private final MainFrame mainFrame;
	private Container pane;
	public ExportCopyDlg copyDlg;
	private List<XmlAlbumItem> items;
	public static final String FORMAT_SIMPLE = I18N.getMsg("export.format.simple"),
			FORMAT_HTML = I18N.getMsg("export.format.html"),
			FORMAT_EPUB = I18N.getMsg("export.format.epub"),
			FORMAT_MPEG = I18N.getMsg("export.format.mpeg");
	private static final String FORMAT[] = {
		FORMAT_SIMPLE, FORMAT_HTML, FORMAT_EPUB, FORMAT_MPEG
	};
	public static final int COMPRESS_NONE = 0, COMPRESS_MINI = 1, COMPRESS_MAXI = 0;
	private static final String COMPRESS[] = {
		I18N.getMsg("export.compress.none"),
		I18N.getMsg("export.compress.mini"),
		I18N.getMsg("export.compress.maxi")
	};
	public int tempo = 5;
	private JTextField tfTempo;
	public JCheckBox ckGeneric;
	private JPanel pTempo, pFolder, pCompress;

	/**
	 * call the function
	 *
	 * @param mainFrame
	 */
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Export(MainFrame mainFrame) {
		super();
		this.mainFrame = mainFrame;
		this.table = mainFrame.albumGet().tableGet();
		initialize();
	}

	public MainFrame getMainFrame() {
		return mainFrame;
	}

	/**
	 * initialize the frame
	 */
	@Override
	public void initialize() {
		//LOG.trace(TT + "initialize()");
		this.setLayout(new GridBagLayout());
		Dimension sz = Toolkit.getDefaultToolkit().getScreenSize();
		this.setMaximumSize(sz);
		pane = this.getContentPane();
		pane.setLayout(new GridBagLayout());
		pane.add(topInit(), new GBC("0, 0, growx, wx 1.0, ins 2"));
		
		taInfosInit("init");
		taInfos.setText(FileUtil.readHtml("Export"));
		JScrollPane scroll = new JScrollPane(taInfos);
		scroll.setPreferredSize(new Dimension(1024, 768));
		pane.add(scroll, new GBC("1, 0, grow, wx 1.0, wy 1.0, ins 2"));

		cbFormat.addItemListener((ItemEvent e) -> {
			pTempo.setVisible(true);
			ckGeneric.setVisible(false);
			pFolder.setVisible(true);
			pCompress.setVisible(true);
			String n = (String) cbFormat.getSelectedItem();
			pTempo.setVisible(n.equals(FORMAT_MPEG));
			ckGeneric.setVisible(n.equals(FORMAT_MPEG));
			btExec.setEnabled(!tfFolder.getText().isEmpty());
		});
	}

	/**
	 * initialize the upper JPanel
	 *
	 * @return
	 */
	private JPanel topInit() {
		//LOG.trace(TT + "initTop()");
		JPanel p = new JPanel(new GridBagLayout());
		
		p.add(formatInit(), new GBC("0, 0, left, ins 2"));
		pCompress = compressInit();
		p.add(pCompress, new GBC("0, 1, left, ins 2"));
		
		pTempo = tempoInit();
		p.add(pTempo, new GBC("1, 0, gw 2, left, ins 2"));
		
		pFolder = folderInit();
		p.add(pFolder, new GBC("2, 0, gw 2, growx, wx 1.0, ins 2"));

		JPanel p2 = new JPanel(new GridBagLayout());
		ckGeneric = new JCheckBox(I18N.getMsg("export.format.mpeg_generic"));
		p2.add(ckGeneric, new GBC("0, 0, right, ins 2"));

		btExec = new JButton(I18N.getMsg("export.start"));
		btExec.setIcon(IconUtil.getIconSmall(ICONS.K.COGS));
		btExec.setEnabled(!tfFolder.getText().isEmpty());
		btExec.addActionListener(e -> copyBegin());
		p2.add(btExec, new GBC("0, 1, right, ins 2"));

		p.add(p2, new GBC("3, 0, gw 2, right, ins 2"));
		return p;
	}

	/**
	 * initialize the selection folder
	 *
	 * @return
	 */
	private JPanel folderInit() {
		//LOG.trace(TT + "initFolder()");
		JPanel p2 = new JPanel(new GridBagLayout());
		p2.add(new JLabel(I18N.getColonMsg("export.dest")), new GBC("0, 0, left, ins 2"));

		tfFolder = new JTextField();
		tfFolder.setColumns(32);
		tfFolder.setEditable(false);
		tfFolder.setText(App.pref.exportLastGet());
		p2.add(tfFolder, new GBC("0, 1, growx, wx 1.0, ins 2"));

		JButton bt = Ui.initIconButton("directory.select", ICONS.K.FOLDER,
				(ActionEvent evt) -> {
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
		p2.add(bt, new GBC("0, 2, ins 2"));
		return p2;
	}

	/**
	 * initialize the export format
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private JPanel formatInit() {
		//LOG.trace(TT + "initFormat()");
		JPanel p0 = new JPanel(new GridBagLayout());
		p0.add(new JLabel(I18N.getColonMsg("export.format")), new GBC("0, 0, left, ins 2"));
		cbFormat = new JComboBox(FORMAT);
		p0.add(cbFormat, new GBC("0, 1, left, ins 2"));
		if (!FFmpeg.isInstalled()) {
			cbFormat.removeItemAt(3);
		}
		return p0;
	}

	private JPanel tempoInit() {
		//LOG.trace(TT + "initTempo()");
		JPanel p = new JPanel(new GridBagLayout());
		p.add(new JLabel(I18N.getColonMsg("export.format.mpeg_tempo")), new GBC("0, 0, left, ins 2"));

		JButton btminus = Ui.initButton("minus", ICONS.K.NONE, e -> tempoAdd(-1));
		btminus.setText("▼");
		p.add(btminus, new GBC("0, 1, ins 1"));

		tfTempo = new JTextField();
		tfTempo.setColumns(2);
		tfTempo.setHorizontalAlignment(JTextField.CENTER);
		p.add(tfTempo, new GBC("0, 2, ins 1"));

		JButton btplus = Ui.initButton("plus", ICONS.K.NONE, e -> tempoAdd(1));
		btplus.setText("▲");
		p.add(btplus, new GBC("0, 3, ins 1"));

		tempoAdd(0);
		p.setVisible(false);
		return p;
	}

	/**
	 * for a mpeg export select the tempo
	 *
	 * @param value
	 */
	private void tempoAdd(int value) {
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
	private JPanel compressInit() {
		//LOG.trace(TT + "initCompress()");
		JPanel p = new JPanel(new GridBagLayout());
		p.add(new JLabel(I18N.getColonMsg("export.compress")), new GBC("0, 0, left, ins 2"));
		cbCompress = new JComboBox(COMPRESS);
		p.add(cbCompress, new GBC("0, 1, left, ins 2"));
		return p;
	}

	/**
	 * get the list of AlbumItem
	 *
	 * @return
	 */
	public List<XmlAlbumItem> itemsGet() {
		return items;
	}

	/**
	 * begin the copy process
	 */
	@Override
	public void copyBegin() {
		//LOG.trace(TT + "copyBegin()");
		dirDest = new File(tfFolder.getText());
		App.pref.exportLastSet(tfFolder.getText());
		String format = (String) cbFormat.getSelectedItem();
		if (format.equals(FORMAT_EPUB)) {
			dirDest = new File(dirDest, File.separator + "EPUB" + File.separator + "OEBPS");
		} else if (format.equals(FORMAT_MPEG)) {
			dirDest = new File(dirDest, File.separator + "images");
		}
		dirDest.mkdirs();
		if (format.equals(FORMAT_MPEG) && ckGeneric.isSelected()) {
			FFmpeg.begin(this);
		}
		items = new ArrayList<>();
		for (XmlAlbumItem src : mainFrame.albumGet().xmlGet().albumGet().itemsGet()) {
			items.add(src);
		}
		Dimension dim = null;
		boolean withText = (cbFormat.getSelectedItem().equals(FORMAT_MPEG)), isremove = false;
		btExec.setEnabled(false);
		setWaitingCursor();
		copyDlg = new ExportCopyDlg(this,
				items,
				withText,
				dirDest,
				cbFormat.getSelectedIndex() + 1,
				isremove,
				null);
		copyDlg.setCompress(cbCompress.getSelectedIndex());
		copyDlg.start();
	}

	/**
	 * end te copy process
	 */
	@Override
	public void copyEnd() {
		//LOG.trace(TT + "doExecSuite()");
		if (!copyDlg.isOK()) {
			return;
		}
		taInfosInit(taInfosContentGet() + copyDlg.getReport());
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
				setWaitingCursor();
				FFmpeg ffmpeg = new FFmpeg();
				ffmpeg.start(this, dirDest.getParentFile(), mp4.getAbsolutePath());
			} catch (IOException ex) {
				LOG.err(TT + "getName() makeFFmpeg error", ex);
			}
			setNormalCursor();
		}
	}

	/**
	 * make an EPUB file
	 */
	private void makeEPUB() {
		//LOG.trace(TT + "makeEPUB()");
		taInfosAdd("<br>" + I18N.getMsg("export.format.epub_make"));
		dirDest = new File(tfFolder.getText());
		String fx = FileUtil.removeExtension(mainFrame.albumGet().diapoNameGet());
		ExportEPUB.create(this, dirDest);
		taInfosAdd(" " + I18N.getMsg("task.ok") + "</p>");
		setNormalCursor();
		btExec.setEnabled(true);
	}

	/**
	 * make a MP4 file
	 *
	 * @param dir
	 * @param outfile
	 * @throws IOException
	 */
	public void makeFFmpeg(File dir, String outfile) {
		//LOG.trace(TT + "makeFFmpeg(dir=" + dir + ", outfile=" + outfile + ")");
		try {
			setWaitingCursor();
			FFmpeg ffmpeg = new FFmpeg();
			ffmpeg.start(this, dir, outfile);
		} catch (IOException e) {

		}
		setNormalCursor();
	}

	/**
	 * create first image (album title)
	 *
	 */
	public void makeFFmpegBegin() {
		try {
			int width = 800, height = 600;
			String text = mainFrame.diapoTitleGet();
			BufferedImage bufferedImage = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = bufferedImage.createGraphics();
			g2d.setColor(Color.BLACK);
			g2d.fillRect(0, 0, width, height);
			g2d.setColor(Color.WHITE);
			g2d.setFont(new Font("Arial", Font.BOLD, 24));
			FontMetrics fontMetrics = g2d.getFontMetrics();
			int textWidth = fontMetrics.stringWidth(text),
					textHeight = fontMetrics.getAscent();
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
		taInfosAdd("<br>" + I18N.getMsg("export.format.html_make"));
		ExportHTML html = new ExportHTML(this, dirDest);
		html.begin(items);
		taInfosAdd(I18N.getMsg("task.ok") + "</p>");
		setNormalCursor();
		btExec.setEnabled(true);
	}

	/**
	 * make an album with a XML file
	 */
	private void makeSimple() {
		//LOG.trace(TT + "makeSimple()");
		taInfosAdd("<br>" + I18N.getMsg("export.format.simple_make"));
		String fx = FileUtil.removeExtension(mainFrame.albumGet().diapoNameGet());
		File outfile = new File(dirDest, fx + ".xml");
		StringBuilder b = new StringBuilder(Xml.getHeader())
				.append("<album>\n")
				.append(XmlUtil.indent(1)).append("<list>\n");
		for (XmlAlbumItem item : items) {
			b.append(XmlUtil.indent(2)).append("<item ");
			b.append("id=\"").append(item.idGet()).append("\" ")
					.append("file=\"").append(item.fileGet().getName()).append("\" ")
					.append("comment=\"").append(item.commentGet()).append("\" ")
					.append("/>\n");
		}
		b.append(XmlUtil.INDENT).append("<list>\n");
		b.append("</album>");
		FileUtil.fileWriteString(outfile, b.toString());
		taInfosAdd(" " + I18N.getMsg("task.ok") + "</p>");
		setNormalCursor();
		btExec.setEnabled(true);
	}

}