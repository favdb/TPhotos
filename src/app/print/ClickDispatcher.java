package app.print;

import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Reusable single-click / double-click dispatcher.
 * <p>
 * Swing has no native "single click" event: a single click must be delayed
 * (here 250ms) to see whether a second click follows and turns it into a
 * double click. This exact pattern was duplicated identically in
 * {@link GridCell} and {@link Pool}; it's now centralized here.
 *
 * @author favdb
 */
public class ClickDispatcher {

	private final Timer timer;
	private Runnable onSingleClick;

	/**
	 * @param delayMs delay in ms used to detect a single click (250 is the
	 * value used across this module)
	 */
	public ClickDispatcher(int delayMs) {
		this.timer = new Timer(delayMs, e -> {
			if (onSingleClick != null) {
				onSingleClick.run();
			}
		});
		this.timer.setRepeats(false);
	}

	/**
	 * Feed a mouseClicked event to the dispatcher.
	 *
	 * @param e the mouse event
	 * @param onSingleClick action to run if the click resolves to a single
	 * click (armed now, fired after the delay if no second click occurs)
	 * @param onDoubleClick action to run immediately on a confirmed double
	 * click
	 */
	public void dispatch(MouseEvent e, Runnable onSingleClick, Runnable onDoubleClick) {
		if (!SwingUtilities.isLeftMouseButton(e)) {
			return;
		}
		switch (e.getClickCount()) {
			case 1:
				this.onSingleClick = onSingleClick;
				timer.restart();
				break;
			case 2:
				timer.stop();
				if (onDoubleClick != null) {
					onDoubleClick.run();
				}
				break;
			default:
				break;
		}
	}

	/**
	 * Cancel any pending single-click callback (e.g. on popup trigger).
	 */
	public void cancelPending() {
		timer.stop();
	}

}
