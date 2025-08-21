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
package tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * utilities for date
 *
 * @author favdb
 */
public class DateUtil {

	/**
	 * get a formatted String date
	 *
	 * @param indate
	 * @return
	 */
	public static String toFormatted(String indate) {
		try {
			SimpleDateFormat fm1 = new SimpleDateFormat("yyyyMMdd_HHmmss");
			Date date = fm1.parse(indate);
			SimpleDateFormat fm2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			return fm2.format(date);
		} catch (ParseException ex) {
			return "???";
		}
	}

	/**
	 * get a condensed String date
	 *
	 * @param indate
	 * @return
	 */
	public static String toCondensed(Date indate) {
		try {
			SimpleDateFormat fm1 = new SimpleDateFormat("yyyyMMdd_HHmmss");
			return fm1.format(indate);
		} catch (Exception ex) {
			return null;
		}
	}

}
