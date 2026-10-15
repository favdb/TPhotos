package app.print;

import app.tools.LOG;
import app.xml.XmlPrint;
import app.xml.XmlPrintCell;
import app.xml.XmlPrintPage;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterAbortException;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Class to print the album pages the way Grid shows them without the visual aids of the
 * editor (no red margin frames and no empty cells).
 *
 * The layout rules are the same as Grid.refresh(): same cols/rows, same central binding
 * handling (landscape + binding), same spans. Margins (XmlPrint.marginsIntGet()) are
 * considered to be in millimetres: change MARGIN_UNIT if they are not.
 *
 * Usage: Printer.print(print);
 */
public class Printer implements Printable {

	private static final String TT = "Printer.";

	private static final double MARGIN_UNIT = 72.0 / 25.4;
	private static final double PHOTO_GAP_MM = 1.0;
	private static final double PRINT_DPI = 300.0;
	private static final double NATIVE_DPI = 300.0;

	private final Print print;
	private final List<Integer> pageIds = new ArrayList<>();

	private PrinterJob job;
	private JDialog progressDialog;
	private JProgressBar progressBar;
	private JLabel progressLabel;
	private JButton cancelButton;
	private volatile boolean cancelled = false;
	private final XmlPrint xmlPrint;
	private final Grid grid;

	private Printer(Print print) {
		this.print = print;
		this.xmlPrint = print.xmlPrintGet();
		this.grid = print.gridGet();

	}

	/**
	 * open the print dialog then print all the pages of the album
	 *
	 * @param print
	 */
	public static void print(Print print) {
		//LOG.trace(TT+"print(print)");
		new Printer(print).run();
	}

	private void run() {
		//LOG.trace(TT+"run()");
		pageIds.clear();
		for (XmlPrintPage p : print.printPagesGet()) {
			try {
				pageIds.add(Integer.valueOf(p.idGet()));
			} catch (NumberFormatException ex) {
				LOG.err(TT + "invalid page id " + p.idGet());
			}
		}
		if (pageIds.isEmpty()) {
			return;
		}
		job = PrinterJob.getPrinterJob();
		job.setJobName("Album");
		boolean a3 = "A3".equalsIgnoreCase(xmlPrint.formatGet());
		double w = (a3 ? 297 : 210) * 72.0 / 25.4,
				h = (a3 ? 420 : 297) * 72.0 / 25.4;
		Paper paper = new Paper();
		paper.setSize(w, h);
		paper.setImageableArea(0, 0, w, h);
		PageFormat pf = job.defaultPage();
		pf.setPaper(paper);
		pf.setOrientation(xmlPrint.isPortrait() ? PageFormat.PORTRAIT : PageFormat.LANDSCAPE);
		job.setPrintable(this, pf);
		PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
		attrs.add(a3 ? MediaSizeName.ISO_A3 : MediaSizeName.ISO_A4);
		attrs.add(xmlPrint.isPortrait() ? OrientationRequested.PORTRAIT : OrientationRequested.LANDSCAPE);
		int[] mg = xmlPrint.marginsIntGet();
		float mTop = mg[0],
				mLeft = mg[1],
				mRight = mg[2],
				mBottom = mg[3],
				sheetW = a3 ? 297f : 210f,
				sheetH = a3 ? 420f : 297f;
		float ax, ay, aw, ah;
		if (xmlPrint.isPortrait()) {
			ax = mLeft;
			ay = mTop;
			aw = sheetW - mLeft - mRight;
			ah = sheetH - mTop - mBottom;
		} else {
			ax = mBottom;
			ay = mLeft;
			aw = sheetW - mBottom - mTop;
			ah = sheetH - mLeft - mRight;
		}
		if (ax >= 0 && ay >= 0 && aw > 0 && ah > 0) {
			attrs.add(new MediaPrintableArea(ax, ay, aw, ah, MediaPrintableArea.MM));
		}

		try {
			// Swing dialog: printer selection + "print to file" destination, on every OS
			PrintService[] services = PrinterJob.lookupPrintServices();
			if (services.length > 0) {
				PrintService selected = ServiceUI.printDialog(null, 200, 200, services,
						PrintServiceLookup.lookupDefaultPrintService(),
						DocFlavor.SERVICE_FORMATTED.PRINTABLE, attrs);
				if (selected == null) {
					return; // cancelled
				}
				job.setPrintService(selected);
			} else if (!job.printDialog(attrs)) { // no service found: native dialog
				return;
			}
		} catch (PrinterException ex) {
			LOG.err(TT + "print error: " + ex.getMessage());
			return;
		}
		attrs.add(a3 ? MediaSizeName.ISO_A3 : MediaSizeName.ISO_A4);
		attrs.add(xmlPrint.isPortrait() ? OrientationRequested.PORTRAIT : OrientationRequested.LANDSCAPE);
		try {
			job.setPrintable(this, job.getPageFormat(attrs));
		} catch (RuntimeException ex) {
			LOG.err(TT + "getPageFormat failed, A3/A4 page format kept: " + ex);
		}
		cancelled = false;
		Window owner = SwingUtilities.getWindowAncestor(grid);
		buildProgressDialog(owner);
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				job.print(attrs);
				return null;
			}

