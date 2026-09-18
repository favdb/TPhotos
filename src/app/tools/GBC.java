/*
 * Copyright (C) 2026 favdb
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
package app.tools;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * short way to write GridBagConstraints
 *
 * @author favdb
 */
public class GBC extends GridBagConstraints {

	/**
	 * Création du GBC
	 *
	 * @param y : nombre de lignes
	 * @param x : nombre de colonnes
	 */
	public GBC(int y, int x) {
		super();
		gridy = y;
		gridx = x;
	}

	/**
	 * Création du GBC
	 *
	 * @param y : numéro de lignes
	 * @param x : numéro de colonnes
	 * @fill	: valeur initiale du paramètre fill
	 */
	public GBC(int y, int x, int fill) {
		this(y, x);
		this.fill = fill;
	}

	/**
	 * Constructeur en mode textuel
	 *
	 * paramétrage explicite des contraintes commence toujours par les valeur gridy et
	 * gridx separateurs : espace pour séparer les paramètres, virgule pour séparer les
	 * valeurs eventuelles
	 *
	 * Remarques:
	 * <ul>
	 * <li>- anchor (forme abrégé anc) : accepte les valeurs (casse indifférente): c ou
	 * center n ou north ne ou northeast nw ou northwest e ou east w ou west s ou south se
	 * ou southeast sw ou southwest ps ou page_start pe ou page_end ls ou line_start le ou
	 * line_end fls ou first_line_start fle ou first_line_end lls ou last_line_start lle
	 * ou last_line_end": </li>
	 * <li>- fill accepte les valeurs (casse indifférente): n ou none h ou horizontal v ou
	 * vertical b ou both </li>
	 * <li>- insets (forme abrégée ins) peut s'écrire de trois manière: insets est
	 * équivalent à insets 0 0 0 0 insets x est équivalent à insets x x x x insets x1 x2
	 * x3 x4 </li>
	 * <li>- width (forme abégée gw) est équivalent à gridwidth </li>
	 * <li>- height (forme abrégée gh) est équivalent à gridheight </li>
	 * <li>- wx est équivalent à weightx </li>
	 * <li>- wy est équivalent à weighty</li>
	 *
	 * @param param : texte de paramétrage
	 */
	public GBC(String param) {
		super();
		String x[] = param.split(",");
		gridy = toInteger(x[0].trim());
		gridx = toInteger(x[1].trim());
		for (int i = 2; i < x.length; i++) {
			String s = x[i].trim();
			String co[] = s.split(" ");
			switch (co[0]) {
				case "anchor":
					switch (co[1].trim().toLowerCase()) {
						case "c":
						case "center":
							anchor = GridBagConstraints.CENTER;
							break;
						case "n":
						case "north":
							anchor = GridBagConstraints.NORTH;
							break;
						case "ne":
						case "northeast":
							anchor = GridBagConstraints.NORTHEAST;
							break;
						case "nw":
						case "northwest":
							anchor = GridBagConstraints.NORTHWEST;
							break;
						case "w":
						case "west":
							anchor = GridBagConstraints.WEST;
							break;
						case "s":
						case "south":
							anchor = GridBagConstraints.SOUTH;
							break;
						case "se":
						case "southeast":
							anchor = GridBagConstraints.SOUTHEAST;
							break;
						case "sw":
						case "southwest":
							anchor = GridBagConstraints.SOUTHWEST;
							break;
						case "e":
						case "east":
							anchor = GridBagConstraints.EAST;
							break;
						case "ps":
						case "page_start":
							anchor = GridBagConstraints.PAGE_START;
							break;
						case "pe":
						case "page_end":
							anchor = GridBagConstraints.PAGE_END;
							break;
						case "ls":
						case "line_start":
							anchor = GridBagConstraints.LINE_START;
							break;
						case "le":
						case "line_end":
							anchor = GridBagConstraints.LINE_END;
							break;
						case "fls":
						case "first_line_start":
							anchor = GridBagConstraints.FIRST_LINE_START;
							break;
						case "fle":
						case "first_line_end":
							anchor = GridBagConstraints.FIRST_LINE_END;
							break;
						case "lls":
						case "last_line_start":
							anchor = GridBagConstraints.LAST_LINE_START;
							break;
						case "lle":
						case "last_line_end":
							anchor = GridBagConstraints.LAST_LINE_END;
							break;
						default:
							break;
					}
					break;
				case "fill":
					switch (co[1].trim().toLowerCase()) {
						case "n":
						case "none":
							fill = GridBagConstraints.NONE;
							break;
						case "h":
						case "horizontal":
							fill = GridBagConstraints.HORIZONTAL;
							break;
						case "v":
						case "vertical":
							fill = GridBagConstraints.VERTICAL;
							break;
						case "b":
						case "both":
							fill = GridBagConstraints.BOTH;
							break;
						default:
							break;
					}
					break;
				case "baseline":
					anchor = GridBagConstraints.BASELINE;
					break;
				case "center":
					anchor = GridBagConstraints.CENTER;
					break;
				case "top":
					anchor = GridBagConstraints.NORTH;
					break;
				case "bottom":
					anchor = GridBagConstraints.SOUTH;
					break;
				case "left":
					anchor = GridBagConstraints.WEST;
					break;
				case "right":
					anchor = GridBagConstraints.EAST;
					break;
				case "grow":
					fill = GridBagConstraints.BOTH;
					break;
				case "growx":
					fill = GridBagConstraints.HORIZONTAL;
					break;
				case "growy":
					fill = GridBagConstraints.VERTICAL;
					break;
				case "width":
				case "gridwidth":
				case "gw":
					gridwidth = Integer.valueOf(co[1].trim());
					break;
				case "height":
				case "gridheight":
				case "gh":
					gridheight = Integer.valueOf(co[1].trim());
					break;
				case "ins":
				case "insets":
					if (co.length < 2) {
						insets = new Insets(0, 0, 0, 0);
					} else if (co.length < 3) {
						int z = toInteger(co[1]);
						insets = new Insets(z, z, z, z);
					} else {
						insets = new Insets(
								toInteger(co[1]),
								toInteger(co[2]),
								toInteger(co[3]),
								toInteger(co[4]));
					}
					break;
				case "ipadx":
					ipadx = toInteger(co[1].trim());
					break;
				case "ipady":
					ipady = toInteger(co[1].trim());
					break;
				case "wx":
				case "weightx":
					weightx = Double.valueOf(co[1].trim());
					break;
				case "wy":
				case "weighty":
					weighty = Double.valueOf(co[1].trim());
					break;
				default:
					break;
			}
		}
	}

	/**
	 * conversion d'un String en son équivalent en Integer
	 *
	 * @param s	: texte représentant un entier
	 * @return
	 */
	private Integer toInteger(String s) {
		return Integer.parseInt(s.trim());
	}

}
