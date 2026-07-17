/*
 * Copyright (C) 2024 favdb
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
package app;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import tools.LOG;
import tools.file.EnvUtil;

/**
 *
 * @author favdb
 */
public class Pref {

	private static final String TT = "Pref.";

	private Integer exportType;

	public enum KEY {
		ALBUM_LAST("AlbumLast", "Album.xml"),
		ASK_DELETE("AskDelete", "1"),
		EXPORT_COMPRESS("ExposrtCompress", "0"),
		EXPORT_FORMAT("ExportFormat", "0"),
		EXPORT_LAST("ExportLast", ""),
		FONT_SIZE("FontSize", Const.FONT_SIZE.toString()),
		ICON_SIZE("IconSize", "0"),
		ICON_SCREEN("IconScreen", "0"),
		LAFDARK("LafDark", "0"),
		ORGANIZE_TYPE("OrganizeType", "0"),
		ORGANIZE_DELETE("OrganizeDelete", "0"),
		PHOTOS_DIR("PhotosDir", EnvUtil.getPhotosDir().getAbsolutePath()),
		VERSION("Version", Const.getVersion()),
		IMAGE_LATSDIR("ImageLastDir", "");
		final private String text, value;

		private KEY(String text, String value) {
			this.text = text;
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public Integer getInteger() {
			return (Integer.valueOf(value));
		}

		public boolean getBoolean() {
			return (value.equals("1") || value.equals("true"));
		}

		@Override
		public String toString() {
			return text;
		}
	}

	List<PrefValue> preferences = new ArrayList<>();
	private final String prefFile = EnvUtil.getIniFile().getAbsolutePath();

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Pref() {
		load();
		String av = get(KEY.VERSION.toString(), KEY.VERSION.getValue());
		String cv = KEY.VERSION.getValue();
		if (!av.equals(cv)) {
			setString(KEY.VERSION.toString(), cv);
			LOG.log("Set Preferences version to " + cv);
		}
	}

