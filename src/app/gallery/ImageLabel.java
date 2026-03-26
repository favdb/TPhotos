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
package app.gallery;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import tools.DateUtil;
import tools.ImageUtil;
import tools.file.FileUtil;

/**
 *
 * @author favdb
 */
public class ImageLabel extends JLabel implements MouseListener {

	private static final String TT = "ImageLabel.";

	private static final int IMG_SZ = 128;
	public static final Color UNSELECTED = Color.LIGHT_GRAY,
			SELECTED = Color.RED, IN_ALBUM = Color.BLUE;
	public static final char SEL_NO = '0', SEL = 'R', SEL_ALBUM = 'B';
	private File file;
	private String comment;
	private char sel = '0';
	private boolean allowedSel;
	private Gallery gallery;

	public ImageLabel(Gallery gallery, File file, String comment, boolean allowed) {
		this.gallery = gallery;
		this.file = file;
		this.comment = comment;
		this.allowedSel = allowed;
		this.sel = SEL_NO;
		initialize();
	}

	private void initialize() {
		setVerticalTextPosition(JLabel.BOTTOM);
		setHorizontalTextPosition(JLabel.CENTER);
		setFile(file);
		setComment(comment);
		setSel(sel);
		this.addMouseListener(this);
		int height = (int) (IMG_SZ * 1.5);
		setMinimumSize(new Dimension(IMG_SZ, height));
		setPreferredSize(new Dimension(IMG_SZ, height));
	}

	private void setColor(char sel) {
		Color c = UNSELECTED;
		switch (sel) {
			case 'r':
			case 'R':
				c = SELECTED;
				break;
			case 'g':
			case 'G':
				c = Color.GREEN;
				break;
			case 'b':
			case 'B':
				c = IN_ALBUM;
				break;
		}
		setBorder(BorderFactory.createLineBorder(c, 2));
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
		String n = DateUtil.toFormatted(FileUtil.removeExtension(file.getName())).replace(" ", "<br>");
		setText("<html><p style=\"text-align: center;\">" + n + "</p></html>");
	}

	public void loadThumbnail() {
		this.setIcon(ImageUtil.getThumb(this.file, IMG_SZ));
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
		if (comment != null && !comment.isEmpty()) {
			setToolTipText(comment);
		}
	}

	public char getSel() {
		return sel;
	}

	public void setSel(char sel) {
		this.sel = sel;
		setColor(sel);
	}

	public void setAllowedSel(boolean b) {
		this.allowedSel = b;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		//LOG.trace(TT + "mouseClicked(e)");
		if (SwingUtilities.isRightMouseButton(e)) {
			gallery.showPopup(e, this);
			e.consume();
		} else if (e.getClickCount() == 2) {
			if (this.getSel() == SEL_ALBUM) {
				gallery.albumRemove(this);
			} else {
				gallery.albumAdd(this);
			}
			e.consume();
		} else if (e.getClickCount() == 1) {
			if (allowedSel) {
				switch (sel) {
					case 'R':
						setSel('0');
						break;
					case 'G':
						break;
					case 'B':
						break;
					case '0':
						setSel('R');
						break;
				}
				gallery.updateBtAdd();
			}
			e.consume();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		//empty
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		//empty
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		//empty
	}

	@Override
	public void mouseExited(MouseEvent e) {
		//empty
	}

}
