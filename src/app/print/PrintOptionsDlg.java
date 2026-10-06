/*
 * Copyright (C) 2026 favdb
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
package app.print;

import api.mig.MIG;
import api.mig.swing.MigLayout;
import app.i18n.I18N;
import app.resources.icons.ICONS;
import app.tools.Ui;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.NumberFormat;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

/**
 * JDialog to set printing format options
 *
 * @author favdb
 */
public class PrintOptionsDlg extends JDialog {

	private static final String TT = "PrintOptions.";

	private static final String GRID_FORMAT[] = {
		I18N.getMsg("print.full"),
		"2x2",// cell size may be 10x14.35
		"3x3",// cell may be 6.66x9.56
		"3x4",// cell size may be 6.66x7.17
		"3x5",// cell size may be 6x5
		"4x4",// cell size may be 5x7.17
		"5x5" // cell size may be 4x5.74
	};
	String[] PAPER = {"A4", "A3"};
	String ORIENTATION[] = {I18N.getMsg("print.orientation_portrait"),
		I18N.getMsg("print.orientation_landscape")};
	private final Print print;
	private JComboBox cbGrid, cbFormat;
	private JComboBox cbOrientation;
	private boolean validated = false;
	private JFormattedTextField tfTop, tfLeft, tfRight, tfBottom;
	private OverviewPanel overview;

	public static boolean show(Print print) {
		PrintOptionsDlg dlg = new PrintOptionsDlg(print);
		dlg.setVisible(true);
		return dlg.isOK();
	}

	public PrintOptionsDlg(Print print) {
		this.print = print;
		initialize();
	}

	/**
	 * initialize
	 */
	@SuppressWarnings("unchecked")
	private void initialize() {
		setLayout(new MigLayout(MIG.WRAP + " 2"));
		JPanel p = new JPanel(new MigLayout(MIG.WRAP + " 2"));

		p.add(new JLabel(I18N.getColonMsg("print.paper_format")), MIG.RIGHT);
		cbFormat = new JComboBox(PAPER);
		cbFormat.setSelectedItem(print.xmlPrintGet().formatGet());
		cbFormat.addItemListener(s -> {
			refresh();
		});
		p.add(cbFormat);

		p.add(new JLabel(I18N.getColonMsg("print.orientation")), MIG.RIGHT);
		cbOrientation = new JComboBox(ORIENTATION);
		cbOrientation.setSelectedIndex((print.xmlPrintGet().isPortrait() ? 0 : 1));
		cbOrientation.addItemListener(s -> {
			refresh();
		});
		p.add(cbOrientation);

		// JComboBox to select the grid format, default is 3x5-5x3
		p.add(new JLabel(I18N.getColonMsg("print.grid")));
		cbGrid = Ui.initComboBox("print.grid", GRID_FORMAT, GRID_FORMAT[4]);
		cbGrid.addItemListener(s -> {
			refresh();
		});
		p.add(cbGrid);

		//todo component to set margins (top, left, right, bottom)
		JPanel margins = new JPanel(new MigLayout(MIG.get(MIG.FILLX, MIG.WRAP + " 3")));
		margins.setBorder(BorderFactory
				.createTitledBorder(I18N.getColonMsg("print.margin") + "(mm)"));
		margins.add(tfTop = initField(margins, "top"),
				MIG.get(MIG.CENTER, MIG.SPAN));
		margins.add(tfLeft = initField(margins, "left"));
		margins.add(new JLabel("        "));
		margins.add(tfRight = initField(margins, "right"),
				MIG.RIGHT);
		margins.add(tfBottom = initField(margins, "bottom"),
				MIG.get(MIG.CENTER, MIG.SPAN));
		p.add(margins, MIG.get(MIG.SPAN, MIG.CENTER, MIG.GROWX));

		add(p, MIG.TOP);

		//todo a component to show an overview of margins and selected grid format
		JPanel r = new JPanel(new MigLayout());
		r.setBorder(BorderFactory
				.createTitledBorder(I18N.getColonMsg("print.overview")));
		r.add(overview = new OverviewPanel());
		overview.setOpaque(true);
		add(r);
		// panel for OK / Cancel buttons
		JPanel pok = new JPanel(new MigLayout());
		pok.add(Ui.initButton("ask.ok", ICONS.K.OK, e -> doOK()));
		pok.add(Ui.initButton("ask.cancel", ICONS.K.CANCEL, e -> dispose()));
		add(pok, MIG.get(MIG.SPAN, MIG.RIGHT));
		refresh();
	}

	private void doOK() {
		//todo validate this datas
		int fmt = cbFormat.getSelectedIndex();
		print.xmlPrintGet().formatSet(PAPER[fmt]);
		int orient = cbOrientation.getSelectedIndex();
		print.xmlPrintGet().orientationSet(orient);
		String str = "?";//(orient == 0 ? "5,3" : "3,5");
		switch (cbGrid.getSelectedIndex()) {
			case 0:
				str = "1,0";
				break;
			case 3:
				str = (orient == 1 ? "4,3" : "3,4");
				break;
			case 4:
				str = (orient == 1 ? "5,3" : "3,5");
				break;
			default:
				str = ((String) cbGrid.getSelectedItem()).replace("x", ",");
				break;
		}
		print.xmlPrintGet().sizeSet(str);
		print.xmlPrintGet().marginsSet(getMargins());
		print.xmlGet().save();
		validated = true;
		dispose();
	}

