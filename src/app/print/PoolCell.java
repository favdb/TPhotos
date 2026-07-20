package app.print;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author favdb
 */
public class PoolCell extends DefaultMutableTreeNode {

	private PrintCell poolCell;

	public PoolCell(PrintCell printCell) {
		// Crucial : transmet l'objet au système de nœuds du JTree
		super(printCell);
		this.poolCell = printCell;
	}

	public PrintCell printCellGet() {
		return poolCell;
	}

	public void printCellSet(PrintCell printCell) {
		this.poolCell = printCell;
		this.setUserObject(printCell);
	}

}
