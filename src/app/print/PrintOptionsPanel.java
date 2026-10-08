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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

/**
 *
 * @author favdb
 */
public class PrintOptionsPanel extends JPanel {

	private static final String TT = "PrintOptionsPanel.";

	/*private static final String GRID_FORMAT[] = {//only 3x5 allowed
		I18N.getMsg("print.full"),
		"2x2",// cell size may be 10x14.35
		"3x3",// cell may be 6.66x9.56
		"3x4",// cell size may be 6.66x7.17
		"3x5",// cell size may be 6x5
		"4x4",// cell size may be 5x7.17
		"5x5" // cell size may be 4x5.74
	};*/
	String[] PAPER = {"A4", "A3"};
	String ORIENTATION[] = {I18N.getMsg("print.orientation_portrait"),
		I18N.getMsg("print.orientation_landscape")};
	private final Print print;
	//private JComboBox cbGrid;
	private boolean validated = false;
	private JFormattedTextField tfTop, tfLeft, tfRight, tfBottom;
	private JRadioButton rbPortrait;
	private JRadioButton rbLandscape;
	private JRadioButton rbFormatA4;
	private JRadioButton rbFormatA3;

	public PrintOptionsPanel(Print print) {
		this.print = print;
		initialize();
	}

	@SuppressWarnings("unchecked")
	private void initialize() {
		setLayout(new MigLayout(MIG.WRAP + " 2"));
		boolean sel = print.xmlPrintGet().formatGet().equals("A4");
		JPanel format = new JPanel(new MigLayout());
		ButtonGroup Gfmt = new ButtonGroup();
		format.setBorder(
				BorderFactory.createTitledBorder(I18N.getColonMsg("print.orientation")));
		format.add(rbFormatA4 = myRb("A4", Gfmt, sel));
		format.add(rbFormatA3 = myRb("A3", Gfmt, !sel));
		add(format, MIG.get(MIG.GROWX, MIG.SPAN));

		JPanel orient = new JPanel(new MigLayout());
		ButtonGroup Gorient = new ButtonGroup();
		orient.setBorder(
				BorderFactory.createTitledBorder(I18N.getColonMsg("print.orientation")));
		sel = print.xmlPrintGet().isPortrait();
		orient.add(rbPortrait = myRb(I18N.getMsg("print.orientation_portrait"), Gorient, sel));
		orient.add(rbLandscape = myRb(I18N.getMsg("print.orientation_landscape"), Gorient, !sel));
		add(orient, MIG.get(MIG.GROWX, MIG.SPAN));

		// JComboBox to select the grid format, default is 3x5-5x3
		/*
		add(new JLabel(I18N.getColonMsg("print.grid")), MIG.RIGHT);
		cbGrid = Ui.initComboBox("print.grid", GRID_FORMAT, GRID_FORMAT[4]);
		cbGrid.addItemListener(s -> {
			save();
		});
		add(cbGrid);*/
		//JPanel to set margins (top, left, right, bottom)
		JPanel margins = new JPanel(new MigLayout(MIG.get(MIG.FILLX, MIG.WRAP), "[right][]"));
		margins.setBorder(BorderFactory
				.createTitledBorder(I18N.getColonMsg("print.margin") + "(mm)"));
		int x[] = print.xmlPrintGet().marginsIntGet();
		margins.add(new JLabel(I18N.getColonMsg("print.margin.top")));
		margins.add(tfTop = initField(margins, "top", 999, x[0]));

		margins.add(new JLabel(I18N.getColonMsg("print.margin.left")));
		margins.add(tfLeft = initField(margins, "left", 999, x[1]));

		margins.add(new JLabel(I18N.getColonMsg("print.margin.right")));
		margins.add(tfRight = initField(margins, "right", 999, x[2]));
		margins.add(new JLabel(I18N.getColonMsg("print.margin.bottom")));
		margins.add(tfBottom = initField(margins, "bottom", 999, x[3]));
		add(margins, MIG.get(MIG.SPAN, MIG.CENTER, MIG.GROWX));
	}

	private JFormattedTextField initField(JPanel margins, String name, int max, int value) {
		NumberFormat format = NumberFormat.getInstance();
		format.setGroupingUsed(false);
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setValueClass(Integer.class);
		formatter.setMaximum(max);
		formatter.setAllowsInvalid(false);
		formatter.setCommitsOnValidEdit(true);
		JFormattedTextField tf = new JFormattedTextField(formatter);
		tf.setColumns(2);
		tf.setHorizontalAlignment(JTextField.CENTER);
		tf.setToolTipText(I18N.getMsg("print.margin." + name));
		tf.setValue(value);
		tf.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				// empty
			}

			@Override
			public void focusLost(FocusEvent e) {
				save();//only save these options, reload previous disposition
				//print.refresh(); to erase all previous disposition and refresh drawing
			}
		});
		return tf;
	}

	private void save() {
		//LOG.trace(TT + "save()");
		print.xmlPrintGet().formatSet(rbFormatA4.isSelected() ? "A4" : "A3");
		int orient = rbPortrait.isSelected() ? 0 : 1;
		print.xmlPrintGet().orientationSet(orient);
		String str = (orient == 1 ? "5,3" : "3,5");
		/*cbGrid is ignored
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
		}*/
		print.xmlPrintGet().sizeSet(str);
		print.xmlPrintGet().marginsSet(getMargins());
		print.xmlGet().save();
		print.reload();
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

	public int[] getMarginsArray() {
		int top = 0, left = 0, bottom = 0, right = 0;
		try {
			top = Integer.parseInt(tfTop.getText().trim());
			left = Integer.parseInt(tfLeft.getText().trim());
			bottom = Integer.parseInt(tfBottom.getText().trim());
			right = Integer.parseInt(tfRight.getText().trim());
		} catch (Exception e) {
		}
		return new int[]{top, left, bottom, right};
	}

	private JRadioButton myRb(String str, ButtonGroup group, boolean selected) {
		JRadioButton rb = new JRadioButton(str);
		rb.setSelected(selected);
		rb.addChangeListener(e -> save());
		group.add(rb);
		return rb;
	}

	public String orientationGet() {
		return ORIENTATION[rbPortrait.isSelected() ? 0 : 1];
	}

	public String formatGet() {
		return (rbFormatA4.isSelected() ? "A4" : "A3");
	}

}