	private String getMargins() {
		try {
			return String.format("%d,%d,%d,%d",
					Integer.valueOf(tfTop.getText()),
					Integer.valueOf(tfLeft.getText()),
					Integer.valueOf(tfRight.getText()),
					Integer.valueOf(tfBottom.getText()));
		} catch (NumberFormatException ex) {
			return "0,0,0,0";
		}
	}

	private JFormattedTextField initField(JPanel margins, String name) {
		//margins.add(new JLabel(I18N.getColonMsg("print.margin." + name)));
		NumberFormat format = NumberFormat.getInstance();
		format.setGroupingUsed(false);
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setValueClass(Integer.class);
		formatter.setMaximum(999);
		formatter.setAllowsInvalid(false);
		formatter.setCommitsOnValidEdit(true);
		JFormattedTextField tf = new JFormattedTextField(formatter);
		tf.setColumns(2);
		tf.setHorizontalAlignment(JTextField.CENTER);
		tf.setToolTipText(I18N.getMsg("print.margin." + name));
		tf.addCaretListener(e -> refresh());
		return tf;
	}

	public boolean isOK() {
		return validated;
	}

	/**
	 * refresh (repaint the overview)
	 */
	private void refresh() {
		overview.setBackground(Color.WHITE);
		if (cbOrientation.getSelectedIndex() == 0) {
			overview.setPreferredSize(new Dimension(210, 297));
			overview.setMinimumSize(new Dimension(210, 297));
		} else {
			overview.setPreferredSize(new Dimension(297, 210));
			overview.setMinimumSize(new Dimension(297, 210));
		}
		overview.repaint();
		overview.invalidate();
		revalidate();
		pack();
		setLocationRelativeTo(print);
	}

	/**
	 * Paint Componant drawing the margins (red) and the virtual grid.
	 */
	private class OverviewPanel extends JPanel {

		public OverviewPanel() {
			setBackground(Color.WHITE);
			setOpaque(true);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			int w = getWidth(), h = getHeight();
			g2.setColor(Color.LIGHT_GRAY);
			g2.drawRect(0, 0, w - 1, h - 1);
			int top = parseMargin(tfTop),
					left = parseMargin(tfLeft),
					right = parseMargin(tfRight),
					bottom = parseMargin(tfBottom);
			int contentX = left, contentY = top;
			int contentW = w - left - right, contentH = h - top - bottom;
			if (contentW > 0 && contentH > 0) {
				g2.setColor(Color.RED);
				float[] dashPattern = {4f, 4f};
				g2.setStroke(new BasicStroke(1f,
						BasicStroke.CAP_BUTT,
						BasicStroke.JOIN_MITER,
						10f,
						dashPattern,
						0f));
				g2.drawLine(0, contentY, w, contentY);
				g2.drawLine(0, contentY + contentH, w, contentY + contentH);
				g2.drawLine(contentX, 0, contentX, h);
				g2.drawLine(contentX + contentW, 0, contentX + contentW, h);
			}
			String selectedGrid = (String) cbGrid.getSelectedItem();
			if (selectedGrid != null
					&& !selectedGrid.equalsIgnoreCase(I18N.getMsg("print.full"))
					&& contentW > 0
					&& contentH > 0) {
				String[] parts = selectedGrid.split("x");
				if (parts.length == 2) {
					try {
						int cols = Integer.parseInt(parts[0].trim()),
								rows = Integer.parseInt(parts[1].trim());
						if (cbOrientation.getSelectedIndex() == 1) {
							int tmp = cols;
							cols = rows;
							rows = tmp;
						}
						int gridX = contentX;
						int gridY = contentY;
						int gridW = contentW;
						int gridH = contentH;
						if (cols != rows) {
							int cellSize = Math.min(contentW / cols, contentH / rows);
							gridW = cols * cellSize;
							gridH = rows * cellSize;
							gridX = contentX + (contentW - gridW) / 2;
							gridY = contentY + (contentH - gridH) / 2;
						}
						g2.setColor(new Color(180, 180, 180));
						g2.setStroke(new BasicStroke(1f));
						g2.drawRect(gridX, gridY, gridW, gridH);
						for (int i = 1; i < cols; i++) {
							int x = gridX + (i * gridW / cols);
							g2.drawLine(x, gridY, x, gridY + gridH);
						}
						for (int j = 1; j < rows; j++) {
							int y = gridY + (j * gridH / rows);
							g2.drawLine(gridX, y, gridX + gridW, y);
						}
					} catch (NumberFormatException e) {
						// Ignored
					}
				}
			}
			g2.dispose();
		}

		private int parseMargin(JFormattedTextField tf) {
			if (tf == null || tf.getText() == null || tf.getText().trim().isEmpty()) {
				return 0;
			}
			try {
				return Integer.parseInt(tf.getText().trim());
			} catch (NumberFormatException e) {
				return 0;
			}
		}
	}

}
