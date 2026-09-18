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
package app.album;

import app.media.Media;
import app.resources.icons.ICONS;
import app.resources.icons.IconUtil;
import app.tools.DateUtil;
import app.tools.ImageUtil;
import app.tools.LOG;
import app.tools.file.FileUtil;
import app.zdlg.ShowPhoto;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Class for a gallery cell panel containing an image thumbnail and date label.
 *
 * @author favdb
 */
public class AlbumGalleryCell extends JPanel implements MouseListener {

	private static final String TT = "ImageLabel.";

	public static final Color UNSELECTED = Color.LIGHT_GRAY,
			SELECTED = Color.RED, IN_ALBUM = Color.BLUE;
	// selection type:0=no selection, R=selected, B=is in albumTable
	public static final char SEL_NO = '0', SEL = 'R', SEL_ALBUM = 'B';

	private File file;
	private String comment;
	private char sel = '0';
	private boolean allowSel;
	private final AlbumGallery gallery;
	private Timer clickTimer;

	private JLabel lbImage;
	private JLabel lbText;

	public AlbumGalleryCell(AlbumGallery gallery, File file, String comment, boolean allowed) {
		super();
		this.gallery = gallery;
		this.file = file;
		this.comment = comment;
		this.allowSel = allowed;
		this.sel = SEL_NO;
		initialize();
	}

	/**
	 * initialize
	 */
	private void initialize() {
		setLayout(new BorderLayout(0, 0));
		setOpaque(false);
		lbImage = new JLabel();
		lbImage.setHorizontalAlignment(SwingConstants.CENTER);
		lbImage.setVerticalAlignment(SwingConstants.CENTER);
		lbText = new JLabel();
		lbText.setHorizontalAlignment(SwingConstants.CENTER);
		lbText.setVerticalAlignment(SwingConstants.CENTER);
		lbText.setBorder(BorderFactory.createEmptyBorder(-4, 0, 0, 0));
		add(lbImage, BorderLayout.CENTER);
		add(lbText, BorderLayout.SOUTH);
		fileSet(file);
		commentSet(comment);
		selSet(sel);
		addMouseListener(this);
		lbImage.addMouseListener(this);
		lbText.addMouseListener(this);
		int width = gallery.imgSzGet();
		int height = (int) (width * 1.5);
		setMinimumSize(new Dimension(width, height));
		setPreferredSize(new Dimension(width, height));
		clickTimer = new Timer(250, e -> actionSimpleClick());
		clickTimer.setRepeats(false);
	}

	/**
	 * simple click action
	 */
	private void actionSimpleClick() {
		if (allowSel) {
			switch (sel) {
				case 'R':
					selSet('0');
					break;
				case 'G':
					break;
				case 'B':
					break;
				case '0':
					selSet('R');
					break;
			}
			gallery.btAddUpdate();
		}
	}

	/**
	 * set the border color
	 *
	 * @param sel
	 */
	private void colorSet(char sel) {
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
		String n = DateUtil.toFormatted(FileUtil
				.removeExtension(file.getName())).replace(" ", "<br>");
		if (n.equals("???")) {
			lbText.setText(file.getName());
		} else {
			lbText.setText("<html><center>" + n + "</center></html>");
		}
	}

	/**
	 * load the thumbnails
	 */
	public void loadThumbnail() {
		if (Media.mp4Is(file)) {
			lbImage.setIcon(IconUtil.getIconLarge(ICONS.K.VIDEO, gallery.imgSzGet()));
		} else {
			lbImage.setIcon(ImageUtil.getThumb(this.file, gallery.imgSzGet()));
		}
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
	public void commentSet(String comment) {
		this.comment = comment;
		if (comment != null && !comment.isEmpty()) {
			setToolTipText(comment);
			lbImage.setToolTipText(comment);
			lbText.setToolTipText(comment);
		}
	}

	/**
	 * get selection status
	 *
	 * @return
	 */
	public char selGet() {
		return sel;
	}

	/**
	 * set selection status
	 *
	 * @param sel
	 */
	public void selSet(char sel) {
		this.sel = sel;
		colorSet(sel);
	}

	/**
	 * set allowed selection
	 *
	 * @param b
	 */
	public void selAllow(boolean b) {
		this.allowSel = b;
	}

	/**
	 * check for popup action
	 *
	 * @param e
	 */
	private void popupCheckFor(MouseEvent e) {
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
		LOG.trace(TT + "mouseClicked(e)");
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (e.getClickCount() == 1) {
				clickTimer.start();
			} else if (e.getClickCount() == 2) {
				if (clickTimer.isRunning()) {
					clickTimer.stop();
				}
				try {
					if (Media.mp4Is(file)) {
						try {
							Desktop.getDesktop().open(fileGet());
						} catch (IOException ex) {
							LOG.err("unable to open MP4 file", ex);
						}
					}
					ShowPhoto.show(file, gallery.cellListGet());
				} catch (Exception ex) {
					LOG.err(TT + "show photo error", ex);
				}
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		popupCheckFor(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		popupCheckFor(e);
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
