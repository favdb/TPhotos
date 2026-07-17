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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import tools.DateUtil;
import tools.ImageUtil;
import tools.LOG;
import tools.file.FileUtil;

/**
 * class for a JLabel of an image
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
	private Timer clickTimer;

	public ImageLabel(Gallery gallery, File file, String comment, boolean allowed) {
		this.gallery = gallery;
		this.file = file;
		this.comment = comment;
		this.allowedSel = allowed;
		this.sel = SEL_NO;
		initialize();
	}

	/**
	 * initialize
	 */
	private void initialize() {
		setVerticalTextPosition(JLabel.BOTTOM);
		setHorizontalTextPosition(JLabel.CENTER);
		fileSet(file);
		setComment(comment);
		setSel(sel);
		this.addMouseListener(this);
		int height = (int) (IMG_SZ * 1.5);
		setMinimumSize(new Dimension(IMG_SZ, height));
		setPreferredSize(new Dimension(IMG_SZ, height));
		clickTimer = new javax.swing.Timer(250, javaEvent -> {
			executeSimpleClickAction();
		});
		clickTimer.setRepeats(false);
	}

	/**
	 * simple click action
	 */
	private void executeSimpleClickAction() {
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
	}

	/**
	 * the the color
	 *
	 * @param sel
	 */
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

	/**
	 * get the file
	 *
	 * @return
	 */
	public File fileGet() {
		return file;
	}

	/**
	 * set the file
	 *
	 * @param file
	 */
	public void fileSet(File file) {
		this.file = file;
		String n = DateUtil.toFormatted(FileUtil.removeExtension(file.getName())).replace(" ", "<br>");
		setText("<html><p style=\"text-align: center;\">" + n + "</p></html>");
	}

	/**
	 * load the thumnails
	 */
	public void loadThumbnail() {
		this.setIcon(ImageUtil.getThumb(this.file, IMG_SZ));
	}

	/**
	 * get the comment text
	 *
	 * @return
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * set the comment text
	 *
	 * @param comment
	 */
	public void setComment(String comment) {
		this.comment = comment;
		if (comment != null && !comment.isEmpty()) {
			setToolTipText(comment);
		}
	}

	/**
	 * get selection status
	 *
	 * @return
	 */
	public char getSel() {
		return sel;
	}

	/**
	 * set selection status
	 *
	 * @param sel
	 */
	public void setSel(char sel) {
		this.sel = sel;
		setColor(sel);
	}

	/**
	 * set allowed selection
	 *
	 * @param b
	 */
	public void setAllowedSel(boolean b) {
		this.allowedSel = b;
	}

	/**
	 * check for popup action
	 *
	 * @param e
	 */
	private void checkForPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			if (clickTimer != null && clickTimer.isRunning()) {
				clickTimer.stop();
			}
			gallery.showPopup(e, this);
			e.consume();
		}
	}

	//** mouse actions **//
	@Override
	public void mouseClicked(MouseEvent e) {
		//LOG.trace(TT + "mouseClicked(e)");
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (e.getClickCount() == 1) {
				clickTimer.start();
			} else if (e.getClickCount() == 2) {
				if (clickTimer.isRunning()) {
					clickTimer.stop();
				}
				try {
					Desktop.getDesktop().open(file);
				} catch (IOException ex) {
					LOG.err(TT + "open file error", ex);
				}
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		checkForPopup(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		checkForPopup(e);
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
