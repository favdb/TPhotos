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
package app.zdlg;

import api.jdatechooser.calendar.JDateChooser;
import app.album.Album;
import app.i18n.I18N;
import app.media.Media;
import app.resources.icons.ICONS;
import app.resources.icons.IconUtil;
import app.tools.DateUtil;
import app.tools.GBC;
import app.tools.LOG;
import app.tools.Ui;
import app.tools.file.FileUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;

/**
 *
 * @author favdb
 */
public class ChangeDateDlg extends JDialog {

	private static final String TT = "ChangeDateDlg.";
	private final Album album;
	private final File infile;
	private DateChooser tfDate;
	private boolean cancel = true;
	private Date date;

	public ChangeDateDlg(Album album, File infile) {
		super(SwingUtilities.windowForComponent(album));
		this.album = album;
		this.infile = infile;
		initialize();
	}

	@SuppressWarnings("unchecked")
	private void initialize() {
		this.setModal(true);
		this.setLayout(new GridBagLayout());
		this.setTitle(I18N.getMsg("album.param.comment.date"));

		// Ligne 0 : Informations date fichier (centré, largeur 2 colonnes)
		add(new JLabel(getDateOf(infile)), new GBC("0, 0, center, gw 2, insets 5 5 5 5"));

		// Ligne 1, Col 0 : Label "Nouvelle date :"
		add(new JLabel(I18N.getColonMsg("date.new")), new GBC("1, 0, left, insets 0 5 5 5"));

		// Ligne 1, Col 1 : Composant DateChooser
		add(tfDate = new DateChooser(), new GBC("1, 1, left, insets 0 0 5 5"));

		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
			date = formatter.parse(infile.getName());
			tfDate.setDate(date);
		} catch (ParseException ex) {
			//empty
		}

		// Ligne 2 : Panneau de boutons OK / Annuler (aligné à droite, largeur 2 colonnes)
		JPanel pok = new JPanel(new GridBagLayout());
		pok.add(Ui.initButton("ask.ok", ICONS.K.OK, e -> doOK()), new GBC("0, 0, insets 0 0 0 5"));
		pok.add(Ui.initButton("ask.cancel", ICONS.K.CANCEL, e -> dispose()), new GBC("0, 1"));

		add(pok, new GBC("2, 0, right, gw 2, insets 5 5 5 5"));

