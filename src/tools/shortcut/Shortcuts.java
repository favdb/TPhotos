/*
 * Copyright (C) 2023 favdb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tools.shortcut;

import i18n.I18N;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javax.swing.KeyStroke;
import tools.LOG;
import tools.file.EnvUtil;
import tools.file.FileUtil;

/**
 * shortcuts key binding
 *
 * @author favdb
 */
public class Shortcuts {

	private static final String TT = "Shortcuts.";

	private static Properties keyProps, userKeyProps;
	private static File userKeyFile;

	// arrays of shortcuts
	public static String SHEF[] = {
		//without modifier
		"help.shortcuts", "spell",
		// with shift
		"charunbreak", "linebreak", "backward",
		//with ctrl
		"bold", "italic", /*"strike", */ "underline", "subscript", "superscript",
		"copy", "cut", "paste",
		"undo", "redo", "find", "replace", "save", "select_all",
		"zoomin", "zoomout",
		//with alt
		"endnote_add", "endnote_show",
		// with ctrl+shit
		"copy_full", "paste_formatted",
		// alt
		"al_left", "al_center", "al_right", "al_justify",
		"heading", "paragraph", "listordered", "listunordered",
		"highlight",
		"image", "hyperlink", "table"
	};

	public static final String[] OSBK = {
		// edit and tools
		"help.shortcut", "copy_full", "duplicate", "insert", "delete", "find", "replace",
		"editor.reset",
		// file
		"file.new", "project.open", "project.close", "project.save",
		"file.exit", "file.refresh",
		// new entity
		"new.category", "new.chapter", "new.event", "new.gender", "new.idea", "new.item",
		"new.location", "new.memo", "new.part", "new.person", "new.plot", "new.relation",
		"new.scene", "new.strand", "new.tag",
		// show view
		// menus
		"menu",
		//"file", "edit", "new", "tools", "help",
		// FOI
		"foi.title",
		"typist.full"
	};

	/**
	 * initialize the user shortcuts properties to access
	 */
	public static void init() {
		keyProps = new Properties();
		try {
			keyProps.load(Shortcuts.class.getResourceAsStream("shortcuts.properties"));
		} catch (IOException ex) {
			LOG.err(TT + "init() unable to read shortcuts.properties");
			return;
		}
		userKeyFile = new File(EnvUtil.getPrefDir() + File.separator + "shortcuts.ini");
		if (!userKeyFile.exists()) {
			// create an empty user key properties
			try {
				userKeyFile.createNewFile();
			} catch (IOException e) {
				LOG.err(TT + "init() create user shortcuts file error"
						+ " Error: \n" + e.getLocalizedMessage());
			}
		}
		try {
			userKeyProps = new Properties();
			userKeyProps.load(new FileInputStream(userKeyFile.getAbsolutePath()));
		} catch (IOException e) {
			LOG.err(TT + "init() usage of user shortcuts file error", e);
			userKeyProps = null;
		}
	}

	/**
	 * get a toolstip text for the given shortcut
	 *
	 * @param type may be shef or osbk, not used
	 * @param msg shortcut to search
	 *
	 * @return empty String if not found
	 */
	public static String getTooltips(String type, String msg) {
		//LOG.trace(TT + ".getTooltips(type='" + type + "', msg='" + msg + "')");
		String str = getKeyAsString(type, msg);
		if (str != null && !str.isEmpty()) {
			if (msg.startsWith("heading")) {
				int t = Integer.parseInt(msg.replace("heading", ""));
				String key = getKeyBinding("heading").replace("H", msg.replace("heading", ""));
				return I18N.getMsg("shortcut.i.heading") + " " + t
						+ " (" + key.replace(" ", "+") + ")";
			} else {
				return I18N.getMsg("shortcut.i." + msg) + " (" + str.replace(" ", "+") + ")";
			}
		}
		return I18N.getMsg(type + "." + msg);
		//return "!" + type + "." + msg + "!";
	}

	/**
	 * get the shorcut String for the given msg in the given type
	 *
	 * @param type
	 * @param msg
	 * @return
	 */
	public static String getKeyAsString(String type, String msg) {
		if (type.equals("editor") || type.equals("shef")) {
			return findString(msg, SHEF);
		} else if (type.equals("osbk")) {
			return findString(msg, OSBK);
		}
		return "";
	}

	/**
	 * find the String value of the given msg into list of keys
	 *
	 * @param msg
	 * @param lst
	 * @return empty String if not found
	 */
	private static String findString(String msg, String[] lst) {
		String rc = "";
		for (String sl : lst) {
			if (sl.startsWith("heading") && msg.startsWith("heading")) {
			} else if (sl.startsWith(msg)) {
				rc = getKeyBinding(msg);
			}
		}
		if (!rc.isEmpty()) {
			rc = i18n(rc);
		}
		return rc;
	}

	public static String i18n(String value) {
		String rc = value;
		if (rc.contains("shift")) {
			rc = rc.replace("shift", I18N.getMsg("shortcut.0.shift"));
		}
		if (rc.contains("alt")) {
			rc = rc.replace("alt", I18N.getMsg("shortcut.0.alt"));
		}
		if (rc.contains("ctrl")) {
			rc = rc.replace("ctrl", I18N.getMsg("shortcut.0.ctrl"));
		}
		rc = rc.replace("ENTER", I18N.getMsg("shortcut.0.enter"))
				.replace("SPACE", I18N.getMsg("shortcut.0.space"))
				.replace("DELETE", I18N.getMsg("shortcut.0.delete"))
				.replace("INSERT", I18N.getMsg("shortcut.0.insert"));
		return rc;
	}

