/**
 *  This file is part of DsaTab.
 *
 *  DsaTab is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DsaTab is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DsaTab.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dsatab.data;

import com.dsatab.common.Util;

/**
 * @author Ganymede
 * 
 */
public class LiturgieInfo {

	private static final String[] COSTS = { "2 KaP", "5 KaP", "10 KaP", "15 KaP", "20 KaP", "25 KaP/ 1 pKap",
			"30 KaP/ 3 pKaP", "35 Kap/ 5 pKap", "40 KaP/ 7 pKaP", "45 KaP/ 9 pKaP" };
	private String name;

	private int grade;

	private String target;

	private String range;

	private String castDuration;

	private String effect;

	private String effectDuration;

	private String origin;

	private String source;

	/**
	 * 
	 */
	public LiturgieInfo() {

	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		return name + " " + Util.intToGrade(grade);
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}

	public String getTarget() {
		return target;
	}

	public String getTargetDetailed() {

		if (target.equals("G"))
			return "der Geweihter selbst";
		else if (target.equals("P"))
			return "einzelne Zielperson oder Objekt";
		else if (target.equals("PP"))
			return "bis zu 10 Personen/Objekte";
		else if (target.equals("PPP"))
			return "10 bis 100 Personen/Objekte";
		else if (target.equals("Z"))
			return "Zone von ca. 10 Schritt Radius";
		else if (target.equals("ZZ"))
			return "Zone von ca. 30 Schritt Radius";
		else if (target.equals("ZZZ"))
			return "Zone von ca. 100 Schritt Radius";
		else
			return target;
	}

	public String getCosts() {
		if (grade >= 0)
			return COSTS[grade];
		else
			return "";
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getRange() {
		return range;
	}

	public String getRangeDetailed() {

		if (range.equals("s"))
			return "der Geweihte selbst";
		else if (range.equals("B"))
			return "Berührung";
		else if (range.equals("s,B"))
			return "der Geweithe selbst oder Berührung";
		else if (range.equals("F"))
			return "Wirkung tritt an einemn frei wählbaren Ort ein";
		else if (range.equals("S"))
			return "beliebiger Ort innerhalb des Sichtfeldes";
		else
			return range;
	}

	public void setRange(String range) {
		this.range = range;
	}

	public String getCastDuration() {
		return castDuration;
	}

	public String getCastDurationDetailed() {
		if (castDuration.equals("G"))
			return "Gebeht (ca. 1 Spielrunde)";
		else if (castDuration.equals("A"))
			return "Andacht (ca. halbe Stunde)";
		else if (castDuration.equals("Ze"))
			return "Zeremonie (mehrere Stunden)";
		else if (castDuration.equals("Zy"))
			return "Zyklus (an mehrere Tagen wiederholende Andachten)";
		else if (castDuration.startsWith("S")) {
			return "Stoßgebet (" + castDuration + " Aktionen)";
		} else
			return castDuration;
	}

	public void setCastDuration(String castDuration) {
		this.castDuration = castDuration;
	}

	public String getEffect() {
		return effect;
	}

	public void setEffect(String effect) {
		this.effect = effect;
	}

	public String getEffectDuration() {
		return effectDuration;
	}

	public void setEffectDuration(String effectDuration) {
		this.effectDuration = effectDuration;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

}
