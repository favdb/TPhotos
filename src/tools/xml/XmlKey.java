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
package tools.xml;

/**
 * dictionary of XML tag names
 *
 * @author favdb
 */
public class XmlKey {

	public enum XK {
		//common
		ID,
		NAME,
		ASPECT,
		CREATION,
		MAJ,
		UPDATE,
		DESCRIPTION,
		NOTES,
		ASSISTANT,
		// entity(s) name(s)
		CATEGORIES,
		CATEGORY,
		CHAPTER,
		ENDNOTE,
		EPISODE,
		EVENT,
		GENDER,
		IDEA,
		ITEM, ITEMS,
		LOCATION, LOCATIONS,
		MEMO,
		PART,
		PERSON, PERSONS,
		PLOT, PLOTS,
		RELATION, RELATIONS,
		SCENE,
		STATUS,
		STRAND, STRANDS,
		TAG, TAGS,
		//datas
		ABBREVIATION,
		ADDRESS,
		ADOLESCENCE,
		ADULTHOOD,
		ALTITUDE,
		ATTRIBUT,
		BIRTHDAY,
		CHILDHOOD,
		CITY,
		COLOR,
		COUNTRY,
		DATE,
		DEATH,
		DURATION,
		FILE,
		FIRSTNAME,
		GPS,
		ICONE,
		INFORMATIVE,
		INTENSITY,
		KEY,
		LASTNAME,
		NARRATOR,
		NUMBER,
		OBJECTIVECHARS, OBJECTIVEDATE, OBJECTIVEDONE,
		OCCUPATION,
		RELATIVESCENE, RELATIVETIME,
		RETIREMENT,
		SORT,
		STAGE,
		STEP,
		SUP,
		TEXT,
		TIME,
		TYPE,
		UUID,
		VALUE,
		//scenario
		LOC,
		MOMENT,
		PITCH,
		START,
		END,
		//challenge
		DAYS, WORDS, INITDATE, LASTDATE, INITWORDS, LASTWORDS, VALUES;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

}