	/**
	 * read the shorcut value from user shortcut.properties or from embeded file
	 *
	 * @param value key to search
	 *
	 * @return
	 */
	public static String getKeyBinding(String value) {
		String kv = null;
		if (userKeyProps != null) {
			kv = userKeyProps.getProperty(value);
		}
		if (kv == null || kv.isEmpty()) {
			kv = readKey(value);
		}
		if (kv.startsWith("!")) {
			kv = null;
		}
		return (kv == null ? "" : kv);
	}

	/**
	 * get the shortcut String for the given key
	 *
	 * @param key
	 * @return
	 */
	public static String getKeyString(String key) {
		if (key.contains("heading")) {
			int n = Integer.parseInt(key.replace("heading", ""));
			String p = getKeyBinding("heading").replace("H", "");
			return p + "[" + n + "]";
		} else {
			return getKeyBinding(key);
		}
	}

	/**
	 * get a KeyStroke for the given key String
	 *
	 * @param key
	 * @return
	 */
	public static KeyStroke getKeyStroke(String key) {
		//LOG.trace("getKeyStroke(type='" + type + "', key='" + key + "')");
		KeyStroke ks = getKeyStroke("shef", key);
		if (ks == null) {
			ks = getKeyStroke("osbk", key);
		}
		if (ks == null) {
			//try to get the keystroke from the embeded file
			String sk = readKey(key);
			if (!sk.startsWith("!")) {
				ks = KeyStroke.getKeyStroke(sk);
			}
		}
		return ks;
	}

	/**
	 * get a KeyStroke for the given key String from the given type
	 *
	 * @param type
	 * @param key
	 * @return
	 */
	public static KeyStroke getKeyStroke(String type, String key) {
		//LOG.trace(".getKeyStroke(type='" + type + "', key='" + key + "')");
		if (type.equals("editor") || type.equals("shef")) {
			return findKeyStroke(key, SHEF);
		} else if (type.equals("osbk")) {
			return findKeyStroke(key, OSBK);
		}
		return null;
	}

	/**
	 * find the given key into the given list of shortcuts
	 *
	 * @param key
	 * @param list
	 * @return
	 */
	private static KeyStroke findKeyStroke(String key, String[] list) {
		//LOG.trace(".findKeyStroke(msg='" + key + "', list len='" + list.length + "')");
		for (String sl : list) {
			if (sl.startsWith("heading") && key.startsWith("heading")) {
				int t = Integer.parseInt(key.replace("heading", ""));
				return KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0 + t,
						KeyEvent.ALT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
			}
			if (sl.startsWith(key)) {
				return getKey(getKeyBinding(key));
			}
		}
		return null;
	}

	/**
	 * get a KeyStroke for the given key with the given modifier
	 *
	 * @param key
	 * @return
	 */
	public static KeyStroke getKey(String key) {
		//LOG.trace(".getKey(key=" + key + ")");
		int modifier = KeyUtil.getModifier(key);
		if (!key.startsWith("!")) {
			if (key.endsWith("[F1]") || key.endsWith("[F2]")
					|| key.endsWith("[F3]") || key.endsWith("[F4]")
					|| key.endsWith("[F5]") || key.endsWith("[F6]")
					|| key.endsWith("[F7]") || key.endsWith("[F8]")
					|| key.endsWith("[F9]") || key.endsWith("[F10]")
					|| key.endsWith("[F11]") || key.endsWith("[F12]")) {

				int i = Integer.parseInt(key
						.replace("[F", "")
						.replace("]", "")
						.replace("alt", "")
						.replace("ctrl", "")
						.replace("shift", "").trim());
				return KeyStroke.getKeyStroke(KeyEvent.VK_F1 + i - 1, modifier);
			} else if (key.endsWith("[+]")) {
				return KeyStroke.getKeyStroke(KeyEvent.VK_ADD, modifier);
			} else if (key.endsWith("[-]")) {
				return KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, modifier);
			} else if (key.contains("[SUPP")) {
				return KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, modifier);
			} else if (key.contains("[INS")) {
				return KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, modifier);
			}
			if (key.endsWith("[1]") || key.endsWith("[2]")
					|| key.endsWith("[3]") || key.endsWith("[4]")
					|| key.endsWith("[5]") || key.endsWith("[6]")) {
				String kk = key.substring(key.lastIndexOf(" ") + 1);
				int i = Integer.parseInt(kk.replace("[", "").replace("]", ""));
				return KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0 + i, modifier);
			}
			return KeyStroke.getKeyStroke(key.replace("[", "").replace("]", ""));
		}

		return null;
	}

	/**
	 * record the key binding in the user properties
	 *
	 * @param id
	 * @param key
	 */
	public static void setKey(String id, String key) {
		//LOG.trace(TT + ".setKey(id='" + id + "', key='" + key + "')");
		userKeyProps.setProperty(id, key);
	}

	/**
	 * save the user properties to the user file
	 */
	public static void saveKeys() {
		//LOG.trace(TT + ".saveKeys()");
		try (FileOutputStream out = new FileOutputStream(userKeyFile)) {
			userKeyProps.store(out, null);
			FileUtil.fileSort(userKeyFile);
			init();
		} catch (IOException ex) {
			LOG.err(TT + ".saveKeys() " + ex.getLocalizedMessage());
		}
	}

	/**
	 * read the key binding from the embeded file
	 *
	 * @param key
	 * @return
	 */
	private static String readKey(String key) {
		String str = keyProps.getProperty(key);
		return (str == null ? "!" + key + "!" : str);
	}

}
