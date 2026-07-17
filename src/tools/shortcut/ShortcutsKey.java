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
import javax.swing.KeyStroke;

/**
 * class for the shortcut key
 *
 * @author favdb
 */
public class ShortcutsKey {

	private final String id;
	private String sort;
	private String key;
	private final String msg;
	private boolean error = false;

	public ShortcutsKey(String id) {
		//LOG.trace(TT+"(id=" + id + ")");
		this.id = id;
		if (id.startsWith("heading")) {
			key = Shortcuts.getKeyBinding("heading").replace("H", "") + "[" + id.replace("heading", "") + "]";
			msg = I18N.getMsg("shortcut.i.heading") + " " + id.replace("heading", "");
		} else {
			key = Shortcuts.getKeyBinding(id).trim();
			String msgT = I18N.getMsg("shortcut.i." + id);
			if (msgT.startsWith("!")) {
				msgT = I18N.getMsg(id);
			}
			msg = msgT;
		}
		KeyStroke ks = Shortcuts.getKey(key);
		if (ks != null) {
			if (key.contains("[INS") || key.contains("[SUP")) {
				sort = ks.getModifiers() + "0" + key;
			} else {
				sort = ks.getModifiers() + "z" + key;
			}
		} else {
			sort = "[zzz] [z]";
		}
	}

	public String getId() {
		return this.id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
		KeyStroke ks = Shortcuts.getKey(key);
		sort = ks.getModifiers() + " " + ks.getKeyChar() + " " + key;
	}

	public String getMsg() {
		return msg;
	}

	public String getSort() {
		return sort;
	}

	public void resetError() {
		this.error = false;
	}

	public void setError() {
		this.error = true;
	}

	public boolean getError() {
		return error;
	}

	@Override
	public String toString() {
		return "{" + id + "," + sort + "," + key + "," + msg + "," + (error ? "error" : "valid") + "}";
	}

}
