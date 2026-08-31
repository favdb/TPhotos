package app.zdlg;

import app.App;
import app.album.AlbumGalleryCell;
import static app.tools.ImageUtil.getImage;
import app.xml.XmlPrintCell;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;

/**
 * Visualiseur de photos avec navigation clavier en boucle.
 *
 * @author favdb
 */
public class ShowPhoto extends JDialog implements KeyListener {

	public static void show(File file, List<?> list) {
		ShowPhoto dlg = new ShowPhoto(file, list);
		dlg.setVisible(true);
	}

	private final List<?> list;
	private File file;
	private final JLabel label;
	private int currentIndex = -1;

	public ShowPhoto(File file, List<?> list) {
		super(App.mainFrame, true);
		this.file = file;
		this.list = list;

		this.label = new JLabel();
		this.setLayout(new BorderLayout());
		this.add(label, BorderLayout.CENTER);

		this.addKeyListener(this);
		this.setFocusable(true);

		initIndex();
		setFile(file);
	}

	private void initIndex() {
		if (list != null && file != null) {
			for (int i = 0; i < list.size(); i++) {
				Object item = list.get(i);
				File itemFile = getFileFromItem(item);
				if (itemFile != null && itemFile.equals(file)) {
					currentIndex = i;
					break;
				}
			}
		}
	}

	/**
	 * Extrait le File selon le type d'élément présent dans la liste.
	 */
	private File getFileFromItem(Object item) {
		if (item == null) {
			return null;
		}
		if (item instanceof File) {
			return (File) item;
		}
		if (item instanceof AlbumGalleryCell) {
			return ((AlbumGalleryCell) item).fileGet();
		}
		if (item instanceof XmlPrintCell) {
			XmlPrintCell xmlCell = (XmlPrintCell) item;
			if (xmlCell.isPhoto()) {
				return xmlCell.photoFileGet();
			}
		}
		return null;
	}

	private void setFile(File f) {
		if (f == null || !f.exists()) {
			return;
		}
		this.file = f;
		ImageIcon img = getImage(file,
				(int) Math.round(Toolkit.getDefaultToolkit().getScreenSize().height * 0.95),
				2);
		label.setIcon(img);
		pack();
		this.setLocationRelativeTo(App.mainFrame);
	}

	private void navigate(int direction) {
		if (list == null || list.isEmpty() || currentIndex == -1) {
			return;
		}

		int size = list.size();
		int step = direction > 0 ? 1 : -1;
		int nextIndex = (currentIndex + step + size) % size;

		// Parcourt la liste en boucle jusqu'à trouver une photo valide (ou faire un tour complet)
		while (nextIndex != currentIndex) {
			Object item = list.get(nextIndex);
			File nextFile = getFileFromItem(item);
			if (nextFile != null && nextFile.exists()) {
				currentIndex = nextIndex;
				setFile(nextFile);
				break;
			}
			nextIndex = (nextIndex + step + size) % size;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				dispose();
				break;
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_PAGE_DOWN:
				navigate(1);
				break;
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_UP:
			case KeyEvent.VK_PAGE_UP:
				navigate(-1);
				break;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// Inutilisé
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// Inutilisé
	}
}
