package app.zdlg;

import app.App;
import app.album.AlbumGalleryCell;
import app.media.Media;
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
 * Photos view with navigation.
 *
 * @author favdb
 */
public class ShowPhoto extends JDialog implements KeyListener {

	public static void show(File file, List<?> list) {
		if (!Media.jpegIs(file)) {
			return;
		}
		ShowPhoto dlg = new ShowPhoto(file, list);
		dlg.setVisible(true);
	}

	private final List<?> list;
	private File file;
	private final JLabel label;
	private int currentIndex = -1;

	@SuppressWarnings("LeakingThisInConstructor")
	public ShowPhoto(File file, List<?> list) {
		super(App.mainFrame, true);
		this.file = file;
		this.list = list;
		this.label = new JLabel();
		this.setLayout(new BorderLayout());
		this.add(label, BorderLayout.CENTER);
		this.addKeyListener(this);
		this.setFocusable(true);
		indexInit();
		fileSet(file);
	}

	/**
	 * initialize index
	 */
	private void indexInit() {
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
	 * Get the File for the elements of list.
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

	/**
	 * set the File
	 *
	 * @param f
	 */
	private void fileSet(File f) {
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

	/**
	 * navigate thru the list
	 *
	 * @param direction
	 */
	private void navigate(int direction) {
		if (list == null || list.isEmpty() || currentIndex == -1) {
			return;
		}
		int size = list.size();
		int step = direction > 0 ? 1 : -1;
		int nextIndex = (currentIndex + step + size) % size;
		while (nextIndex != currentIndex) {
			Object item = list.get(nextIndex);
			File nextFile = getFileFromItem(item);
			if (nextFile != null && nextFile.exists()) {
				currentIndex = nextIndex;
				fileSet(nextFile);
				break;
			}
			nextIndex = (nextIndex + step + size) % size;
		}
	}

	/**
	 * key pressed action
	 *
	 * @param e
	 */
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

	/**
	 * key typed action
	 *
	 * @param e
	 */
	@Override
	public void keyTyped(KeyEvent e) {
		// Inutilisé
	}

	/**
	 * key released action
	 *
	 * @param e
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		// Inutilisé
	}
}
