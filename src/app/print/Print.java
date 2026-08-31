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
import app.MainFrame;
import app.i18n.I18N;
import app.resources.icons.ICONS;
import app.tools.Html;
import app.tools.LOG;
import app.tools.Ui;
import app.xml.Xml;
import app.xml.XmlPrint;
import app.xml.XmlPrintCell;
import app.xml.XmlPrintPage;
import app.zdlg.SherpaDlg;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

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

	public static String PORTRAIT = "portrait", LANDSCAPE = "landscape";

	/**
	 * Number of grid rows/cols for each orientation. Single source of truth: previously
	 * this 5x3 / 3x5 layout was hard-coded independently in Grid.setDim(),
	 * Print.actionReinit() and Print.paperOrientationChange().
	 */
	public static final int GRID_ROWS_PORTRAIT = 5, GRID_COLS_PORTRAIT = 3;

	public static int gridRowsFor(String orientation) {
		return PORTRAIT.equalsIgnoreCase(orientation) ? GRID_ROWS_PORTRAIT : GRID_COLS_PORTRAIT;
	}

	public static int gridColsFor(String orientation) {
		return PORTRAIT.equalsIgnoreCase(orientation) ? GRID_COLS_PORTRAIT : GRID_ROWS_PORTRAIT;
	}

	private final MainFrame mainFrame;
	private Grid pGrid;
	private Pool pPool;
	private JComboBox cbOrientation;
	private JLabel lbPage;
	private JButton btPagePrev, btPageNext;
	private boolean isPortrait = true;
	private int currentPage = 1, totalPages = 1;
	private final Xml xml;
	private XmlPrint xmlPrint;
	private List<XmlPrintPage> pages = new ArrayList<>();
	private List<XmlPrintCell> cells = new ArrayList<>();
	private JComboBox cbFormat;
	private JCheckBox ckPage;
	private JButton btPageRemove;

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
	 * initialize this JPanel
	 */
	private void initialize() {
		//LOG.trace(TT + "initialize()");
		xmlPrint = xml.printGet();
		pages = xmlPrint.getPages();
		if (pages.size() < 1) {
			pages.add(new XmlPrintPage(xml, "1"));
		}
		totalPages = pages.size();
		cells.clear();
		cells = xmlPrint.getCells();
		this.setLayout(new MigLayout(MIG.get(MIG.FILL), "[256px][]"));
		add(poolInit(), MIG.get(MIG.TOP, MIG.GROWY));
		add(gridInit(), MIG.get(MIG.SPAN, MIG.GROW));
		add(bottomInit(), MIG.get(MIG.SPAN, MIG.RIGHT));
		refresh();
	}

	public List<XmlPrintCell> getCells() {
		return cells;
	}

	public XmlPrintCell printCellFind(int i) {
		for (XmlPrintCell p : cells) {
			if (p.cellNumGet() == i) {
				return p;
			}
		}
		return new XmlPrintCell();
	}

//***************************************************
//** manage the Pool
//***************************************************
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
		//LOG.trace(TT + "poolRefresh()");
		pPool.refresh();
	}

