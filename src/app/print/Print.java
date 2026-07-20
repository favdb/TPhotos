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

import api.mig.MIG;
import api.mig.swing.MigLayout;
import app.ui.MainFrame;
import app.ui.SHEFDialog;
import app.xml.Xml;
import app.xml.XmlPrint;
import app.xml.XmlPrintPage;
import i18n.I18N;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import resources.icons.ICONS;
import tools.Ui;

/**
 * Configuration interface for the print layout.
 *
 * @author favdb
 */
public class Print extends JPanel {

	private static final String TT = "Print.";
	public final static Border BORDER_NORMAL = BorderFactory.createLineBorder(Color.WHITE, 2),
			BORDER_ALLOW = BorderFactory.createLineBorder(Color.GREEN, 2),
			BORDER_SELECTED = BorderFactory.createLineBorder(Color.RED, 2);

	private final MainFrame mainFrame;
	private Grid pGrid;
	private Pool pPool;
	private JComboBox cbOrientation;
	private JLabel lbPage;
	private JButton btPagePrev, btPageNext;
	private boolean isPortrait = true;
	private int currentPage = 1, totalPages = 1;
	private final Xml xml;
	public XmlPrint xmlPrint;
	List<XmlPrintPage> pages = new ArrayList<>();
	List<PrintCell> cells = new ArrayList<>();

	public Print(MainFrame mainFrame) {
		super();
		this.mainFrame = mainFrame;
		this.xml = mainFrame.albumGet().xmlGet();
		setName(I18N.getMsg(TT + "panel"));
		initialize();
	}

	public MainFrame getMainFrame() {
		return mainFrame;
	}

	/**
	 * initialize this JFrame
	 */
	private void initialize() {
		//LOG.trace(TT + "initialize()");
		xmlPrint = xml.printGet();
		pages = xmlPrint.getPages();
		if (pages.size() < 1) {
			pages.add(new XmlPrintPage(xml, "1"));
		}
		totalPages = pages.size();
		cells = xmlPrint.getCells();

		this.setLayout(new MigLayout(MIG.get(MIG.FILL), "[256px][]"));
		add(poolInit(), MIG.get(MIG.TOP, MIG.GROWY));
		add(gridInit(), MIG.get(MIG.SPAN, MIG.GROW));
		add(bottomPanelInit(), MIG.get(MIG.SPAN, MIG.RIGHT));
		refresh();
	}

	public List<PrintCell> getCells() {
		return cells;
	}

	public PrintCell printCellFind(int i) {
		for (PrintCell p : cells) {
			if (p.cellNumGet() == i) {
				return p;
			}
		}
		return new PrintCell();
	}

	//******************************//
	//** manage the Pool          **//
	//******************************//
	public Pool poolGet() {
		return pPool;
	}

	/**
	 * initialize the Pool
	 *
	 * @return
	 */
	private JScrollPane poolInit() {
		//LOG.trace(TT + "poolInit()");
		pPool = new Pool(this);
		JScrollPane scroller = new JScrollPane(pPool);
		scroller.setBorder(BorderFactory.createTitledBorder(I18N.getMsg("print.pool")));
		scroller.setMinimumSize(new Dimension(256, 256));
		return scroller;
	}

	/**
	 * Refresh the photos pool
	 */
	private void poolRefresh() {
		pPool.refresh();
	}

	//******************************//
	//** manage the Grid          **//
	//******************************//
	/**
	 * get the current page of the grid
	 *
	 * @return
	 */
	public int gridCurrentPageGet() {
		return this.currentPage;
	}

	/**
	 * get the grid panel
	 *
	 * @return
	 */
	public Grid gridGet() {
		return pGrid;
	}

