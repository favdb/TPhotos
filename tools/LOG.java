/*
 * Copyright (C) 2022 favdb
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
package tools;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import tools.file.EnvUtil;
import tools.file.FileUtil;

/**
 * logging facility
 *
 * @author favdb
 */
public class LOG {

	private static boolean BTRACE = false, BTRACEFILE = false;

	/**
	 * set trace logging
	 */
	public static void setTrace() {
		BTRACE = true;
	}

	public static void setTrace(boolean btrace) {
		BTRACE = btrace;
	}

	public static void traceToFile(String file, String text) {
		if (BTRACE) {
			FileUtil.fileWriteString(EnvUtil.getHomeDir() + File.separator + "trace.html", text);
		}
	}

	/**
	 * empty because this is only for static functions
	 */
	private LOG() {
		//empty
	}

	/**
	 * initalize logging
	 */
	public static void init() {
		BTRACE = false;
	}

	/**
	 * log a message
	 *
	 * @param msg
	 */
	public static void log(String msg) {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
		System.out.println(dateFormat.format(new Date()) + " " + msg);
	}

	/**
	 * get the trace status
	 *
	 * @return
	 */
	public static boolean getTrace() {
		return BTRACE;
	}

	/**
	 * trace a message
	 *
	 * @param msg
	 */
	public static void trace(String msg) {
		if (BTRACE) {
			log(msg);
		}
	}

	/**
	 * trace a message
	 *
	 * @param key
	 * @param value
	 */
	public static void trace(String key, String value) {
		if (BTRACE) {
			trace(key + "='" + value + "'");
		}
	}

	/**
	 * get boolean value to String
	 *
	 * @param b
	 * @return
	 */
	public static String trace(boolean b) {
		return "'" + (b ? "true" : "false") + "'";
	}

	/**
	 * report an error
	 *
	 * @param txt
	 * @param e
	 */
	public static void err(String txt, Exception... e) {
		if (e != null && e.length > 0) {
			System.err.println(txt);
			System.err.println("Exception:" + e[0].getMessage());
			e[0].printStackTrace(System.err);
		} else {
			System.err.println(txt);
		}
	}

}
