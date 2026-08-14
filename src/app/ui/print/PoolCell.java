package app.ui.print;

import app.xml.XmlPrintCell;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author favdb
 */
public class PoolCell extends DefaultMutableTreeNode {

	private static final String TT = "PoolCell.";

	private XmlPrintCell poolCell;

	public PoolCell(XmlPrintCell printCell) {
		super(printCell);
		this.poolCell = printCell;
	}

	public XmlPrintCell printCellGet() {
		return poolCell;
	}

	public void printCellSet(XmlPrintCell printCell) {
		this.poolCell = printCell;
		this.setUserObject(printCell);
	}

}
