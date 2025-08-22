/*
 * Copyright (C) 2024 favdb
 *
 * This program is free software"; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation"; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY"; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program"; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package tools.jpeg;

/**
 * adapted from https://github.com/drewnoakes/metadata-extractor
 *
 * @author favdb
 */
public class JpegExifTag {

	public static final String TAG_EXIFIFD = "8769";
	public static final String TAG_GPSIFD = "8825";
	public static final String TAG_NAME = "0000";
	public static final String TAG_SIZE = "0001";
	public static final String TAG_RESOLUTION = "0002";
	public static final String TAG_FOCALPLANERESOLUTION = "0003";
	public static final String TAG_FILESIZE = "0004";
	public static final String TAG_FILEDATETIME = "0005";
	public static final String TAG_MAKE = "010F";
	public static final String TAG_MODEL = "0110";
	public static final String TAG_ORIENTATION = "0112";
	public static final String TAG_XRESOLUTION = "011A";
	public static final String TAG_YRESOLUTION = "011B";
	public static final String TAG_RESOLUTIONUNIT = "0128";
	public static final String TAG_SOFTWARE = "0131";
	public static final String TAG_CHANGEDATETIME = "0132";//<<<<<
	public static final String TAG_ARTIST = "013B";
	public static final String TAG_YCBCRPOSITION = "0213";
	public static final String TAG_COPYRIGHT = "8298";
	public static final String TAG_DESCRIPTION = "010E";
	public static final String TAG_EXPOSURETIME = "829A";
	public static final String TAG_FNUMBER = "829D";
	public static final String TAG_EXPOSUREPROGRAM = "8822";
	public static final String TAG_ISOSPEEDRATINGS = "8827";
	public static final String TAG_EXIFVERSION = "9000";
	public static final String TAG_ORIGINALDATETIME = "9003";//<<<<<
	public static final String TAG_DIGITIZEDDATETIME = "9004";//<<<<<
	public static final String TAG_COMPONENTSCCONFIG = "9101";
	public static final String TAG_COMPRESSEDPITSPERPIXEL = "9102";
	public static final String TAG_SHUTERSPEEDVALUE = "9201";
	public static final String TAG_APERTUREVALUE = "9202";
	public static final String TAG_BRIGHTNESSVALUE = "9203";
	public static final String TAG_EXPOSUREBIASVALUE = "9204";
	public static final String TAG_MAXAPERTUREVALUE = "9205";
	public static final String TAG_METERINGMODE = "9207";
	public static final String TAG_LIGHTSOURCE = "9208";
	public static final String TAG_FLASH = "9209";
	public static final String TAG_FOCALLENGTH = "920A";
	public static final String TAG_MAKERNOTE = "927C";
	public static final String TAG_USERCOMMENT = "9286";
	public static final String TAG_COLORSPACE = "A001";
	public static final String TAG_PIXELXDIMENSION = "A002";
	public static final String TAG_PIXELYDIMENSION = "A003";
	public static final String TAG_FOCALPLANEXRESOLUTION = "A20E";
	public static final String TAG_FOCALPLANEYRESOLUTION = "A20F";
	public static final String TAG_FOCALPLANERESOLUTIONUNIT = "A210";
	public static final String TAG_SENSINGMODE = "A217";
	public static final String TAG_FILESOURCE = "A300";
	public static final String TAG_SCENETYPE = "A301";
	public static final String TAG_CUSTOMRENDERED = "A401";
	public static final String TAG_EXPOSUREMODE = "A402";
	public static final String TAG_WHITEBALANCE = "A403";
	public static final String TAG_DIGITALZOOMRATIO = "A404";
	public static final String TAG_FOCALLENGTH35MM = "A405";
	public static final String TAG_SCENECAPTURETYPE = "A406";
	public static final String TAG_GAINCONTROL = "A407";
	public static final String TAG_CONTRAST = "A408";
	public static final String TAG_SATURATION = "A409";
	public static final String TAG_SHARPNESS = "A40A";
	public static final String TAG_SUBJECTDISTANCERANGE = "A40C";
	public static final String TAG_UNIQUEIMAGEID = "A420";
	public static final String TAG_CAMERAOWNER = "A430";
	public static final String TAG_BODYSERIAL = "A431";
	public static final String TAG_LENSMAKE = "A433";
	public static final String TAG_LENSMODEL = "A434";
	public static final String TAG_LENSSERIAL = "A435";

	public enum FORMAT {
		BYTE(1), ASCII(2), SHORT(3), LONG(4), RATIONAL(5), UNDEFINED(7), SLONG32(9), SRATIONAL(10);
		final private int val;

		private FORMAT(int val) {
			this.val = val;
		}
	}

	public static String getFormat(int fmt) {
		return FORMAT.values()[fmt - 1].toString();
	}

}
