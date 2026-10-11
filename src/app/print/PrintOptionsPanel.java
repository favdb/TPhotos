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
import app.tools.Ui;
import app.xml.XmlPrint;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

/**
 * JPanel class to manage print options
 *
 * @author favdb
 */
public class PrintOptionsPanel extends JPanel {

	private static final String TT = "PrintOptionsPanel.";

	public static final String[] PAPER = {"A4", "A3"};
	public static final String ORIENTATION[] = {I18N.getMsg("print.orientation_portrait"),
		I18N.getMsg("print.orientation_landscape")};
	private final Print print;
	private final XmlPrint xmlPrint;
	private boolean validated = false;

	private JFormattedTextField tfTop, tfLeft, tfRight, tfBottom, tfLines, tfCols;
	private JRadioButton rbPortrait;
	private JRadioButton rbLandscape;
	private JRadioButton rbFormatA4;
	private JRadioButton rbFormatA3;
	private JCheckBox ckCentral, ckPhoto;
	private String origin;

	public PrintOptionsPanel(Print print) {
		this.print = print;
		this.xmlPrint = print.xmlPrintGet();
		initialize();
	}

	/**
	 * initialize
	 */
	@SuppressWarnings("unchecked")
	private void initialize() {
		setLayout(new MigLayout(MIG.get(MIG.INS1, MIG.GAP1, MIG.WRAP + " 2")));
		origin = xmlPrint.commonGet();
		// JPanel for format (A4, A3)
		JPanel format = new JPanel(new MigLayout());
		boolean sel = xmlPrint.formatGet().equals("A4");
		ButtonGroup Gfmt = new ButtonGroup();
		format.setBorder(
				BorderFactory.createTitledBorder(I18N.getColonMsg("print.paper_format")));
		format.add(rbFormatA4 = myRb("A4", Gfmt, sel, false));
		format.add(rbFormatA3 = myRb("A3", Gfmt, !sel, false));
		add(format, MIG.get(MIG.GROWX, MIG.SPAN));
		//JPanel for orientation
		JPanel orient = new JPanel(new MigLayout());
		ButtonGroup Gorient = new ButtonGroup();
		orient.setBorder(
				BorderFactory.createTitledBorder(I18N.getColonMsg("print.orientation")));
		sel = xmlPrint.isPortrait();
		orient.add(rbPortrait = myRb(I18N.getMsg("print.orientation_portrait"), Gorient, sel, true));
		orient.add(rbLandscape = myRb(I18N.getMsg("print.orientation_landscape"), Gorient, !sel, true));
		add(orient, MIG.get(MIG.GROWX, MIG.SPAN));
		// Grid parameters (lines and cols
		int z[] = xmlPrint.gridGet();
		JPanel grid = new JPanel(new MigLayout(MIG.WRAP, "[right][]"));
		grid.setBorder(
				BorderFactory.createTitledBorder(I18N.getColonMsg("print.grid")));
		grid.add(new JLabel(I18N.getColonMsg("print.grid.lines")));
		grid.add(tfLines = initField("lines", 1, 10, z[1]));
		grid.add(new JLabel(I18N.getColonMsg("print.grid.cols")));
		grid.add(tfCols = initField("cols", 1, 10, z[0]));
		// don't show the grid parameters
		//add(grid, MIG.get(MIG.GROWX, MIG.SPAN));
		//JPanel to set margins (top, left, right, bottom)
		JPanel margins = new JPanel(new MigLayout(
				MIG.get(MIG.FILLX, MIG.HIDEMODE3, MIG.WRAP),
				"[right][]"));
		margins.setBorder(BorderFactory
				.createTitledBorder(I18N.getColonMsg("print.margin") + "(mm)"));
		int x[] = xmlPrint.marginsIntGet();
		margins.add(new JLabel(I18N.getColonMsg("print.margin.top")));
		margins.add(tfTop = initField("top", 0, 999, x[0]));

		margins.add(new JLabel(I18N.getColonMsg("print.margin.left")));
		margins.add(tfLeft = initField("left", 0, 999, x[1]));

		margins.add(new JLabel(I18N.getColonMsg("print.margin.right")));
		margins.add(tfRight = initField("right", 0, 999, x[2]));

		margins.add(new JLabel(I18N.getColonMsg("print.margin.bottom")));
		margins.add(tfBottom = initField("bottom", 0, 999, x[3]));
		// check boxes for central bing and photo margin
		margins.add(ckCentral = Ui.initCheckBox("print.margin.central",
				xmlPrint.centralGet(), e -> save()), MIG.get(MIG.SPAN, MIG.LEFT));
		ckCentral.addActionListener(e -> save());
		ckCentral.setVisible(rbLandscape.isSelected());
		margins.add(ckPhoto = Ui.initCheckBox("print.margin.photo",
				xmlPrint.gapGet(), e -> save()), MIG.get(MIG.SPAN, MIG.LEFT));
		ckPhoto.addActionListener(e -> save());
		add(margins, MIG.get(MIG.SPAN, MIG.CENTER, MIG.GROWX));
	}