			@Override
			@SuppressWarnings("null")
			protected void done() {
				progressDialog.dispose();
				try {
					get();
				} catch (ExecutionException ex) {
					Throwable cause = ex.getCause();
					if (!(cause instanceof PrinterAbortException)) {
						LOG.err(TT + "print error: " + cause);
						JOptionPane.showMessageDialog(owner,
								"Print error : " + cause.getMessage(),
								"Print", JOptionPane.ERROR_MESSAGE);
					}
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}
		};
		worker.execute();
		progressDialog.setVisible(true);
	}

	/**
	 * build the progress window: message, progress bar and cancel button
	 */
	private void buildProgressDialog(Window owner) {
		progressDialog = new JDialog(owner, "Impression",
				Dialog.ModalityType.APPLICATION_MODAL);
		progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		progressLabel = new JLabel("Préparation de l'impression…");
		progressBar = new JProgressBar(0, pageIds.size() * 100);
		progressBar.setIndeterminate(true);
		cancelButton = new JButton("Annuler");
		cancelButton.addActionListener(e -> {
			cancelled = true;
			cancelButton.setEnabled(false);
			progressLabel.setText("Annulation en cours…");
			job.cancel();
		});
		JPanel panel = new JPanel(new BorderLayout(0, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
		panel.add(progressLabel, BorderLayout.NORTH);
		panel.add(progressBar, BorderLayout.CENTER);
		JPanel south = new JPanel();
		south.add(cancelButton);
		panel.add(south, BorderLayout.SOUTH);
		progressDialog.setContentPane(panel);
		progressDialog.setPreferredSize(new Dimension(380, 150));
		progressDialog.pack();
		progressDialog.setLocationRelativeTo(owner);
	}

	/**
	 * update the progress window (may be called from any thread). The value only grows,
	 * because Java may ask the same page twice.
	 */
	private void progress(int pageIndex, int done, int total) {
		final int value = pageIndex * 100 + (total == 0 ? 100 : done * 100 / total);
		final String text = String.format("Page %d/%d – élément %d/%d",
				pageIndex + 1, pageIds.size(), done, total);
		SwingUtilities.invokeLater(() -> {
			progressBar.setIndeterminate(false);
			if (value >= progressBar.getValue()) {
				progressBar.setValue(value);
				if (!cancelled) {
					progressLabel.setText(text);
				}
			}
		});
	}

	/**
	 * print for given Graphics, PageFormat and page index
	 *
	 * @param g
	 * @param pf
	 * @param pageIndex
	 * @return
	 * @throws PrinterException
	 */
	@Override
	public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
		if (cancelled) {
			throw new PrinterAbortException();
		}
		if (pageIndex < 0 || pageIndex >= pageIds.size()) {
			return NO_SUCH_PAGE;
		}
		/*LOG.trace(String.format(TT + "print() imageable "
				+ "x=%.1f y=%.1f w=%.1f h=%.1f pt (x=%.1f y=%.1f mm)",
				pf.getImageableX(), pf.getImageableY(),
				pf.getImageableWidth(), pf.getImageableHeight(),
				pf.getImageableX() / MARGIN_UNIT, pf.getImageableY() / MARGIN_UNIT));*/
		Graphics2D g2 = (Graphics2D) g.create();
		try {
			g2.setClip(-10000, -10000, 20000, 20000);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			drawPage(g2, pf.getWidth(), pf.getHeight(),
					pageIds.get(pageIndex), pageIndex);
		} finally {
			g2.dispose();
		}
		return PAGE_EXISTS;
	}

	/**
	 * draw one page, same layout rules as Grid.refresh()
	 */
	private void drawPage(Graphics2D g2, double pageW, double pageH,
			int pageId, int pageIndex) throws PrinterException {
		int pageNum = pageIndex + 1;
		int cols = grid.colsGet(), rows = grid.rowsGet();
		if (cols < 1 || rows < 1) {
			return;
		}
		int[] m = xmlPrint.marginsIntGet();
		double top = m[0] * MARGIN_UNIT, left = m[1] * MARGIN_UNIT,
				right = m[2] * MARGIN_UNIT, bottom = m[3] * MARGIN_UNIT,
				gap = xmlPrint.gapGet() ? PHOTO_GAP_MM * 72.0 / 25.4 : 0;
		boolean binding = !xmlPrint.isPortrait() && xmlPrint.centralGet() && cols >= 2;
		int halfCols = cols / 2;
		double[] x0 = new double[cols], x1 = new double[cols];
		if (binding) {
			double centralGap = left + right,
					halfW = (pageW - left - right - centralGap) / 2,
					secondX = left + halfW + centralGap;
			int rightCols = cols - halfCols;
			for (int c = 0; c < cols; c++) {
				if (c < halfCols) {
					x0[c] = left + c * halfW / halfCols;
					x1[c] = left + (c + 1) * halfW / halfCols;
				} else {
					x0[c] = secondX + (c - halfCols) * halfW / rightCols;
					x1[c] = secondX + (c - halfCols + 1) * halfW / rightCols;
				}
			}
		} else {
			double cw = (pageW - left - right) / cols;
			for (int c = 0; c < cols; c++) {
				x0[c] = left + c * cw;
				x1[c] = left + (c + 1) * cw;
			}
		}
		double rh = (pageH - top - bottom) / rows;
		int total = 0, done = 0;
		for (XmlPrintCell cell : print.getCells()) {
			if (cell.pageGet() == pageId && !cell.isEmpty()) {
				total++;
			}
		}
		progress(pageIndex, 0, total);
		for (XmlPrintCell cell : print.getCells()) {
			if (cancelled) {
				throw new PrinterAbortException();
			}
			if (cell.pageGet() != pageId || cell.isEmpty()) {
				continue;
			}
			int num = cell.cellNumGet();
			if (num < 1 || num > rows * cols) {
				continue;
			}
			int r = (num - 1) / cols, c = (num - 1) % cols,
					sH = Math.max(1, cell.spanHorizontalGet()),
					sV = Math.max(1, cell.spanVerticalGet());
			sH = Math.min(sH, cols - c);
			sV = Math.min(sV, rows - r);
			if (binding && c < halfCols && c + sH > halfCols) {
				sH = halfCols - c;
			}
			double rx = x0[c] + gap / 2,
					ry = top + r * rh + gap / 2,
					rw = x1[c + sH - 1] - x0[c] - gap,
					rhh = sV * rh - gap;
			if (rw <= 0 || rhh <= 0) {
				continue;
			}
			Rectangle2D area = new Rectangle2D.Double(rx, ry, rw, rhh);
			if (cell.isPhoto()) {
				drawPhoto(g2, cell, area);
			} else if (cell.isText()) {
				drawText(g2, cell, area);
			}
			progress(pageIndex, ++done, total);
		}
		if (xmlPrint.numpageGet()) {
			Graphics2D gn = (Graphics2D) g2.create();
			gn.setColor(Color.BLACK);
			gn.setFont(gn.getFont().deriveFont(Font.PLAIN, 9f));
			FontMetrics fm = gn.getFontMetrics();
			String s = Integer.toString(pageNum);
			float y = (float) (pageH - Math.max(bottom / 2,
					fm.getHeight()) + fm.getAscent() / 2.0);
			gn.drawString(s, (float) ((pageW - fm.stringWidth(s)) / 2), y);
			gn.dispose();
		}
	}

	/**
	 * zoom 0 = original size (at NATIVE_DPI), 1 = contain, 2 = cover.
	 *
	 * Always clipped to the cell.
	 */
	private void drawPhoto(Graphics2D g2, XmlPrintCell cell, Rectangle2D area) {
		File f = cell.photoFileGet();
		if (f == null || !f.exists()) {
			return;
		}
		BufferedImage img;
		try {
			img = ImageIO.read(f);
		} catch (IOException ex) {
			LOG.err(TT + "cannot read " + f + " : " + ex.getMessage());
			return;
		}
		if (img == null) {
			return;
		}
		double iw = img.getWidth(), ih = img.getHeight(), s;
		switch (cell.zoomGet()) {
			case 2:
				s = Math.max(area.getWidth() / iw, area.getHeight() / ih);
				break;
			case 0:
				s = 72.0 / NATIVE_DPI;
				break;
			default:
				s = Math.min(area.getWidth() / iw, area.getHeight() / ih);
				break;
		}
		double dx = area.getX() + (area.getWidth() - iw * s) / 2,
				dy = area.getY() + (area.getHeight() - ih * s) / 2;
		Rectangle2D drawn = new Rectangle2D.Double(dx, dy, iw * s, ih * s),
				vis = drawn.createIntersection(area);
		if (vis.isEmpty()) {
			return;
		}
		int sx = (int) Math.max(0, Math.floor((vis.getX() - dx) / s)),
				sy = (int) Math.max(0, Math.floor((vis.getY() - dy) / s)),
				sw = (int) Math.min(img.getWidth() - sx,
						Math.ceil(vis.getWidth() / s) + 1),
				sh = (int) Math.min(img.getHeight() - sy,
						Math.ceil(vis.getHeight() / s) + 1);
		if (sw < 1 || sh < 1) {
			return;
		}
		BufferedImage part = img.getSubimage(sx, sy, sw, sh);
		int tw = (int) Math.ceil(sw * s / 72.0 * PRINT_DPI),
				th = (int) Math.ceil(sh * s / 72.0 * PRINT_DPI);
		BufferedImage out = downscale(part, Math.max(1, tw), Math.max(1, th));
		Graphics2D gc = (Graphics2D) g2.create();
		gc.clip(area);
		AffineTransform at = new AffineTransform();
		at.translate(dx + sx * s, dy + sy * s);
		at.scale(sw * s / out.getWidth(), sh * s / out.getHeight());
		gc.drawImage(out, at, null);
		gc.dispose();
	}

	/**
	 * reduce the given image to the target size by successive halvings (better quality
	 * than one big step). An image that is already small enough is returned unchanged.
	 */
	private static BufferedImage downscale(BufferedImage src, int tw, int th) {
		if (src.getWidth() <= tw || src.getHeight() <= th) {
			return src;
		}
		BufferedImage cur = src;
		int w = cur.getWidth(), h = cur.getHeight();
		while (w > tw || h > th) {
			int nw = Math.min(w, Math.max(tw, w / 2)),
					nh = Math.min(h, Math.max(th, h / 2));
			BufferedImage next = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = next.createGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, nw, nh);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(cur, 0, 0, nw, nh, null);
			g.dispose();
			cur = next;
			w = nw;
			h = nh;
		}
		return cur;
	}

	/**
	 * the text is rendered by a JLabel, like in GridCell, but without background
	 */
	private void drawText(Graphics2D g2, XmlPrintCell cell, Rectangle2D area) {
		String content = cell.textGet() != null ? cell.textGet() : "";
		if (content.isEmpty()) {
			return;
		}
		JLabel label = new JLabel("<html>"
				+ "<head>"
				+ "<style>body { font-size: 10px; }</style>"
				+ "</head>"
				+ "<body>"
				+ content
				+ "</body>"
				+ "</html>");
		label.setOpaque(false);
		label.setForeground(Color.BLACK);
		label.setVerticalAlignment(SwingConstants.TOP);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		label.setBounds(0, 0, (int) area.getWidth(), (int) area.getHeight());
		Graphics2D g2d = (Graphics2D) g2.create();
		g2d.clip(area);
		g2d.translate(area.getX(), area.getY());
		label.print(g2d);
		g2d.dispose();
	}

}
