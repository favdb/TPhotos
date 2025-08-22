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
package tools.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Calendar;
import static java.util.Calendar.*;
import java.util.Date;

/**
 * source from
 * https://codes-sources.commentcamarche.net/source/102746-lecture-et-ecriture-des-dates-d-un-fichier-ou-d-un-dossier
 *
 * @author favdb
 */
public class FileDate {

	private static final Calendar cal = Calendar.getInstance();

	/**
	 * Cette fonction permet de définir la date de création d'un fichier ou d'un dossier.
	 *
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param minute
	 * @param seconde
	 * @param file Le fichier ou dossier dont il faut modifier la date de création
	 */
	public static void creationSet(int year, int month, int day,
			int hour, int minute, int seconde,
			File file) {
		try {
			Path path = file.toPath();
			cal.set(year, month - 1, day, hour, minute, seconde);
			BasicFileAttributes ba;
			ba = Files.readAttributes(path, BasicFileAttributes.class);
			BasicFileAttributeView bs = Files.getFileAttributeView(path, BasicFileAttributeView.class);
			bs.setTimes(ba.lastModifiedTime(),
					ba.lastAccessTime(),
					FileTime.fromMillis(cal.getTimeInMillis()));
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}

	public static void creationSet(Date d, File file) {
		cal.setTime(d);
		creationSet(cal.get(YEAR), cal.get(MONTH), cal.get(DAY_OF_MONTH),
				cal.get(HOUR), cal.get(MINUTE), cal.get(SECOND),
				file);
	}

	/**
	 * Cette fonction permet de définir la date de modification d'un fichier ou d'un dossier.
	 *
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param minute
	 * @param seconde
	 * @param file Le fichier ou dossier dont il faut modifier la date de modification.
	 */
	public static void modificationSet(int year, int month, int day,
			int hour, int minute, int seconde,
			File file) {
		try {
			Path path = file.toPath();
			cal.set(year, month - 1, day, hour, minute, seconde);
			BasicFileAttributes ba;
			ba = Files.readAttributes(path, BasicFileAttributes.class);
			BasicFileAttributeView b
					= Files.getFileAttributeView(path, BasicFileAttributeView.class);
			b.setTimes(FileTime.fromMillis(cal.getTimeInMillis()),
					ba.lastAccessTime(), ba.creationTime());
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}

	public static void modificationSet(Date d, File file) {
		cal.setTime(d);
		modificationSet(cal.get(YEAR), cal.get(MONTH), cal.get(DAY_OF_MONTH),
				cal.get(HOUR), cal.get(MINUTE), cal.get(SECOND),
				file);
	}

	/**
	 * Cette fonction permet de définir la date de dernier accès d'un fichier ou d'un dossier.
	 *
	 * @param yy: année
	 * @param mm: mois
	 * @param dd: jour
	 * @param hh: heure
	 * @param MM: minute
	 * @param ss: seconde
	 * @param file Le fichier ou dossier dont il faut modifier la date de dernier accès.
	 */
	public static void lastSet(int yy, int mm, int dd,
			int hh, int MM, int ss,
			File file) {
		try {
			Path path = file.toPath();
			cal.set(yy, mm - 1, dd, hh, MM, ss);
			BasicFileAttributes ba;
			ba = Files.readAttributes(path, BasicFileAttributes.class);
			BasicFileAttributeView b = Files.getFileAttributeView(path, BasicFileAttributeView.class);
			b.setTimes(ba.lastModifiedTime(),
					FileTime.fromMillis(cal.getTimeInMillis()),
					ba.creationTime());
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}

	public static void lastSet(Date d, File file) {
		cal.setTime(d);
		lastSet(cal.get(YEAR), cal.get(MONTH), cal.get(DAY_OF_MONTH),
				cal.get(HOUR), cal.get(MINUTE), cal.get(SECOND),
				file);
	}

	/**
	 * Cette fonction permet d'obtenir la date de création d'un fichier ou d'un dossier.
	 *
	 * @param file Le fichier ou dossier dont il faut obtenir la date de création.
	 * @return La date de création du fichier ou du dossier donné en paramètre de la méthode, ou
	 * null si le fichier n'existe pas.
	 */
	public static Date creationGet(File file) {
		if (!file.exists()) {
			return null;
		}
		Path path = file.toPath();
		try {
			BasicFileAttributes b = Files.readAttributes(path, BasicFileAttributes.class);
			cal.setTimeInMillis(b.creationTime().toMillis());
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
		return cal.getTime();
	}

	/**
	 * Cette fonction permet d'obtenir la date de modification d'un fichier ou d'un dossier.
	 *
	 * @param file le fichier ou le dossier dont il faut obtenir la date de modification.
	 * @return La date de modification du fichier ou du dossier donné en paramètre de la méthode, ou
	 * null si le fichier n'existe pas.
	 */
	public static Date modificationGet(File file) {
		if (!file.exists()) {
			return null;
		}
		Path path = file.toPath();
		try {
			BasicFileAttributes b = Files.readAttributes(path, BasicFileAttributes.class);

			cal.setTimeInMillis(b.lastModifiedTime().toMillis());
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
		return cal.getTime();
	}

	/**
	 * Cette fonction permet d'obtenir la date de dernier accès d'un fichier ou d'un dossier.
	 *
	 * @param file Le fichier ou le dossier dont il faut obtenir la date de dernier accès.
	 * @return La date de modification du fichier ou du dossier donné en paramètre de la méthode, ou
	 * null si le fichier n'existe pas.
	 */
	public static Date lastGet(File file) {
		if (!file.exists()) {
			return null;
		}
		Path path = file.toPath();
		try {
			BasicFileAttributes b = Files.readAttributes(path, BasicFileAttributes.class);
			cal.setTimeInMillis(b.lastAccessTime().toMillis());
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
		return cal.getTime();
	}
}