	/**
	 * initialize the grid
	 *
	 * @return
	 */
	private JPanel gridInit() {
		//LOG.trace(TT + "gridInit()");
		pGrid = new Grid(this);
		JPanel panel = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.INS1, MIG.GAP1)));
		panel.setBorder(BorderFactory.createTitledBorder(I18N.getMsg("print.page")));
		panel.add(gridTopInit(), MIG.get(MIG.SPAN, MIG.GROWX));
		JScrollPane scroll = new JScrollPane(pGrid);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		scroll.getHorizontalScrollBar().setUnitIncrement(16);
		scroll.setPreferredSize(new Dimension(1920, 1920));
		panel.add(scroll, MIG.get(MIG.SPAN, MIG.GROW));
		return panel;
	}

	/**
	 * Add all Photos to the current Grid
	 */
	private void gridAddAll() {
		if (pGrid == null) {
			return;
		}
		int totalRows = pGrid.rowsGet();
		int totalCols = pGrid.colsGet();
		int maxCells = totalRows * totalCols;
		boolean[] occupied = new boolean[maxCells + 1];
		int indexPage = currentPage - 1;
		if (indexPage >= 0 && indexPage < pages.size()) {
			for (PrintCell cell : cells) {
				if (cell.pageGet() == currentPage) {
					int cellNum = cell.cellNumGet();
					int sH = cell.spanHorizontalGet();
					int sV = cell.spanVerticalGet();
					int startRow = (cellNum - 1) / totalCols;
					int startCol = (cellNum - 1) % totalCols;
					for (int r = 0; r < sV && (startRow + r) < totalRows; r++) {
						for (int c = 0; c < sH && (startCol + c) < totalCols; c++) {
							int slot = ((startRow + r) * totalCols) + (startCol + c) + 1;
							if (slot <= maxCells) {
								occupied[slot] = true;
							}
						}
					}
				}
			}
		}
		int currentSlot = 1;
		boolean modified = false;
		for (PrintCell cell : cells) {
			if (cell.isPhoto() && cell.pageGet() == 0) {
				while (currentSlot <= maxCells && occupied[currentSlot]) {
					currentSlot++;
				}
				if (currentSlot > maxCells) {
					break;
				}
				cell.pageSet(currentPage);
				cell.cellNumSet(currentSlot);
				cell.spanHorizontalSet(1);
				cell.spanVerticalSet(1);
				occupied[currentSlot] = true;
				modified = true;
			}
		}
		if (modified) {
			actionSave();
		}
	}

	/**
	 * Build the top panel of the grid (printer config, page nav)
	 */
	@SuppressWarnings("unchecked")
	private JPanel gridTopInit() {
		//LOG.trace(TT + "gridTopInit()");
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.WRAP, "ins 5"), "[][][][][]"));
		p.setBorder(BorderFactory.createEtchedBorder());
		String array[] = {I18N.getMsg("print.orientation_portrait"),
			I18N.getMsg("print.orientation_landscape")};
		cbOrientation = new JComboBox(array);
		cbOrientation.addItemListener(s -> {
			this.gridOrientationChange();
		});
		p.add(cbOrientation);
		p.add(Ui.initIconButton("btRefresh", ICONS.K.REFRESH, "print.refresh", e -> refresh()));
		p.add(Ui.initIconButton("btAddAll", ICONS.K.AR_RIGHT, "print.add_all", e -> gridAddAll()));
		JPanel pNav = new JPanel(new MigLayout("ins 0, alignx right"));
		btPagePrev = Ui.initIconButton("btPagePrev", ICONS.K.NAV_PREV, e -> GridNavigation(-1));
		pNav.add(btPagePrev);
		lbPage = new JLabel(I18N.getMsg("print.page") + " 1 / 1");
		pNav.add(lbPage, "gapx 1 1");
		btPageNext = Ui.initIconButton("btPageNext", ICONS.K.NAV_NEXT, e -> GridNavigation(1));
		pNav.add(btPageNext);
		pNav.add(Ui.initIconButton("btPageAdd", ICONS.K.PLUS, "print.page_add", e -> gridPageAdd()));

		p.add(pNav, MIG.RIGHT);

		return p;
	}

	/**
	 * add a page to the grid
	 */
	private void gridPageAdd() {
		//LOG.trace(TT + "gridPageAdd()");
		pages.add(new XmlPrintPage(xml, String.valueOf(pages.size() + 1)));
		totalPages = pages.size();
		refresh();
	}

	/**
	 * Change the orientation (portrait or landscape)
	 */
	private void gridOrientationChange() {
		int str = cbOrientation.getSelectedIndex();
		isPortrait = (str != 1);
		String sorient = (isPortrait ? "portrait" : "landscape");
		int rows = (isPortrait ? 5 : 3), cols = (isPortrait ? 3 : 5);
		pGrid.orientationSet(rows, cols);
		xml.printGet().orientationSet(sorient);
		gridGet().setDim("A4", sorient);
		gridRefresh();
	}

	/**
	 * Initialize the bottom panel (preview, actionSave and exit buttons)
	 */
	private JPanel bottomPanelInit() {
		//LOG.trace(TT + "bottomPanelInit()");
		JPanel p = new JPanel(new MigLayout("ins 5, alignx right"));
		p.add(Ui.initButton("print.action_preview", ICONS.K.PREVIEW, e -> actionPreview()));
		p.add(Ui.initButton("print.action_exit", ICONS.K.EXIT, e -> actionClose()));
		return p;
	}

	/**
	 * refresh this Print
	 */
	public void refresh() {
		//LOG.trace(TT + "refresh()");
		pPool.refresh();
		pGrid.refresh();
		refreshButtons();
	}

	/**
	 * Reinit action for the grid and the pool
	 */
	private void actionReinit() {
		int rows = isPortrait ? 5 : 3;
		int cols = isPortrait ? 3 : 5;
		int cellsPerPage = rows * cols;
		int row_count = mainFrame.albumGet().getTable().getRowCount();
		if (row_count == 0) {
			totalPages = 1;
		} else {
			totalPages = (int) Math.ceil((double) row_count / cellsPerPage);
		}
		currentPage = 1;
		refresh();
	}

	/**
	 * Refresh the status of the buttons
	 */
	private void refreshButtons() {
		lbPage.setText(String.format("%s %d / %d", I18N.getMsg("print.page"), currentPage, totalPages));
		btPagePrev.setEnabled(currentPage > 1);
		btPageNext.setEnabled(currentPage < totalPages);
	}

	/**
	 * Navigation panel
	 *
	 * @param direction
	 */
	private void GridNavigation(int direction) {
		currentPage += direction;
		if (currentPage < 1) {
			currentPage = 1;
		}
		if (currentPage > totalPages) {
			currentPage = totalPages;
		}
		refresh();
	}

	/**
	 * Refresh the grid panel
	 */
	private void gridRefresh() {
		//LOG.trace(TT + "gridRefresh()");
		pGrid.refresh();
	}

	/**
	 * action for previewing in default browser as a HTML
	 */
	private void actionPreview() {
		File fx = mainFrame.albumGet().fileGet();
		File dirDest = fx.getParentFile();
		File outfile = new File(dirDest, fx.getName().replace(".xml", "") + "_print.html");
		BuilderHtml.generateHTML(this, outfile, true);
	}

	/**
	 * action for close (return to the default album panel)
	 */
	private void actionClose() {
		mainFrame.printHide();
	}

	/**
	 * get the PintPage list
	 *
	 * @return
	 */
	public List<XmlPrintPage> printPagesGet() {
		return pages;
	}

	/**
	 * get Xml
	 *
	 * @return
	 */
	public Xml xmlGet() {
		return xml;
	}

	/**
	 * get the XmlPrint
	 *
	 * @return
	 */
	public XmlPrint xmlPrintGet() {
		return xmlPrint;
	}

	/**
	 * Save Print data
	 */
	public void actionSave() {
		//LOG.trace(TT + "actionSave()");
		PrintCell.sortByPage(cells);
		xml.save();
	}

