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

import api.mig.MIG;
import api.mig.swing.MigLayout;
import app.album.Album;
import app.i18n.I18N;
import app.media.Media;
import app.resources.icons.ICONS;
import app.resources.icons.IconUtil;
import app.tools.DateUtil;
import app.tools.LOG;
import app.tools.Ui;
import app.tools.file.FileUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
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

	public ChangeDateDlg(Album album, File infile) {
		super(SwingUtilities.windowForComponent(album));
		this.album = album;
		this.infile = infile;
		initialize();
	}

	@SuppressWarnings("unchecked")
	private void initialize() {
		this.setModal(true);
		this.setLayout(new MigLayout(MIG.WRAP + " 2"));
		this.setTitle(I18N.getMsg("album.param.comment.date"));
		add(new JLabel(getDateOf(infile)),
				MIG.get(MIG.CENTER, MIG.SPAN));
		add(new JLabel(I18N.getColonMsg("date.new")));

		//todo à changer en JDatePicker
		add(tfDate = new DateChooser());
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
			Date date = formatter.parse(infile.getName());
			tfDate.setDate(date);
		} catch (ParseException ex) {
			//empty
		}

		JPanel pok = new JPanel(new MigLayout());
		pok.add(Ui.initButton("ask.ok", ICONS.K.OK, e -> doOK()));
		pok.add(Ui.initButton("ask.cancel", ICONS.K.CANCEL, e -> {
			dispose();
		}));
		add(pok, MIG.get(MIG.SPAN, MIG.RIGHT));
		tfDate.setMinimumSize(new Dimension(IconUtil.getDefSize() * 8, IconUtil.getDefSize()));
		pack();
		this.setLocationRelativeTo(getParent());
	}

	/**
	 * OK action, check if valide date
	 */
	private void doOK() {
		tfDate.errorReset();
		boolean err = false;
		Date dateValue = (Date) tfDate.spDate.getValue();
		Date hourValue = (Date) tfDate.spHour.getValue();
		LocalDateTime minLdt = LocalDateTime.of(1900, 1, 1, 0, 0, 0);
		Date minDate = Date.from(minLdt.atZone(ZoneId.systemDefault()).toInstant());
		Date now = new Date();
		if (dateValue.after(now) || dateValue.before(minDate)) {
			tfDate.errorSet("date");
			err = true;
		}
		LocalDateTime ldtHour = hourValue.toInstant()
				.atZone(ZoneId.systemDefault()).toLocalDateTime();
		int hh = ldtHour.getHour();
		int mm = ldtHour.getMinute();
		int ss = ldtHour.getSecond();
		if (hh < 0 || hh > 23 || mm < 0 || mm > 59 || ss < 0 || ss > 59) {
			tfDate.errorSet("time");
			err = true;
		}
		if (err) {
			JOptionPane.showMessageDialog(this,
					I18N.getMsg("date.error"),
					I18N.getMsg("date"),
					JOptionPane.ERROR_MESSAGE);
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

		private JSpinner spDate, spHour;

		public DateChooser() {
			initialize();
		}

		private void initialize() {
			this.setLayout(new MigLayout());
			add(spDate = new JSpinner(new SpinnerDateModel()));
			JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(spDate, "dd/MM/yyyy");
			spDate.setEditor(timeEditor);
			add(spHour = new JSpinner(new SpinnerDateModel()));
			JSpinner.DateEditor hourEditor = new JSpinner.DateEditor(spHour, "HH:mm:ss");
			spHour.setEditor(hourEditor);
		}

		public void setDate(Date date) {
			spDate.setValue(date);
			spHour.setValue(date);
		}

		public String getDate() {
			LocalDateTime ld = ((Date) spDate.getValue()).toInstant()
					.atZone(ZoneId.systemDefault()).toLocalDateTime();
			LocalDateTime lh = ((Date) spHour.getValue()).toInstant()
					.atZone(ZoneId.systemDefault()).toLocalDateTime();
			String d = ld.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
			String h = lh.format(DateTimeFormatter.ofPattern("HHmmss"));
			return d + "_" + h;
		}

		private void errorSet(String val) {
			LOG.trace("DateChooser.errorSet(val=" + val + ")");
			if (val.equals("date")) {
				spDate.setBorder(BorderFactory.createLineBorder(Color.red));
			}
			if (val.equals("time")) {
				spHour.setBorder(BorderFactory.createLineBorder(Color.red));
			}
		}

		private void errorReset() {
			JTextField tf = new JTextField();
			spDate.setBorder(tf.getBorder());
			spHour.setBorder(tf.getBorder());
		}

	}
}