//***************************************************
//** manage the Grid
//***************************************************
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
		JPanel panel = new JPanel(new MigLayout(MIG.get(MIG.FILL, MIG.INS1, MIG.GAP1)));
		panel.setBorder(BorderFactory.createTitledBorder(I18N.getMsg("print.page")));
		panel.add(gridTopInit(), MIG.get(MIG.SPAN, MIG.GROWX));
		JScrollPane scroll = new JScrollPane(pGrid = new Grid(this));
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
			for (XmlPrintCell cell : cells) {
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
		for (XmlPrintCell cell : cells) {
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
			refresh();
		}
	}

	private void gridClearAll() {
		for (XmlPrintCell cell : cells) {
			cell.pageSet(0);
		}
		xml.save();
		refresh();
	}

	/**
	 * Build the top panel of the grid (printer config, page nav, options)
	 */
	@SuppressWarnings("unchecked")
	private JPanel gridTopInit() {
		//LOG.trace(TT + "gridTopInit()");
		JPanel p = new JPanel(new MigLayout(MIG.get(MIG.WRAP, "ins 5"), "[][][][][][]"));
		p.setBorder(BorderFactory.createEtchedBorder());
		String orFmt[] = {"A4", "A3"};
		cbFormat = new JComboBox(orFmt);
		cbFormat.setSelectedItem(xmlPrint.formatGet());
		cbFormat.addItemListener(s -> {
			xmlPrint.formatSet(paperFormatGet());
			xml.save();
			gridGet().setDim(paperFormatGet(), paperOrientationGet());
			gridRefresh();
		});
		//p.addLib(cbFormat);

		String orList[] = {I18N.getMsg("print.orientation_portrait"),
			I18N.getMsg("print.orientation_landscape")};
		cbOrientation = new JComboBox(orList);
		cbOrientation.setSelectedIndex((xmlPrint.isPortrait() ? 0 : 1));
		cbOrientation.addItemListener(s -> {
			this.paperOrientationChange();
		});
		p.add(cbOrientation);
		p.add(Ui.initIconButton("btRefresh", ICONS.K.REFRESH, "print.refresh", e -> refresh()));
		p.add(Ui.initIconButton("btAddAll", ICONS.K.AR_RIGHT, "print.add_all", e -> gridAddAll()));
		p.add(Ui.initIconButton("btRemoveAll", ICONS.K.CANCEL, "print.clear_all",
				e -> gridClearAll()));

		JPanel pNav = new JPanel(new MigLayout(MIG.get(MIG.INS0, MIG.RIGHT)));
		btPagePrev = Ui.initIconButton("btPagePrev", ICONS.K.NAV_PREV, e -> gridNavigation(-1));
		pNav.add(btPagePrev);
		lbPage = new JLabel(I18N.getMsg("print.page") + " 1 / 1");
		pNav.add(lbPage, "gapx 1 1");
		btPageNext = Ui.initIconButton("btPageNext", ICONS.K.NAV_NEXT, e -> gridNavigation(1));
		pNav.add(btPageNext);
		// add and remove button
		pNav.add(Ui.initIconButton("btPageAdd", ICONS.K.PLUS, "print.page_add", e -> gridPageAdd()));
		btPageRemove = Ui.initIconButton("btPageRemove", ICONS.K.MINUS,
				"print.page_remove", e -> gridPageRemove());
		pNav.add(btPageRemove);

		p.add(pNav, MIG.get(MIG.SPAN, MIG.RIGHT));

		return p;
	}

	private void changePage() {
		xmlPrint.numpageSet(ckPage.isSelected());
		xml.save();
	}

	public boolean pagenumGet() {
		return xmlPrint.numpageGet();
	}

	/**
	 * addLib a page to the grid
	 */
	private void gridPageAdd() {
		//LOG.trace(TT + "gridPageAdd()");
		pages.add(new XmlPrintPage(xml, String.valueOf(pages.size() + 1)));
		totalPages = pages.size();
		this.currentPage = totalPages;
		refresh();
	}

	public String paperFormatGet() {
		return (String) cbFormat.getSelectedItem();
	}

	public String paperOrientationGet() {
		return (cbOrientation.getSelectedIndex() == 0 ? PORTRAIT : LANDSCAPE);
	}

	/**
	 * Change the orientation (PORTRAIT or LANDSCAPE)
	 */
	private void paperOrientationChange() {
		int str = cbOrientation.getSelectedIndex();
		isPortrait = (str == 0);
		String sorient = (isPortrait ? PORTRAIT : LANDSCAPE);
		int rows = gridRowsFor(sorient), cols = gridColsFor(sorient);
		pGrid.orientationSet(rows, cols);
		xmlPrint.orientationSet(sorient);
		gridClearAll();
		xml.save();
		gridGet().setDim(paperFormatGet(), sorient);
		gridRefresh();
	}

	/**
	 * Initialize the bottom panel (preview, print and exit buttons)
	 */
	private JPanel bottomInit() {
		//LOG.trace(TT + "bottomPanelInit()");
		JPanel p = new JPanel(new MigLayout("ins 5, alignx right"));
		p.add(Ui.initButton("print.action_preview", ICONS.K.PREVIEW,
				e -> actionPreview()));
		p.add(Ui.initButton("print.action_print", ICONS.K.F_PRINT,
				e -> Printer.executePrint(this)));
		/*p.addLib(Ui.initButton("print.action_close", ICONS.K.EXIT,
				e -> actionClose()));*/
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
	 * Refresh the status of the buttons
	 */
	public void refreshButtons() {
		lbPage.setText(String.format("%s %d / %d",
				I18N.getMsg("print.page"), currentPage, totalPages));
		btPagePrev.setEnabled(currentPage > 1);
		btPageNext.setEnabled(currentPage < totalPages);
		boolean canRemove = (currentPage > 1) && !isPageOccupied(currentPage);
		if (btPageRemove != null) {
			btPageRemove.setEnabled(canRemove);
		}
	}

	/**
	 * Navigation panel
	 *
	 * @param direction
	 */
	private void gridNavigation(int direction) {
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
		refreshButtons();
	}

	/**
	 * get the PrintPage list
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

//***************************************************
//** main actions
//***************************************************
	/**
	 * action for previewing in default browser as a HTML
	 */
	private void actionPreview() {
		File fx = mainFrame.albumGet().fileGet();
		File dirDest = fx.getParentFile();
		File outfile = new File(dirDest,
				fx.getName().replace(".xml", "") + "_print.html");
		BuilderHtml.generateHTML(this, outfile, true);
	}

	/**
	 * action for close (return to the default album panel). Currently unused: the close
	 * button is commented out in bottomInit(). Kept for when that button is reinstated.
	 */
	private void actionClose() {
		mainFrame.printHide();
	}

	/**
	 * action for saving the Print data
	 */
	public void actionSave() {
		//LOG.trace(TT + "actionSave()");
		XmlPrintCell.sortByPage(cells);
		xml.save();
	}

	/**
	 * update the given XmlPrintCell
	 *
	 * @param dest
	 * @param pageGet
	 * @param posGet
	 */
	public void updateCell(XmlPrintCell dest, int pageGet, String posGet) {
		xmlPrint.updateCell(dest, pageGet, posGet);
		xml.save();
		refresh();
	}

	/**
	 * swap the two given XmlPrintCell
	 *
	 * @param srce
	 * @param dest
	 */
	public void swapCell(XmlPrintCell srce, XmlPrintCell dest) {
		int sPage = srce.pageGet();
		String sPos = srce.posGet();
		srce.pageSet(dest.pageGet());
		srce.posSet(dest.posGet());
		dest.pageSet(sPage);
		dest.posSet(sPos);
		xml.save();
		pGrid.refresh();
	}

	/**
	 * SHERPA editor to edit the given XmlPrintCell text
	 *
	 * @param item
	 */
	public void textEdit(XmlPrintCell item) {
		SherpaDlg dlg = new SherpaDlg(mainFrame, item.textGet());
		if (dlg.isValidate()) {
			String txt = dlg.getHtmlContent();
			LOG.trace(TT + "textEdit(item) validated ='" + txt + "'");
			if (Html.htmlToText(txt).isEmpty()) {
				return;
			}
			item.textSet(txt);
			xml.libsGet().libUpdate(item.textIdGet(), txt);
			xml.save();
			refresh();
		}
	}

	/**
	 * SHERPA editor to create a new text
	 *
	 * @param item
	 */
	public void textCreate(XmlPrintCell item) {
		SherpaDlg dlg = new SherpaDlg(mainFrame, "", "print.text_create");
		if (dlg.isValidate()) {
			String txt = dlg.getHtmlContent();
			if (Html.htmlToText(txt).isEmpty()) {
				return;
			}
			xml.libsGet().addLib(txt);
			XmlPrintCell cell = new XmlPrintCell(xml.libsGet().getAll().size() - 1,
					dlg.getHtmlContent(),
					item.pageGet(),
					item.posGet());
			xmlPrint.addCell(cell);
			xml.save();
			refresh();
		}
	}

//***************************************************
// Manage interaction between Pool and Grid
//***************************************************
	private XmlPrintCell pendingCellToPlace = null;

	/**
	 * Define the Pool cell waiting for placement on the Grid
	 *
	 * @param cell
	 */
	public void pendingCellToPlaceSet(XmlPrintCell cell) {
		this.pendingCellToPlace = cell;
	}

	/**
	 * Get the waiting cell to be placed
	 *
	 * @return
	 */
	public XmlPrintCell pendingCellToPlaceGet() {
		return this.pendingCellToPlace;
	}

	/**
	 * Reinit current selection
	 */
	public void pendingCellClear() {
		if (pendingCellToPlace != null) {
			pendingCellToPlace.pageSet(0);
			xml.save();
		}
		this.pendingCellToPlace = null;
		refresh();
	}

	/**
	 * Supprime la page courante si elle est vide et renumérote les pages/cellules
	 * suivantes.
	 */
	private void gridPageRemove() {
		if (currentPage <= 1 || isPageOccupied(currentPage)) {
			return;
		}
		int pageToRemove = currentPage;
		pages.remove(pageToRemove - 1);
		totalPages = pages.size();
		for (XmlPrintCell cell : cells) {
			if (cell.pageGet() > pageToRemove) {
				cell.pageSet(cell.pageGet() - 1);
			}
		}
		for (int i = 0; i < pages.size(); i++) {
			pages.get(i).idSet(String.valueOf(i + 1));
		}
		if (currentPage > totalPages) {
			currentPage = totalPages;
		}
		xml.save();
		refresh();
	}

	/**
	 * Vérifie si la page passée en paramètre contient au moins une cellule assignée.
	 */
	private boolean isPageOccupied(int pageNum) {
		for (XmlPrintCell cell : cells) {
			if (cell.pageGet() == pageNum) {
				return true;
			}
		}
		return false;
	}

}
