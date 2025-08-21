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

import api.mig.swing.MigLayout;
import app.MainFrame;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import resources.icons.ICONS;
import resources.icons.IconUtil;
import tools.MIG;

/**
 *
 * @author favdb
 */
public class Diaporama extends JFrame {

	private JLabel lbImage;
	private static Dimension SCREEN = Toolkit.getDefaultToolkit().getScreenSize();
	private int SZIMAGE;

	private JLabel lbText;
	private final MainFrame mainFrame;
	private int nbImage;
	private AlbumTable table;

	public Diaporama(MainFrame mainFrame) {
		super();
		this.mainFrame = mainFrame;
		initialize();

	}

	private void initialize() {
		table = mainFrame.getAlbumPanel().getTable();
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setLayout(new MigLayout(MIG.get(MIG.INS0, MIG.GAP0, MIG.FILL, MIG.WRAP1)));
		Container p = this.getContentPane();
		this.addKeyListener(new KeyListener());
		initImage((JPanel) p);

		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setUndecorated(true);
		this.setVisible(true);
		SwingUtilities.invokeLater(() -> {
			this.navImage(-1);
		});
	}

	public JPanel initImage(JPanel p) {
		p.setBackground(Color.BLACK);
		p.setForeground(Color.BLUE);
		lbImage = new JLabel(IconUtil.getIconSmall(ICONS.K.HELP), JLabel.CENTER);
		lbImage.setBackground(Color.BLACK);
		lbImage.setOpaque(true);
		p.add(lbImage, MIG.get(MIG.CENTER, MIG.GROW));

		lbText = new JLabel(" ");
		lbText.setOpaque(true);
		lbText.setHorizontalAlignment(JLabel.CENTER);
		lbText.setBackground(Color.BLACK);
		lbText.setForeground(Color.WHITE);
		Font fnt = lbText.getFont();
		Font fnt2 = new Font(Font.SANS_SERIF, Font.BOLD, (int) (fnt.getSize() * 1.1));
		lbText.setFont(fnt2);
		p.add(lbText, MIG.CENTER);
		return p;
	}

	public void setImage(File file, String text) {
		int sz = SCREEN.height - (int) (lbText.getFont().getSize() * 2);
		updateSize(lbImage, sz);
		lbImage.setIcon(IconUtil.getJpegIconFromFile(file, new Dimension(SCREEN.width, sz)));
		lbText.setText(text.trim());
	}

	private void updateSize(JComponent comp, int sz) {
		//int sz = SCREEN.height - (lbText.getFont().getSize() * 2);
		Dimension dim = new Dimension(SCREEN.width, sz);
		comp.setMaximumSize(dim);
		comp.setMinimumSize(dim);
		comp.setPreferredSize(SCREEN);
	}

	int curImage = 0;

	public void navImage(int n) {
		if (n == Integer.MAX_VALUE) {
			curImage = table.getRowCount() - 1;
		} else {
			curImage += n;
			if (curImage < 0) {
				curImage = 0;
			} else if (curImage >= table.getRowCount()) {
				dispose();
				return;
			}
		}
		setImage((File) table.getValueAt(curImage, 1), (String) table.getValueAt(curImage, 2));
	}

	public void close() {
		dispose();
	}

	private class KeyListener implements java.awt.event.KeyListener {

		public KeyListener() {
		}

		@Override
		public void keyTyped(KeyEvent e) {
			//empty
		}

		@Override
		public void keyPressed(KeyEvent e) {
			int key = e.getKeyCode();
			switch (key) {
				case KeyEvent.KEY_FIRST:
				case KeyEvent.VK_HOME:
					curImage = -1;
					navImage(1);
					break;
				case KeyEvent.VK_RIGHT:
					navImage(1);
					break;
				case KeyEvent.VK_LEFT:
					navImage(-1);
					break;
				case KeyEvent.KEY_LAST:
				case KeyEvent.VK_END:
					navImage(Integer.MAX_VALUE);
					break;
				case KeyEvent.VK_ESCAPE:
					close();
					break;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			//empty
		}
	}

}
