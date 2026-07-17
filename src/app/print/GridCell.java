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
package app.print;

import app.xml.XmlPrintPage;
import i18n.I18N;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import tools.ImageUtil;

public class GridCell extends JLabel {

	private static final String TT = "GridImage.";

	private PrintItem item;
	private final Grid grid;
	private static GridCell selectedSource = null;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public GridCell(Grid grid, PrintItem item) {
		this.grid = grid;
		this.item = item;
		initialize();
		setupInteractions();
	}

	public void initialize() {
		this.setLayout(new BorderLayout());
		//this.setBorder(normalBorder);
		this.setPreferredSize(new Dimension(100, 80));
		this.setOpaque(true);
		this.setBackground(Color.WHITE);
		this.setHorizontalAlignment(SwingConstants.CENTER);
		this.setVerticalAlignment(SwingConstants.CENTER);
		refresh();
	}

	public void refresh() {
		//LOG.trace(TT + "refresh()" + item.toString());
		this.removeAll();
		this.setIcon(null);
		this.setText("");

		// Largeur et hauteur disponibles pour s'adapter dynamiquement
		int w = grid.getPreferredSize().width / grid.colsGet(); // Ou utilisez grid.imgGetSize().width
		int h = grid.getPreferredSize().height / grid.rowsGet(); // Ou utilisez grid.imgGetSize().height
		if (w <= 0 || h <= 0) {
			return;
		}
		int sz = Math.min(w, h);
		this.setPreferredSize(new Dimension(sz, sz));
		//this.setMinimumSize(new Dimension(w, h));
		//this.setMaximumSize(new Dimension(w, h));

		if (item.isPhoto()) {
			//this.setBorder(normalBorder);
			this.setBackground(Color.WHITE);
			if (item.photoFileGet() != null && !item.photoFileGet().isEmpty()) {
				this.setIcon(ImageUtil.getImage(item.photoFileGet(), sz - 25));
			} else {
				this.setText("Photo vide (#" + item.photoIdGet() + ")");
				this.setHorizontalAlignment(JLabel.CENTER);
			}
		} else if (item.isText()) {
			//this.setBorder(normalBorder);
			this.setBackground(new Color(255, 255, 245)); // Fond ivoire léger pour différencier le texte

			// Traitement du texte sous forme d'image avec ajustement dynamique (zoom de travail)
			String txt = "<html><body style='padding:1px; font-size:10px;'>" + item.textGet() + "</body></html>";
			this.setIcon(ImageUtil.createTextImage(txt, sz - 40));
			this.setHorizontalAlignment(JLabel.CENTER);
			this.setVerticalAlignment(JLabel.CENTER);
		} else {
			// Affichage pour une case vide/non attribuée du tableau
			this.setBorder(BorderFactory.createDashedBorder(Color.LIGHT_GRAY, 2, 2, 1, false));
			this.setBackground(new Color(248, 248, 248));
			this.setText(String.valueOf(item.cellNumGet()));
			this.setFont(this.getFont().deriveFont(14.0f));
			this.setForeground(Color.LIGHT_GRAY);
			this.setHorizontalAlignment(JLabel.CENTER);
			this.setVerticalAlignment(JLabel.CENTER);
		}
		this.revalidate();
		this.repaint();
	}

