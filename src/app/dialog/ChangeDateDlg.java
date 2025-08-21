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
package app.dialog;

import api.mig.swing.MigLayout;
import app.App;
import app.album.Album;
import i18n.I18N;
import java.awt.Dimension;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import tools.DateUtil;
import tools.MIG;
import tools.Ui;
import tools.file.FileUtil;
import tools.jpeg.Jpeg;

/**
 *
 * @author favdb
 */
public class ChangeDateDlg extends JDialog {

	private static final String TT = "ChangeDateDlg.";
	private final Album album;
	private File infile;
	private DateChooser tfDate;
	private JComboBox cbMode;
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
		add(new JLabel(I18N.getColonMsg("date.actual") + getDateOf(infile)), MIG.SPAN);
		add(new JLabel(I18N.getColonMsg("date.new")), MIG.RIGHT);
		add(tfDate = new DateChooser());
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
			Date date = formatter.parse(infile.getName());
			tfDate.setDate(date);
		} catch (ParseException ex) {
			//empty
		}
		add(new JLabel(I18N.getColonMsg("organise.by")), MIG.RIGHT);
		add(cbMode = new JComboBox(new String[]{
			I18N.getMsg("organise.by_year"),
			I18N.getMsg("organise.by_month"),
			I18N.getMsg("organise.by_day"),
			I18N.getMsg("organise.by_none")
		}));
		cbMode.setSelectedIndex(App.preferences.organizeTypeGet());

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

	private void doOK() {
		App.preferences.organizeTypeSet(cbMode.getSelectedIndex());
		cancel = false;
		dispose();
	}

	public int getMode() {
		return cbMode.getSelectedIndex();
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
			Jpeg jpeg = new Jpeg(file);
			if (jpeg.exif != null) {
				str = jpeg.exif.getDate();
				if (str != null) {
					b.append("EXIF=");
				}
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

		private JSpinner spDate;

		public DateChooser() {
			initialize();
		}

		private void initialize() {
			this.setLayout(new MigLayout());
			add(spDate = new JSpinner(new SpinnerDateModel()));
			JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(spDate, "dd/MM/yyyy HH:mm:ss");
			spDate.setEditor(timeEditor);
		}

		public void setDate(Date date) {
			spDate.setValue(date);
		}

		public String getDate() {
			LocalDateTime lt = ((Date) spDate.getValue()).toInstant()
					.atZone(ZoneId.systemDefault()).toLocalDateTime();
			return lt.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
		}

	}
}
