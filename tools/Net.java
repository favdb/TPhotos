/*
Storybook: Scene-based software for novelists and authors.
Copyright (C) 2008 - 2011 Martin Mustun

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tools;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;

/**
 * network utilities
 *
 * @author favdb
 */
public class Net {

	private static final String TT = "Net";

	public enum KEY {
		ROOT("http://pas66430.free.fr/TPhotos"),
		OSM("http://www.openstreetmap.org/"),
		DO_UPDATE("false");
		private final String text;

		private KEY(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	/**
	 * open default browser
	 *
	 * @param path
	 */
	public static void openBrowser(String path) {
		//LOG.trace(TT + ".openBrowser(" + path + ")");
		try {
			Desktop.getDesktop().browse(new URI(path));
		} catch (URISyntaxException | IOException e) {
			LOG.err(TT + ".openBrowser(" + path + ")", e);
		}
	}

	/**
	 * open an URL, may be a local file
	 *
	 * @param evt
	 */
	public static void openUrl(HyperlinkEvent evt) {
		//LOG.trace(TT + "openUrl(evt description=\"" + evt.getDescription() + "\")");
		String url = evt.getDescription();
		try {
			if (url.toLowerCase().startsWith("http")) {
				//App.trace("open browser");
				Desktop.getDesktop().browse(new URI(url));
			} else if (url.startsWith("#")) {
				if (evt.getSource() instanceof JEditorPane) {
					JEditorPane ep = (JEditorPane) evt.getSource();
					ep.scrollToReference(url.substring(1));
				}
			} else if (url.startsWith("file")) {
				openFile(url);
			} else {
				Desktop.getDesktop().browse(new URI(url));
			}
		} catch (URISyntaxException ex) {
			LOG.err(TT + ".openUrl URI exception ", ex);
		} catch (IOException ex) {
			//empty
		}
	}

	private static void openFile(String url) {
		//LOG.trace(TT+.openFile(url=\"" + url + "\")");
		File file = new File(url.replace("file://", ""));
		if (file.exists()) {
			try {
				Desktop.getDesktop().edit(file);
			} catch (UnsupportedOperationException ex) {
				try {
					Desktop.getDesktop().open(file);
				} catch (IOException ex1) {
					LOG.err(TT + "file exception for " + url);
				}
			} catch (IOException ex) {
				LOG.err(TT + "file exception for " + url);
			}
		} else {
			LOG.err(TT + "warning file not exists: " + file.getAbsolutePath());
		}
	}

}