	private void setupInteractions() {
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showContextMenu(e);
				} else {
					// 1. Vérifier d'abord s'il y a un élément du Pool en attente de placement
					Print print = grid.getPrint();
					PrintItem cellToPlace = print.pendingCellToPlaceGet();

					if (cellToPlace != null) {
						// Si la cellule sur laquelle on clique est vide, on y place le contenu
						if (!item.isPhoto() && !item.isText()) {
							placePendingCell(cellToPlace);
						} else {
							// Optionnel : Si l'emplacement n'est pas vide, on peut au choix annuler le placement
							// ou ignorer. Ici on nettoie la sélection en attente pour éviter les conflits.
							print.pendingCellClear();
							grid.getPrint().poolGet().tree.clearSelection();
						}
					} else {
						// 2. Comportement d'origine (sélection / échange de contenu)
						handleSelection();
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showContextMenu(e);
				}
			}
		});
	}

	/**
	 * Place la cellule provenant du Pool dans cette cellule vide de la grille
	 */
	private void placePendingCell(PrintItem cellToPlace) {
		//LOG.trace(TT + "placePendingCell(cellToPlace=" + cellToPlace.toString() + ") to " + item.posGet());
		cellToPlace.pageSet(grid.getPrint().gridCurrentPageGet());
		cellToPlace.posSet(item.posGet());

		grid.getPrint().actionSave();
	}

	/**
	 * handle action for selection
	 */
	private void handleSelection() {
		if (selectedSource == null) {
			if (item.photoIdGet() != -1 || item.textIdGet() != -1 || !item.textGet().isEmpty()) {
				selectedSource = this;
				this.setBorder(Print.BORDER_SELECTED);
			}
		} else {
			if (selectedSource == this) {
				selectedSource = null;
				this.setBorder(Print.BORDER_NORMAL);
				this.refresh();
			} else {
				PrintItem sourceCell = selectedSource.item;
				PrintItem targetCell = this.item;
				sourceCell.swapContentWith(targetCell);
				sourceCell.spanHorizontalSet(1);
				sourceCell.spanVerticalSet(1);
				targetCell.spanHorizontalSet(1);
				targetCell.spanVerticalSet(1);
				int indexPage = grid.getPrint().gridCurrentPageGet() - 1;
				XmlPrintPage page = grid.getPrint().printPagesGet().get(indexPage);
				if (!page.cellsGet().contains(targetCell)) {
					page.cellsGet().add(targetCell);
				}
				if (!sourceCell.isPhoto() && !sourceCell.isText()) { //
					page.cellsGet().remove(sourceCell);
				}
				selectedSource.setBorder(Print.BORDER_NORMAL);
				selectedSource = null;
				grid.setModified();
				grid.refresh();
			}
		}
	}

	private void showContextMenu(MouseEvent e) {
		if (grid == null) {
			return;
		}
		JPopupMenu menu = new JPopupMenu();
		int totalCols = grid.colsGet();
		int totalRows = grid.rowsGet();
		int cellNum = item.cellNumGet();
		int col = (cellNum - 1) % totalCols;
		int row = (cellNum - 1) / totalCols;
		int maxAllowedSpanH = Math.min(3, totalCols - col);
		int maxAllowedSpanV = Math.min(3, totalRows - row);

		JMenuItem incSpanH = new JMenuItem(I18N.getMsg("print.menu.spanh.inc") + " (+1)");
		incSpanH.setEnabled(item.spanHorizontalGet() < maxAllowedSpanH);
		incSpanH.addActionListener(al -> {
			item.spanHorizontalSet(item.spanHorizontalGet() + 1);
			grid.refresh();
		});

		JMenuItem decSpanH = new JMenuItem(I18N.getMsg("print.menu.spanh.dec") + " (-1)");
		decSpanH.setEnabled(item.spanHorizontalGet() > 1);
		decSpanH.addActionListener(al -> {
			item.spanHorizontalSet(item.spanHorizontalGet() - 1);
			grid.refresh();
		});

		JMenuItem incSpanV = new JMenuItem(I18N.getMsg("print.menu.spanv.inc") + " (+1)");
		incSpanV.setEnabled(item.spanVerticalGet() < maxAllowedSpanV);
		incSpanV.addActionListener(al -> {
			item.spanVerticalSet(item.spanVerticalGet() + 1);
			grid.refresh();
		});

		JMenuItem decSpanV = new JMenuItem(I18N.getMsg("print.menu.spanv.dec") + " (-1)");
		decSpanV.setEnabled(item.spanVerticalGet() > 1);
		decSpanV.addActionListener(al -> {
			item.spanVerticalSet(item.spanVerticalGet() - 1);
			grid.refresh();
		});

		JMenuItem toggleType = new JMenuItem("t".equals(item.typeGet())
				? I18N.getMsg("print.menu.type.photo")
				: I18N.getMsg("print.menu.type.text"));
		toggleType.addActionListener(al -> {
			// S'il y avait un élément présent, on le libère dans le Pool avant de changer de type
			releaseCellInPool();

			item.typeSet("t".equals(item.typeGet()) ? "p" : "t");
			item.clear();
			refresh();
			grid.setModified();
			grid.getPrint().poolGet().refresh(); // Rafraîchit le Pool pour appliquer les changements visuels
		});

		JMenuItem clearCell = new JMenuItem(I18N.getMsg("print.menu.clear"));
		clearCell.setEnabled(item.photoIdGet() != -1 || item.textIdGet() != -1 || !item.textGet().isEmpty());
		clearCell.addActionListener(al -> {
			// 1. Libérer l'élément d'origine dans le Pool (page passera à 0)
			releaseCellInPool();

			// 2. Vider la cellule locale de la Grille
			item.clear();
			refresh();
			grid.setModified();

			// 3. Forcer le Pool à se rafraîchir pour repasser la bordure au blanc
			grid.getPrint().poolGet().refresh();
		});

		menu.add(incSpanH);
		menu.add(decSpanH);
		menu.addSeparator();
		menu.add(incSpanV);
		menu.add(decSpanV);
		menu.addSeparator();
		menu.add(toggleType);
		menu.add(clearCell);
		menu.show(e.getComponent(), e.getX(), e.getY());
	}

	/**
	 * Recherche l'élément correspondant dans le Pool pour le remettre à la page 0
	 */
	private void releaseCellInPool() {
		Print print = grid.getPrint();
		if (print == null || print.getCells() == null) {
			return;
		}

		// Parcourir toutes les cellules du Pool
		for (PrintItem poolCell : print.getCells()) {
			if (this.item.isPhoto() && poolCell.isPhoto()) {
				if (this.item.photoIdGet() == poolCell.photoIdGet()) {
					poolCell.pageSet(0); // Libère l'élément
					break;
				}
			} else if (this.item.isText() && poolCell.isText()) {
				if (this.item.textIdGet() == poolCell.textIdGet()) {
					poolCell.pageSet(0); // Libère l'élément
					break;
				}
			}
		}
	}

}
