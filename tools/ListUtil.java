/*
 * Copyright (C) 2017 FaVdB
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * utilities for list
 *
 * @author FaVdB
 */
public class ListUtil {

	/**
	 * find the given String in the list
	 *
	 * @param list
	 * @param val
	 * @return empty if not found
	 */
	public static String find(List<String> list, String val) {
		for (String s : list) {
			if (s.contains(val)) {
				return s;
			}
		}
		return "";
	}

	/**
	 * empty for utilities
	 */
	private ListUtil() {
		// nothing
	}

	/**
	 * join a list into a String
	 *
	 * @param array
	 * @return
	 */
	public static String join(List array) {
		return (join(array, ","));
	}

	/**
	 * join a list into a String with the given separator
	 *
	 * @param array
	 * @param separator
	 * @return
	 */
	public static String join(List array, String separator) {
		//LOG.trace(TT+"join(array="+array.toString()+", separator)");
		String sep = separator;
		if (separator == null) {
			sep = "";
		}
		final StringBuilder buf = new StringBuilder();
		if (array != null) {
			for (int i = 0; i < array.size(); i++) {
				if (i > 0) {
					buf.append(sep);
				}
				if (array.get(i) != null) {
					Object obj = (Object) array.get(i);
					if (obj instanceof String) {
						buf.append(array.get(i));
					} else {
						buf.append(obj.toString());
					}
				}
			}
		}
		return buf.toString();
	}

	/**
	 * foint a given String array into a String
	 *
	 * @param str
	 * @return
	 */
	public static String join(String[] str) {
		if (str == null) {
			return "null";
		}
		StringBuilder b = new StringBuilder();
		for (String s : str) {
			if (!b.toString().isEmpty()) {
				b.append(", ");
			}
			b.append(s);
		}
		return b.toString();
	}

	/**
	 * set list with unique elements
	 *
	 * @param array
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List setUnique(List array) {
		List list = new ArrayList();
		if (array != null) {
			for (Object o : array.toArray()) {
				if (o != null && !list.contains(o)) {
					list.add(o);
				}
			}
		}
		return (list);
	}

	/**
	 * remove dupplicate or null elements
	 *
	 * @param list
	 * @return
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static List removeNullAndDuplicates(List list) {
		list.removeAll(Collections.singletonList(null));
		Set set = new LinkedHashSet(list);
		return new ArrayList(set);
	}
}
