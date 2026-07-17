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
package app.print;

import java.util.Comparator;
import java.util.List;

/**
 * class for a print cell
 *
 * @author favdb
 */
public class PrintItem {

	public enum CellType {
		EMPTY,
		PHOTO,
		TEXT
	}

	public int id, page, photoId = -1;
	private int spanH = 1, spanV = 1, textId;
	public String type = "text", comment = "", photoFile = "", text = "";
	private POSITION pos = new POSITION();

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public PrintItem(int id, int photo_id, String photo, String comment, int page, int... span) {
		this.id = id;
		this.comment = comment;
		this.photoId = photo_id;
		this.photoFile = photo;
		this.type = "photo";
		this.page = page;
		this.spanSet(span);
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public PrintItem(int id, String text, int page, int... span) {
		this.id = id;
		this.type = "text";
		this.text = text;
		this.page = page;
		this.spanSet(span);
	}

	/**
	 * empty PrintCell
	 *
	 * @param id
	 * @param cellNum
	 */
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public PrintItem(int id, int cellNum) {
		this.id = id;
		this.type = "";
		this.cellNumSet(cellNum);
		this.spanSet(1, 1);
	}

	public PrintItem() {
		this.id = 0;
		this.type = "unknown";
		this.cellNumSet(0);
		this.spanSet(1, 1);
	}

	/**
	 * get the id
	 *
	 * @return
	 */
	public int idGet() {
		return id;
	}

	/**
	 * get the type
	 *
	 * @return
	 */
	public String typeGet() {
		return type;
	}

	/**
	 * set the type
	 *
	 * @param value
	 */
	public void typeSet(String value) {
		this.type = value;
	}

	/**
	 * check if this is empty
	 *
	 * @return
	 */
	public boolean isEmpty() {
		return !"text".equals(typeGet()) && !"photo".equals(typeGet());
	}

	/**
	 * chack if type is a photo 'p'
	 *
	 * @return
	 */
	public boolean isPhoto() {
		return (type.startsWith("p"));
	}

	/**
	 * set the photo id
	 *
	 * @param value
	 */
	public void photoIdSet(int value) {
		this.type = "photo";
		this.photoId = value;
		this.text = "";
	}

	/**
	 * get the photo id
	 *
	 * @return
	 */
	public int photoIdGet() {
		return photoId;
	}

	/**
	 * set the photo file name
	 *
	 * @param value
	 */
	public void photoFileSet(String value) {
		try {
			this.photoFile = value.trim();
		} catch (Exception e) {
			this.photoId = -1;
			this.photoFile = "";
		}
	}

	/**
	 * get the photo file name
	 *
	 * @return
	 */
	public String photoFileGet() {
		return this.photoFile;
	}

	/**
	 * check if this is a text
	 *
	 * @return
	 */
	public boolean isText() {
		return (type.equals("text"));
	}

	/**
	 * set the text
	 *
	 * @param value
	 */
	public void textSet(String value) {
		this.type = "text";
		this.text = value;
		this.photoId = -1;
	}

	public String textGet() {
		return this.text;
	}

	/**
	 * set only the pos number
	 *
	 * @param cellNum
	 */
	public void posSet(int cellNum) {
		pos.cellNumSet(cellNum);
	}

	/**
	 * set pos and span values from the given String (P,C,H,V)
	 *
	 * @param value
	 */
	public void posSet(String value) {
		String p[] = value.split(",");
		if (p.length > 2) {
			pos.cellNumSet(Integer.parseInt(p[0].trim()));
			pos.spanHSet(Integer.parseInt(p[1].trim()));
			pos.setSpanV(Integer.parseInt(p[2].trim()));
		}
	}

	/**
	 * get the cell id
	 *
	 * @return
	 */
	public int cellIdGet() {
		if (pos != null) {
			return pos.cellNumGet();
		}
		return 0;
	}

	/**
	 * set only the span
	 *
	 * @param h
	 * @param v
	 */
	public void spanSet(int h, int v) {
		pos.spanSet(h, v);
	}

	/**
	 * the the span values as a String (H,V)
	 *
	 * @param span
	 */
	public void spanSet(String span) {
		pos.setSpan(span);
	}

	/**
	 * set the span values
	 *
	 * @param span
	 */
	public void spanSet(int... span) {
		if (span != null && span.length > 1) {
			pos.spanHSet(span[0]);
			pos.setSpanV(span[1]);
		}
	}

	/**
	 * get the horizontal span
	 *
	 * @return
	 */
	public int spanHorizontalGet() {
		if (pos != null) {
			return pos.spanHGet();
		}
		return 0;
	}

	/**
	 * get the vertical span
	 *
	 * @return
	 */
	public int spanVerticalGet() {
		if (pos != null) {
			return pos.getSpanV();
		}
		return 0;
	}

	/**
	 * get the page number
	 *
	 * @return
	 */
	public int pageGet() {
		return page;
	}

	/**
	 * set the page number
	 *
	 * @param value
	 */
	public void pageSet(int value) {
		this.page = value;
	}

	/**
	 * get this PrintItem as a String
	 *
	 * @return
	 */
	@Override
	public String toString() {
		if (isPhoto()) {
			return String.format("id=%d type=%s photo_id=%s file=%s page=%d pos=(%s) comment=%s", id, type,
					photoId, photoFile, page, pos.toString(), comment);
		} else {
			return String.format("id=%d type=%s text_id=%s page=%d pos=%s textlen=%d", id, type,
					textId, page, pos.toString(), text.length());
		}
	}

	/**
	 * get the Xml String of this PrintItem
	 *
	 * @return
	 */
	public String toXml() {
		StringBuilder b = new StringBuilder();
		b.append("<cell")
				.append(String.format(" id=\"%d\"", id))
				.append(String.format(" pos=\"%s\"", pos.toString()));
		if (isPhoto()) {
			b.append(String.format(" ref=\"%s\"", photoId))
					.append(" type=\"photo\"")
					.append(" />");
		} else {
			b.append(" type=\"text\"")
					.append(" />");
			b.append(">");
			b.append("<content><![CDATA[\n").append(text).append("]]></content>");
			b.append("</cell>");
		}
		return b.toString();
	}

	/**
	 * get the photo file name as a String
	 *
	 * @return
	 */
	public String photoGet() {
		return photoFile;
	}

	/**
	 * get the comment attribute
	 *
	 * @return
	 */
	public String commentGet() {
		return comment;
	}

	public void commentSet(String value) {
		this.comment = value;
	}

	/**
	 * set the cell id
	 *
	 * @param value
	 */
	public void cellIdSet(int value) {
		pos.cellNumSet(value);
	}

	/**
	 * set the horizontal span
	 *
	 * @param value
	 */
	public void spanHorizontalSet(int value) {
		pos.spanHSet(value);
	}

	/**
	 * set the vertical span
	 *
	 * @param value
	 */
	public void spanVerticalSet(int value) {
		pos.setSpanV(value);
	}

	/**
	 * set the text id
	 *
	 * @param refId
	 */
	public void textIdSet(int refId) {
		textId = refId;
	}

	/**
	 * get the id of a text
	 *
	 * @return
	 */
	public int textIdGet() {
		return textId;
	}

	/**
	 * Clear content of this PrintItem (without change of POSITION)
	 */
	public void clear() {
		this.type = "unknown";
		this.photoId = -1;
		this.photoFile = "";
		this.textId = -1;
		this.text = "";
		this.comment = "";
	}

	/**
	 * Swap content with the given PrintItem (without change of POSITION)
	 *
	 * @param old
	 */
	public void swapContentWith(PrintItem old) {
		if (old == null) {
			return;
		}
		String tempType = this.type;
		int tempPhotoId = this.photoId;
		String tempPhotoFile = this.photoFile;
		int tempTextId = this.textId;
		String tempText = this.text;
		String tempComment = this.comment;
		this.type = old.type;
		this.photoId = old.photoId;
		this.photoFile = old.photoFile;
		this.textId = old.textId;
		this.text = old.text;
		this.comment = old.comment;
		old.type = tempType;
		old.photoId = tempPhotoId;
		old.photoFile = tempPhotoFile;
		old.textId = tempTextId;
		old.text = tempText;
		old.comment = tempComment;
	}

	/**
	 * get the POSITION as a String
	 *
	 * @return
	 */
	public String posGet() {
		return pos.toString();
	}

	public int posNumGet() {
		return pos.cellNumGet();
	}

	/**
	 * Get the cell number
	 *
	 * @return
	 */
	public int cellNumGet() {
		return pos.num;
	}

	/**
	 * Set the cell number
	 *
	 * @param value
	 */
	public void cellNumSet(int value) {
		this.pos.cellNumSet(value);
	}

	/**
	 * Sort the given List by page and cell number
	 *
	 * @param cells
	 */
	public static void sortByPage(List<PrintItem> cells) {
		if (cells == null || cells.isEmpty()) {
			return;
		}

		cells.sort(Comparator.comparingInt(PrintItem::pageGet)
				.thenComparingInt(PrintItem::posNumGet)
		);
	}

	/**
	 * Sort the given List by ID
	 *
	 * @param cells
	 */
	public static void sortById(List<PrintItem> cells) {
		if (cells == null || cells.isEmpty()) {
			return;
		}

		cells.sort(Comparator.comparingInt(PrintItem::idGet)
				.thenComparingInt(PrintItem::idGet)
		);
	}

	/**
	 * class to manage position data (cell number, span H and V)
	 */
	private static class POSITION {

		int num, H, V;

		public POSITION() {
			this.num = 0;
			this.H = 0;
			this.V = 0;
		}

		/**
		 * get the cell number
		 *
		 * @return
		 */
		public int cellNumGet() {
			return num;
		}

		/**
		 * set the cell number
		 *
		 * @param cell
		 */
		public void cellNumSet(int cell) {
			this.num = cell;
		}

		/**
		 * get the span H
		 *
		 * @return
		 */
		public int spanHGet() {
			return H;
		}

		/**
		 * set the span H and V
		 *
		 * @param h
		 * @param v
		 */
		public void spanSet(int h, int v) {
			H = h;
			V = v;
		}

		/**
		 * set the span H
		 *
		 * @param spanH
		 */
		public void spanHSet(int spanH) {
			this.H = spanH;
		}

		/**
		 * get the span V
		 *
		 * @return
		 */
		public int getSpanV() {
			return V;
		}

		/**
		 * set the span V
		 *
		 * @param spanV
		 */
		public void setSpanV(int spanV) {
			this.V = spanV;
		}

		/**
		 * set the span value from a String "H,V"
		 *
		 * @param span
		 */
		private void setSpan(String value) {
			if (value == null || value.isEmpty()) {
				H = 0;
				V = 0;
			} else {
				String p[] = value.split(",");
				if (p.length > 1) {
					H = Integer.parseInt(p[0].trim());
					V = Integer.parseInt(p[1].trim());
				}
			}
		}

		/**
		 * get the String value of this Position
		 *
		 * @return
		 */
		@Override
		public String toString() {
			return String.format("%d,%d,%d", num, H, V);
		}

	}

}
