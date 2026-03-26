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
package app.album;

import api.mig.MIG;
import api.mig.swing.MigLayout;
import i18n.I18N;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import resources.icons.ICONS;
import tools.Ui;

/**
 *
 * @author favdb
 */
public class AlbumParamDlg extends JDialog {

	private static final String TT = "AlbumParamDlg.";

	public static boolean showing(Album panel, boolean b) {
		AlbumParamDlg dlg = new AlbumParamDlg(panel, b);
		dlg.setVisible(true);
		return !dlg.isCanceled();
	}
	private final Album albumPanel;
	private JTextField tfComment;
	private AlbumParam param;
	private JComboBox cbDate;
	private JButton btAdd;
	private boolean canceled = true, saveComment = true;

	public AlbumParamDlg(Album mainFrame, boolean b) {
		super();
		this.saveComment = b;
		this.setModal(true);
		this.albumPanel = mainFrame;
		param = mainFrame.getAlbumParam();
		initialize();
	}

	@SuppressWarnings("unchecked")
	private void initialize() {
		setLayout(new MigLayout());
		this.setTitle(I18N.getMsg("album.param.comment_tips"));
		// param for mode and tempo
		String[] ls = {
			I18N.getMsg("album.param.comment.year"),
			I18N.getMsg("album.param.comment.month"),
			I18N.getMsg("album.param.comment.month_long"),
			I18N.getMsg("album.param.comment.day"),
			I18N.getMsg("album.param.comment.full"),
			I18N.getMsg("album.param.comment.full_hour")
		};
		add(new JLabel(I18N.getColonMsg("album.param.comment.date")));
		cbDate = new JComboBox(ls);
		add(cbDate, MIG.get(MIG.SPAN, MIG.SPLIT2));
		add(btAdd = Ui.initIconButton("btAdd", ICONS.K.COGS, e -> {
			String str = tfComment.getText() + "{" + cbDate.getSelectedItem() + "}";
			tfComment.setText(str);
		}), MIG.WRAP);
		btAdd.setToolTipText(I18N.getMsg("album.param.adddate"));
		// param for comments
		add(new JLabel(I18N.getColonMsg("album.param.comment")));
		add(tfComment = new JTextField(param.getComment()), MIG.WRAP);
		tfComment.setColumns(32);
		// ok cancel buttons
		JPanel p = new JPanel(new MigLayout());
		p.add(Ui.initButton("ask.cancel", ICONS.K.CANCEL, e -> dispose()));
		p.add(Ui.initButton("ask.ok", ICONS.K.OK, e -> doOK()));
		add(p, MIG.get(MIG.SPAN, MIG.RIGHT));
		pack();
		setLocationRelativeTo(albumPanel);
	}

	private void addDate(String fmt) {
		StringBuilder b = new StringBuilder(tfComment.getText());
		if (b.length() > 0) {
			b.append(" ");
		}
		b.append("{").append(fmt).append("}");
		tfComment.setText(b.toString());
	}

	private void doOK() {
		// set param mode and tempo
		if (saveComment) {
			param.setComment(tfComment.getText());
			albumPanel.getTable().setModified();
		}
		canceled = false;
		dispose();
	}

	public boolean isCanceled() {
		return canceled;
	}

	public String getComment() {
		return tfComment.getText();
	}

}