	/**
	 * get a JFormattedField for number with min/max and initial value
	 *
	 * @param name name of this text field
	 * @param min minimal value
	 * @param max maximal value
	 * @param value initial value
	 * @return
	 */
	private JFormattedTextField initField(String name, int min, int max, int value) {
		NumberFormat format = NumberFormat.getInstance();
		format.setGroupingUsed(false);
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setValueClass(Integer.class);
		formatter.setMinimum(min);
		formatter.setMaximum(max);
		formatter.setAllowsInvalid(false);
		formatter.setCommitsOnValidEdit(true);
		JFormattedTextField tf = new JFormattedTextField(formatter);
		tf.setColumns(2);
		tf.setHorizontalAlignment(JTextField.CENTER);
		tf.setValue(value);
		tf.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				// empty
			}

			@Override
			public void focusLost(FocusEvent e) {
				save();
			}
		});
		return tf;
	}

	/**
	 * get a JRadioButton
	 *
	 * @param str name of this button
	 * @param group button groupe
	 * @param selected boolean for selected
	 * @param actOrient, boolean for orientation action
	 * @return
	 */
	private JRadioButton myRb(String str, ButtonGroup group, boolean selected, boolean actOrient) {
		JRadioButton rb = new JRadioButton(str);
		rb.setSelected(selected);
		rb.addActionListener((ActionEvent evt) -> {
			if (actOrient) {
				this.orientationChange();
			}
			save();
		});
		group.add(rb);
		return rb;
	}

	/**
	 * save all parameters to the XmlPrint
	 */
	private void save() {
		//LOG.trace(TT + "save()");
		xmlPrint.formatSet(rbFormatA4.isSelected() ? "A4" : "A3");
		int orient = rbPortrait.isSelected() ? 0 : 1;
		xmlPrint.orientationSet(orient);
		int cx[] = {0, 0};
		cx[0] = Integer.parseInt(tfLines.getText());
		cx[1] = Integer.parseInt(tfCols.getText());
		xmlPrint.gridSet(cx);
		try {
			int mx[] = {Integer.parseInt(tfTop.getText()),
				Integer.parseInt(tfLeft.getText()),
				Integer.parseInt(tfRight.getText()),
				Integer.parseInt(tfBottom.getText()),
				(ckCentral.isSelected() ? 1 : 0),
				(ckPhoto.isSelected() ? 1 : 0)
			};
			xmlPrint.marginsSet(mx);
		} catch (NumberFormatException ex) {
			xmlPrint.marginsSet(new int[]{0, 0, 0, 0, 0, 0});
		}
		if (!origin.equalsIgnoreCase(xmlPrint.commonGet())) {
			print.xmlGet().save();
			print.reload();
		}
	}

	/**
	 * get current orientation value
	 *
	 * @return
	 */
	public String orientationGet() {
		return ORIENTATION[rbPortrait.isSelected() ? 0 : 1];
	}

	/**
	 * get current format value
	 *
	 * @return
	 */
	public String formatGet() {
		return (rbFormatA4.isSelected() ? "A4" : "A3");
	}

	/**
	 * action for changing format
	 */
	private void formatChange() {
		if (rbFormatA4.isSelected()) {
			tfLines.setValue(6);
			tfCols.setValue(4);
		} else {
			tfLines.setValue(10);
			tfCols.setValue(6);
		}
	}

	/**
	 * exchange lines and cols when changing orientation
	 */
	private void orientationChange() {
		int l = (int) tfLines.getValue();
		tfLines.setValue((int) tfCols.getValue());
		tfCols.setValue(l);
		ckCentral.setVisible(rbLandscape.isSelected());
	}

}