	/**
	 * check if a preference loaded
	 *
	 * @param key: the preference key
	 *
	 * @return : true if it is loaded
	 */
	private boolean isToLoad(String key) {
		//LOG.trace(TT + "isLoad(key=\"" + key + "\")");
		for (KEY k : KEY.values()) {
			if (k.toString().equals(key)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * load preferences
	 *
	 */
	public void load() {
		File file = new File(prefFile);
		if (!file.exists()) {
			create(file);
			return;
		}
		LOG.trace("Load Preferences from " + file.getAbsolutePath());
		try {
			InputStream ips = new FileInputStream(prefFile);
			InputStreamReader ipsr = new InputStreamReader(ips);
			try (BufferedReader br = new BufferedReader(ipsr)) {
				String ligne;
				while ((ligne = br.readLine()) != null) {
					String[] s = ligne.split("=");
					if (s.length < 2) {
						preferences.add(new PrefValue(s[0], ""));
					} else {
						if (isToLoad(s[0])) {
							preferences.add(new PrefValue(s[0], s[1]));
						}
					}
				}
				br.close();
			} catch (Exception e) {
				LOG.err(TT + "load() err", e);
			}
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	/**
	 * set the last used Album (file name only)
	 *
	 * @param value
	 */
	public void albumLastSet(String value) {
		setString(KEY.ALBUM_LAST, value);
		save();
	}

	/**
	 * get the last used Album (file name only)
	 *
	 * @return
	 */
	public String albumLastGet() {
		return getString(KEY.ALBUM_LAST);
	}

	/**
	 * get the Photos directory
	 *
	 * @return
	 */
	public String photosDirGet() {
		return getString(KEY.PHOTOS_DIR);
	}

	/**
	 * set the Photos directory
	 *
	 * @param value
	 */
	public void photosDirSet(String value) {
		//LOG.trace("Pref.photosDirSet(value=" + value + ")");
		setString(KEY.PHOTOS_DIR, value);
		save();
	}

	public void exportLastSet(String value) {
		setString(KEY.EXPORT_LAST, value);
		save();
	}

	public String exportLastGet() {
		return getString(KEY.EXPORT_LAST);
	}

	public void exportCompressSet(Integer value) {
		setString(KEY.EXPORT_COMPRESS, value.toString());
		save();
	}

	public Integer exportCompressGet() {
		return getInteger(KEY.EXPORT_COMPRESS);
	}

	public void exportFormatSet(Integer value) {
		setString(KEY.EXPORT_FORMAT, value.toString());
		save();
	}

	public Integer exportFormatGet() {
		return getInteger(KEY.EXPORT_FORMAT);
	}

	public void organizeDeleteSet(boolean value) {
		setBoolean(KEY.ORGANIZE_DELETE, value);
		save();
	}

	public boolean organizeDeleteGet() {
		return getBoolean(KEY.ORGANIZE_DELETE);
	}

	public void organizeTypeSet(Integer value) {
		setInteger(KEY.ORGANIZE_TYPE, value);
		save();
	}

	public Integer organizeTypeGet() {
		return getInteger(KEY.ORGANIZE_TYPE);
	}

	public void darkSet(boolean value) {
		setBoolean(KEY.LAFDARK, value);
	}

	public boolean darkGet() {
		return getBoolean(KEY.LAFDARK);
	}

	/**
	 * get value for a key String
	 *
	 * @param key
	 * @param def: default value for empty
	 * @return
	 */
	public String get(String key, String def) {
		return (getString(key, def));
	}

	public String get(KEY key, String def) {
		return getString(key, def);
	}

	public String getString(KEY key) {
		return getString(key.toString(), key.getValue());
	}

	public String getString(KEY key, String def) {
		return getString(key.toString(), def);
	}

	public String getString(String key, String def) {
		for (PrefValue v : preferences) {
			if (v.key.equals(key)) {
				return (v.value);
			}
		}
		if (!"".equals(def)) {
			preferences.add(new PrefValue(key, def));
			save();
		}
		return def;
	}

	public String getChar(KEY key) {
		String r = getString(key, key.getValue());
		if (r.length() > 1) {
			int n = Integer.parseInt(r);
			String s = "" + (char) n;
			return (s);
		}
		return (r);
	}

	public Integer getInteger(KEY key) {
		if (key.getValue().isEmpty()) {
			return 0;
		}
		return getInteger(key, Integer.valueOf(key.getValue()));
	}

	public Integer getInteger(KEY key, Integer val) {
		String r = getString(key, val.toString());
		return (r.isEmpty() ? 0 : Integer.valueOf(r));
	}

	public boolean getBoolean(KEY key) {
		String b = key.getValue();
		return getBoolean(key.toString(), ("1".equals(b) || "true".equals(b)));
	}

	public boolean getBoolean(KEY key, boolean b) {
		return getBoolean(key.toString(), b);
	}

	public boolean getBoolean(String key, boolean val) {
		String r = getString(key, (val ? "true" : "false"));
		return "true".equals(r) || "1".equals(r);
	}

	public void setInteger(KEY key, Integer value) {
		setString(key, value.toString());
	}

	public void setInteger(String key, Integer value) {
		setString(key, value.toString());
	}

	public void setBoolean(KEY key, boolean value) {
		setString(key, (value ? "true" : "false"));
	}

	public void setBoolean(String key, boolean value) {
		setString(key, (value ? "true" : "false"));
	}

	public void setString(KEY key, String value) {
		setString(key.toString(), value);
	}

	public void setString(String key, String value) {
		boolean notok = true;
		if (value == null || value.isEmpty() || value.equals("null")) {
			removeString(key);
			return;
		}
		for (PrefValue v : preferences) {
			if (v.key.equals(key)) {
				v.set(value);
				notok = false;
			}
		}
		if (notok) {
			preferences.add(new PrefValue(key, value));
		}
	}

	public void removeString(KEY key) {
		removeString(key.toString());
	}

	public void removeString(String keyName) {
		for (int i = 0; i < preferences.size(); i++) {
			if (preferences.get(i).key.equals(keyName)) {
				preferences.remove(i);
				return;
			}
		}
	}

	/**
	 * create preferences File
	 *
	 * @param file
	 */
	private void create(File file) {
		LOG.log("Create new Preferences in " + file.getAbsolutePath());
		try {
			EnvUtil.getPrefDir().mkdir();
			file.createNewFile();
		} catch (IOException e) {
			LOG.log("Unable to create new file " + file.getAbsolutePath());
		}
		preferences.add(new PrefValue(KEY.VERSION));
		preferences.add(new PrefValue(KEY.FONT_SIZE));
		save();
	}

	/**
	 * save preferences
	 *
	 */
	public void save() {
		preferences.sort((PrefValue c1, PrefValue c2) -> c1.key.compareTo(c2.key));
		String newline = System.getProperty("line.separator");
		try (OutputStream f = new FileOutputStream(prefFile)) {
			for (PrefValue p : preferences) {
				if (isToLoad(p.key)) {
					f.write((p.toString() + newline).getBytes());
				}
			}
		} catch (FileNotFoundException ex) {
			LOG.err("Unable to save Preferences (file not found)", ex);
		} catch (IOException ex) {
			LOG.err("Unable to save Preferences", ex);
		}
	}

	public void fontInit() {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int sz = (int) d.getWidth() / 800 * 10;
		setString(KEY.FONT_SIZE, "Dialog,plain," + sz);
	}

	public String toString() {
		StringBuilder b = new StringBuilder();
		for (PrefValue p : preferences) {
			b.append(p.key).append("=").append(p.value).append("\n");
		}
		return b.toString();
	}

	/**
	 * Preference value class
	 */
	private static class PrefValue {

		String key;
		String value;

		public PrefValue(String k, String v) {
			key = k;
			value = v;
		}

		private PrefValue(KEY key) {
			this.key = key.toString();
			this.value = key.getValue();
		}

		public void set(String v) {
			value = v;
		}

		public String get() {
			return (value);
		}

		@Override
		public String toString() {
			return (key + "=" + value);
		}
	}

	/**
	 * Recent File class to associate filename with title
	 *
	 */
	public static class PrefFile {

		public String file, title;

		public PrefFile(String file, String title) {
			this.file = file;
			this.title = title;
		}

		@Override
		public String toString() {
			return (file + "|" + title);
		}
	}

}
