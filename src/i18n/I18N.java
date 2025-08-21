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
package i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.UIManager;
import tools.LOG;

/**
 * static class for internationalization
 *
 * @author favdb
 */
public class I18N {

	private static final String TT = "I18N.";

	private I18N() {
		throw new IllegalStateException("Utility class");
	}

	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String TIME_FORMAT = "HH:mm:ss";
	public static final String DATE_TIME_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;
	private static ResourceBundle messageBundle = null;
	private static Properties propMsg;
	private static Properties propFile;
	private static String propFileName;
	private static char msgType = '0';//0=not init, i=internal, x=external, f=file

	public static String getCountryLanguage(Locale locale) {
		return locale.getLanguage() + "_" + locale.getCountry();
	}

	public static final String getMsg(String resourceKey, Object arg) {
		Object[] args = new Object[]{arg};
		return getMsg(resourceKey, args);
	}

	public static final String getMsg(String resourceKey, Object[] args) {
		MessageFormat formatter = new MessageFormat("");
		formatter.setLocale(Locale.getDefault());
		String pattern = getMsg(resourceKey).replace("'", "''");
		formatter.applyPattern(pattern);
		return formatter.format(args);
	}

	public static Integer getMnem(String key) {
		String s = getMsg(key + ".mnemonic");
		int z = ' ';
		if (s != null && s.length() > 0 && !s.startsWith("!")) {
			z = s.charAt(0);
		}
		return z;
	}

	public static char getMnemonic(String key) {
		String s = getMsg(key + ".mnemonic");
		if (s != null && s.length() > 0 && !s.startsWith("!")) {
			return s.charAt(0);
		}
		return ' ';
	}

	public static String getLanguage(String key) {
		ResourceBundle rb = ResourceBundle.getBundle("i18n.language", Locale.getDefault());
		try {
			return rb.getString(key);
		} catch (Exception ex) {
			LOG.err("language exception:" + ex.getLocalizedMessage() + "\nlanguage=" + key);
			return '!' + key + '!';
		}
	}

	public static final void initMessages(Locale locale) {
		//LOG.trace(TT + "initMessages(locale=" + locale.toString() + ")");
		if (msgType == '0' || msgType == 'i') {
			initMsgInternal(locale);
			initMsgProp();
		}
		if (propFileName != null) {
			initMsgFile();
		}
	}

	public static void resetFileMessages() {
		//LOG.trace(TT+".resetFileMessages()");
		msgType = '0';
		propFileName = null;
		initMessages(Locale.getDefault());
	}

	public static void setFileMessages(String nf) {
		//LOG.trace(TT+"setFileMessages(nf=" + nf + ")");
		if (nf == null || nf.isEmpty() || nf.equals("null")) {
			resetFileMessages();
			return;
		}
		File file = new File(nf);
		if (file.exists()) {
			propFileName = nf;
			msgType = 'x';
		}
	}

	// an external properties file somewhere
	public static final void initMsgFile() {
		//LOG.trace(TT+"initMsgFile()");
		if (propFileName == null || propFileName.equals("null")) {
			resetFileMessages();
			return;
		}
		propMsg = new Properties();
		InputStream input;
		try {
			input = new FileInputStream(propFileName);
			propMsg.load(input);
			input.close();
		} catch (IOException ex) {
			LOG.err("messages file error ");
		}
	}

	// an external file inside the install sub-directory resources/i18n
	public static final Properties initMsgProp() {
		//LOG.trace(TT+"initMsgProp()");
		if (propMsg == null) {
			try {
				propMsg = new Properties();
				InputStream input;
				// messages localized
				String dir = System.getProperty("user.dir") + File.separator + "resources" + File.separator + "i18n";
				String propMsgName = dir + File.separator + "messages_" + Locale.getDefault().getLanguage() + ".properties";
				File file = new File(propMsgName);
				if (!file.exists()) {
					propMsgName = dir + File.separator + "messages.properties";
					file = new File(propMsgName);
					if (!file.exists()) {
						propMsg = null;
						return (null);
					}
				}
				input = new FileInputStream(propMsgName);
				propMsg.load(input);
				input.close();
				msgType = 'x';
				return (propMsg);
			} catch (IOException ex) {
				LOG.err("external resources missing");
				propMsg = null;
				return (null);
			}
		}
		return (propMsg);
	}

	public static final Properties getExternalResources() {
		if (propMsg == null) {
			initMsgFile();
		}
		return propMsg;
	}

	public static String getMsgFile(String key) {
		//LOG.trace(TT+"getMsgFile(key="+key+")");
		try {
			String r = propFile.getProperty(key);
			if (r == null) {
				return "?" + key + "?";
			}
			return (r);
		} catch (Exception ex) {
			return "?" + key + "?";
		}
	}

	public static String getMsgExternal(String key) {
		//LOG.trace(TT+"getMsgExternal(key="+key+")");
		try {
			String r = propMsg.getProperty(key);
			if (r == null) {
				return "[" + key + "]";
			}
			return (r);
		} catch (Exception ex) {
			return "[" + key + "]";
		}
	}

	public static String getMsgInternal(String key) {
		//LOG.trace(TT+"getMsgInternal(key="+key+")");
		ResourceBundle rb = getResourceBundle();
		try {
			return (rb.getString(key));
		} catch (Exception ex) {
			return "!" + key + "!";
		}
	}

	public static final void initMsgInternal(Locale locale) {
		//LOG.trace(TT+"initMsgInternal(locale="+locale.toString()+")");
		ResourceBundle.clearCache();
		messageBundle = null;
		msgType = 'i';
		Locale.setDefault(locale);
		UIManager.getDefaults().setDefaultLocale(locale);
	}

	public static final ResourceBundle getResourceBundle() {
		if (messageBundle == null) {
			messageBundle = ResourceBundle.getBundle("i18n.msg.messages", Locale.getDefault());
		}
		return messageBundle;
	}

	public static String getMsg(String key) {
		String r;
		switch (msgType) {
			case 'f':
				r = getMsgFile(key);
				break;
			case 'x':
				r = getMsgExternal(key);
				break;
			default:
				r = getMsgInternal(key);
				break;
		}
		return r.replaceAll("  *", " ");
	}

	public static String getRequiredMsg(String key, boolean required) {
		StringBuilder buf = new StringBuilder();
		if (required) {
			buf.append("*");
		}
		buf.append(getMsg(key));
		return buf.toString();
	}

	public static String getColonMsg(String key) {
		return getColonMsg(key, false);
	}

	public static String getColonMsg(String resourceKey, boolean required) {
		StringBuilder buf = new StringBuilder();
		if (required) {
			buf.append("*");
		}
		buf.append(getMsg(resourceKey));
		buf.append(":");
		return buf.toString();
	}

}