//***************************************************
// Manage interaction between Pool and Grid
//***************************************************
	private PrintCell pendingCellToPlace = null;

	/**
	 * Define the Pool cell waiting for placement on the Grid
	 *
	 * @param cell
	 */
	public void pendingCellToPlaceSet(PrintCell cell) {
		this.pendingCellToPlace = cell;
	}

	/**
	 * Get the waiting cell to be placed
	 *
	 * @return
	 */
	public PrintCell pendingCellToPlaceGet() {
		return this.pendingCellToPlace;
	}

	/**
	 * Reinit current selection
	 */
	public void pendingCellClear() {
		pendingCellToPlace.pageSet(0);
		xml.save();
		this.pendingCellToPlace = null;
		refresh();
	}

	public void shefEdit(PrintCell item) {
		SHEFDialog dlg = new SHEFDialog(mainFrame, item.textGet());
		if (dlg.isSaved()) {
			item.textSet(dlg.getHtmlContent());
			xml.save();
			refresh();
		}
	}

	public void updateCell(PrintCell dest, int pageGet, String posGet) {
		xmlPrint.updateCell(dest, pageGet, posGet);
		xml.save();
		refresh();
	}

	public void swapCell(PrintCell srce, PrintCell dest) {
		int sPage = srce.pageGet();
		String sPos = srce.posGet();
		srce.pageSet(dest.pageGet());
		srce.posSet(dest.posGet());
		dest.pageSet(sPage);
		dest.posSet(sPos);
		xml.save();
		pGrid.refresh();
	}
}