		tfDate.setMinimumSize(new Dimension(IconUtil.getDefSize() * 8, IconUtil.getDefSize()));
		pack();
		this.setLocationRelativeTo(getParent());
	}

	/**
	 * OK action, check if valide date
	 */
	/**
	 * OK action, check if valid date
	 */
	private void doOK() {
		tfDate.errorReset();
		StringBuilder errorMsg = new StringBuilder();

		Date dateValue = tfDate.dateChooser.getDate();
		Date hourValue = (Date) tfDate.spHour.getValue();

		// 1. Vérification de la présence de la date
		if (dateValue == null) {
			tfDate.errorSet("date");
			errorMsg.append("- ").append(I18N.getMsg("error.date.missing")).append("\n");
		} else {
			// 2. Vérification de l'intervalle de date (1900 à aujourd'hui)
			LocalDateTime minLdt = LocalDateTime.of(1900, 1, 1, 0, 0, 0);
			Date minDate = Date.from(minLdt.atZone(ZoneId.systemDefault()).toInstant());
			Date now = new Date();

			if (dateValue.after(now)) {
				//tfDate.errorSet("date");
				errorMsg.append("- ").append(I18N.getMsg("error.date.future")).append("\n");
			} else if (dateValue.before(minDate)) {
				//tfDate.errorSet("date");
				errorMsg.append("- ").append(I18N.getMsg("error.date.too_old")).append("\n");
			}
		}

		// 3. Vérification de l'heure
		if (hourValue == null) {
			tfDate.errorSet("time");
			errorMsg.append("- ").append(I18N.getMsg("error.time.invalid")).append("\n");
		} else {
			LocalDateTime ldtHour = hourValue.toInstant()
					.atZone(ZoneId.systemDefault()).toLocalDateTime();
			int hh = ldtHour.getHour();
			int mm = ldtHour.getMinute();
			int ss = ldtHour.getSecond();
			if (hh < 0 || hh > 23 || mm < 0 || mm > 59 || ss < 0 || ss > 59) {
				tfDate.errorSet("time");
				errorMsg.append("- ").append(I18N.getMsg("error.time.invalid")).append("\n");
			}
		}

		// Affichage du message détaillé si au moins une erreur est détectée
		if (errorMsg.length() > 0) {
			JOptionPane.showMessageDialog(this,
					I18N.getMsg("error.date") + " :\n" + errorMsg.toString(),
					I18N.getMsg("date"),
					JOptionPane.ERROR_MESSAGE);
			//tfDate = new DateChooser();
			//tfDate.setDate(date);
			return;
		}

		cancel = false;
		dispose();
	}

	public String getDate() {
		return tfDate.getDate();
	}

	public boolean isCancel() {
		return cancel;
	}

	private String getDateOf(File file) {
		try {
			StringBuilder b = new StringBuilder();
			String str;
			if (Media.whichDate(file) != null) {
				str = Media.getDate(file);
				b.append("Date ").append(Media.whichDate(file)).append("=");
			} else {
				str = FileUtil.removeExtension(file.getName());
				b.append("Date fichier=");
			}
			b.append(DateUtil.toFormatted(str));
			return b.toString();
		} catch (Exception ex) {
			return "???";
		}
	}

	private class DateChooser extends JPanel {

		private JSpinner spHour;
		private JDateChooser dateChooser;
		private javax.swing.border.Border defaultTfBorder;
		private javax.swing.border.Border defaultSpBorder;

		public DateChooser() {
			initialize();
		}

		private void initialize() {
			this.setLayout(new GridBagLayout());
			dateChooser = new JDateChooser();
			dateChooser.setDateFormatString("dd/MM/yyyy");

			spHour = new JSpinner(new SpinnerDateModel());
			JSpinner.DateEditor hourEditor = new JSpinner.DateEditor(spHour, "HH:mm:ss");
			spHour.setEditor(hourEditor);

			// Sauvegarde des bordures d'origine des composants éditables
			if (dateChooser.getDateEditor().getUiComponent() instanceof JComponent) {
				defaultTfBorder = ((JComponent) dateChooser
						.getDateEditor().getUiComponent()).getBorder();
			}
			defaultSpBorder = spHour.getBorder();

			add(dateChooser, new GBC("0, 0, left, insets 0 0 0 5"));
			add(spHour, new GBC("0, 1, left"));
		}

		public void setDate(Date date) {
			dateChooser.setDate(date);
			spHour.setValue(date);
		}

		public String getDate() {
			LocalDateTime cd = ((Date) dateChooser.getDate()).toInstant()
					.atZone(ZoneId.systemDefault()).toLocalDateTime();
			LocalDateTime lh = ((Date) spHour.getValue()).toInstant()
					.atZone(ZoneId.systemDefault()).toLocalDateTime();
			String d = cd.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
			String h = lh.format(DateTimeFormatter.ofPattern("HHmmss"));
			return d + "_" + h;
		}

		private void errorSet(String val) {
			LOG.trace("DateChooser.errorSet(val=" + val + ")");
			if (val.equals("date")) {
				if (dateChooser.getDateEditor().getUiComponent() instanceof JComponent) {
					((JComponent) dateChooser.getDateEditor().getUiComponent())
							.setBorder(BorderFactory.createLineBorder(Color.red));
				}
			}
			if (val.equals("time")) {
				spHour.setBorder(BorderFactory.createLineBorder(Color.red));
			}
		}

		private void errorReset() {
			if (dateChooser.getDateEditor().getUiComponent() instanceof JComponent) {
				((JComponent) dateChooser.getDateEditor().getUiComponent())
						.setBorder(defaultTfBorder);
			}
			spHour.setBorder(defaultSpBorder);
		}

	}
}
