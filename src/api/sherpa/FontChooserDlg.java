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
package api.sherpa;

import api.mig.MIG;
import api.mig.swing.MigLayout;
import app.App;
import app.i18n.I18N;
import app.resources.icons.ICONS;
import app.tools.Ui;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;

/**
 *
 * @author favdb
 */
public class FontChooserDlg extends JDialog {

	String[] styleList = new String[]{
		I18N.getMsg("she.font.plain"),
		I18N.getMsg("she.font.bold"),
		I18N.getMsg("she.font.italic")
	};
	Integer[] sizeList = new Integer[]{3, 4, 5, 6, 7, 8, 9, 10,
		11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 22,
		24, 27, 30, 34, 39, 45, 51, 60};
	private final JLabel sample = new JLabel();
	private JList lsFamily, lsStyle;
	private JList<Integer> lsSize;
	//private JLabel lbFamily, lbStyle, lbSize;
	private boolean cancel = true;
	private JPanel pup;

	public FontChooserDlg(java.awt.Window parent, Font initfont) {
		super(parent, ModalityType.APPLICATION_MODAL);
		initialize(initfont);
	}

	private void initialize(Font inFont) {
		setTitle(I18N.getMsg("she.font"));
		setLayout(new MigLayout(MIG.WRAP1));
		pup = new JPanel(new MigLayout(MIG.WRAP, "[][][]"));
		//family
		pup.add(initFamily(inFont));
		String fam = inFont.getFamily();
		if (fam.equalsIgnoreCase("sans serif")) {
			fam = "SansSerif";
		}
		if (fam.equalsIgnoreCase("monospace")) {
			fam = "Monospaced";
		}
		lsFamily.setSelectedValue(fam, true);
		lsFamily.addListSelectionListener((ListSelectionEvent e) -> {
			showSample();
		});
		//style
		pup.add(initStyle(inFont));
		lsStyle.setSelectedValue(styleList[inFont.getStyle()], true);
		lsStyle.addListSelectionListener((ListSelectionEvent e) -> {
			showSample();
		});
		//size
		pup.add(initSize(inFont));
		lsSize.addListSelectionListener((ListSelectionEvent e) -> {
			showSample();
		});
		add(pup);
		//sample
		sample.setText("The quick brown fox jumped over the lazy dog.");
		sample.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		add(sample, MIG.get(MIG.SPAN, MIG.CENTER));
		//ok+cancel
		add(initOkCancel(), MIG.get(MIG.SPAN, MIG.RIGHT));

		pack();
		this.setLocationRelativeTo(getParent());
		setSampleSize();
	}

	public void disableStyle() {
		lsStyle.setEnabled(false);
	}

	public void disableSize() {
		lsSize.setEnabled(false);
	}

	@SuppressWarnings("unchecked")
	private JPanel initFamily(Font initfont) {
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0, MIG.WRAP1)));
		p.add(new JLabel(I18N.getColonMsg("she.font.family")));
		lsFamily = new JList(GraphicsEnvironment
				.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
		p.add(new JScrollPane(lsFamily));
		return p;
	}

	@SuppressWarnings("unchecked")
	private JPanel initStyle(Font initfont) {
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0, MIG.WRAP1)));
		p.add(new JLabel(I18N.getColonMsg("she.font.style")));
		lsStyle = new JList(styleList);
		p.add(new JScrollPane(lsStyle));
		return p;
	}

	@SuppressWarnings("unchecked")
	private JPanel initSize(Font initfont) {
		int sz = initfont.getSize();
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0, MIG.WRAP1)));
		p.add(new JLabel(I18N.getColonMsg("she.font.size")));
		DefaultListModel<Integer> model = new DefaultListModel<>();
		int i = 0;
		Integer idx = 12;
		for (Integer v : sizeList) {
			model.addElement(v);
			if (v <= sz) {
				idx = v;
			}
			i++;
		}
		lsSize = new JList<>(model);
		lsSize.setPreferredSize(new Dimension(40, 800));
		lsSize.setSelectedValue(idx, true);
		p.add(new JScrollPane(lsSize), MIG.GROW);
		return p;
	}

	private JPanel initOkCancel() {
		JPanel p = new JPanel(new MigLayout());
		p.add(Ui.initButton("btOK", "ask.ok", ICONS.K.OK, "", e -> {
			cancel = false;
			dispose();
		}));
		p.add(Ui.initButton("btCancel", "ask.cancel", ICONS.K.CANCEL, "", e -> {
			dispose();
		}));
		return p;
	}

	public Font getSelectedFont() {
		String fontfamily = (String) lsFamily.getSelectedValue();
		int fontsize = 0;
		try {
			fontsize = lsSize.getSelectedValue();
		} catch (NumberFormatException nfe) {
			fontsize = 12;
		}
		int fontstyle = Font.PLAIN;
		switch (lsStyle.getSelectedIndex()) {
			case 1:
				fontstyle = Font.BOLD;
				break;
			case 2:
				fontstyle = Font.ITALIC;
				break;
			default:
				break;
		}
		return new Font(fontfamily, fontstyle, fontsize);
	}

	private void showSample() {
		int fontsize = 0;
		try {
			fontsize = lsSize.getSelectedValue();
		} catch (Exception nfe) {
			fontsize = 12;
		}
		// zoom the font size for visual rendering
		fontsize = (fontsize * App.fontGet().getSize()) / 12;
		int fontstyle = Font.PLAIN;
		switch (lsStyle.getSelectedIndex()) {
			case 1:
				fontstyle = Font.BOLD;
				break;
			case 2:
				fontstyle = Font.ITALIC;
				break;
			default:
				break;
		}
		sample.setFont(new Font((String) lsFamily.getSelectedValue(), fontstyle, fontsize));
		setSampleSize();
	}

	private void setSampleSize() {
		int h = sample.getFont().getSize() * 2, w = pup.getWidth() - sample.getFont().getSize();
		Dimension dim = new Dimension(w, h);
		sample.setSize(dim);
		sample.setPreferredSize(dim);
		sample.setMaximumSize(dim);
		sample.setMinimumSize(dim);
	}

	public boolean isCanceled() {
		return cancel;
	}

	/**
	 * remove size values out of limits
	 *
	 * @param min: minimal allowed size
	 * @param max: maximal allowed size
	 */
	public void setLimitedSize(int min, int max) {
		Integer idx = lsSize.getSelectedValue();
		DefaultListModel<Integer> model = (DefaultListModel<Integer>) lsSize.getModel();
		List<Integer> valuesToKeep = new ArrayList<>();
		for (int i = 0; i < model.getSize(); i++) {
			Integer value = model.getElementAt(i);
			if (value >= min && value <= max) {
				valuesToKeep.add(value);
			}
		}
		model.clear();
		for (Integer value : valuesToKeep) {
			model.addElement(value);
		}
		//select current font size
		try {
			lsSize.setSelectedValue(idx, true);
		} catch (Exception ex) {

		}
	}

}
