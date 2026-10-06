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

import app.album.Album;
import app.diapo.DiapoParam;
import app.i18n.I18N;
import app.resources.icons.ICONS;
import app.tools.GBC;
import app.tools.LOG;
import app.tools.Ui;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author favdb
 */
public class CommentParamDlg extends JDialog {

	private static final String TT = "CommentParamDlg.";

	public static boolean showing(Album panel, boolean b) {
		CommentParamDlg dlg = new CommentParamDlg(panel, b);
		dlg.setVisible(true);
		return !dlg.isCanceled();
	}
	private final Album album;
	private JTextField tfComment;
	private final DiapoParam param;
	private JComboBox cbDate;
	private JButton btAdd;
	private boolean canceled = true, saveComment = true;

	public CommentParamDlg(Album album, boolean b) {
		super();
		this.saveComment = b;
		this.setModal(true);
		this.album = album;
		param = album.diapoParamGet();
		initialize();
	}

	@SuppressWarnings("unchecked")
	private void initialize() {
		setLayout(new GridBagLayout());
		this.setTitle(I18N.getMsg("album.param.comment_tips"));
		String[] ls = {
			I18N.getMsg("album.param.comment.year"),
			I18N.getMsg("album.param.comment.month"),
			I18N.getMsg("album.param.comment.month_long"),
			I18N.getMsg("album.param.comment.day"),
			I18N.getMsg("album.param.comment.full"),
			I18N.getMsg("album.param.comment.full_hour")
		};

		// Ligne 0 : Date selector
		add(new JLabel(I18N.getColonMsg("album.param.comment.date")), new GBC("0, 0, left, ins 2"));
		cbDate = new JComboBox(ls);
		add(cbDate, new GBC("0, 1, left, ins 2"));
		
		btAdd = Ui.initIconButton("btAdd", ICONS.K.AR_DOWN,
				e -> {
					String str = tfComment.getText()
					+ "{" + cbDate.getSelectedItem() + "}";
					tfComment.setText(str);
				});
		btAdd.setToolTipText(I18N.getMsg("album.param.adddate"));
		add(btAdd, new GBC("0, 2, left, ins 2"));

		// Ligne 1 : Comment field
		add(new JLabel(I18N.getColonMsg("album.param.comment")), new GBC("1, 0, left, ins 2"));
		tfComment = new JTextField(param.getComment());
		tfComment.setColumns(32);
		add(tfComment, new GBC("1, 1, gw 2, fill h, wx 1.0, ins 2"));

		// Ligne 2 : Buttons OK / Cancel
		JPanel p = new JPanel(new GridBagLayout());
		p.add(Ui.initButton("ask.cancel", ICONS.K.CANCEL, e -> dispose()), new GBC("0, 0, ins 2"));
		p.add(Ui.initButton("ask.ok", ICONS.K.OK, e -> doOK()), new GBC("0, 1, ins 2"));
		add(p, new GBC("2, 0, gw 3, right, ins 5"));

		pack();
		setLocationRelativeTo(album);
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
		LOG.trace(TT + "doOK()");
		// set param mode and tempo
		if (saveComment) {
			param.setComment(tfComment.getText());
			album.xmlGet().albumGet().setPrefComment(tfComment.getText());
			//album.xmlGet().save();
			album.tableGet().setModified();
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