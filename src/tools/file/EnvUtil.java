/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun

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
package tools.file;

import app.Const;
import i18n.I18N;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;

/**
 * @author martin
 *
 * mod FaVdB
 *
 * Tools to get environement data file
 */
public class EnvUtil {

	public static File getTempDir() {
		return new File(System.getProperty("java.io.tmpdir"));
	}

	public static File getHomeDir() {
		return new File(System.getProperty("user.home"));
	}

	public static File getUserDir() {
		return new File(System.getProperty("user.dir"));
	}

	public static File getPrefDir() {
		return new File(System.getProperty("user.home") + File.separator
			+ "." + Const.getName());
	}

	public static File getIniFile() {
		return (new File(getPrefDir().getAbsolutePath() + File.separator + Const.getName() + ".ini"));
	}

	/**
	 * get the default Photos directory
	 *
	 * @return
	 */
	public static File getPhotosDir() {
		return (new File(getHomeDir().getAbsolutePath() + File.separator + "Photos"));
	}

	public static File getThumbDir() {
		return (new File(getPrefDir().getAbsolutePath() + File.separator + ".thumbnails"));
	}

	public static File getVracDir() {
		return (new File(getPhotosDir().getAbsolutePath() + File.separator + "0000-" + I18N.getMsg("photos.unsorted")));
	}

	public static void notAvailable() {
		JOptionPane.showMessageDialog(null,
			I18N.getMsg("not.available"),
			I18N.getMsg("error"),
			JOptionPane.ERROR_MESSAGE);
	}

	public static String getDateString(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return (formatter.format(date));
	}

	public static File getLogFile() {
		return new File(getHomeDir() + File.separator + Const.getName() + ".log");
	}

}
