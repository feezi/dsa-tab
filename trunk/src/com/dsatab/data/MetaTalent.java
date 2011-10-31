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

import org.json.JSONException;
import org.json.JSONObject;

import com.dsatab.common.DsaMath;
import com.dsatab.data.items.EquippedItem;
import com.gandulf.guilib.util.Debug;

/**
 * @author Ganymede
 * 
 */
public class MetaTalent extends Talent implements JSONable {

	enum MetaTalentType {
		PirschAnsitzJagd("Pirsch- und Ansitzjagd"), NahrungSammeln("Nahrung sammeln"), Kräutersuchen("Kräutersuche"), Wache(
				"Wache halten");

		/**
		 * 
		 */
		private MetaTalentType(String name) {
			this.name = name;
		}

		private String name;

		public String getName() {
			return name;
		}
	}

	private static final String FIELD_META_TYPE = "metaType";

	private static final String FIELD_FAVORITE = "favorite";

	private static final String FIELD_UNUSED = "unused";

	private MetaTalentType metaType;

	private boolean favorite, unused;

	public MetaTalent(Hero hero, MetaTalentType type) {
		super(hero, null);

		this.metaType = type;
	}

	public MetaTalent(Hero hero, JSONObject json) throws JSONException {
		super(hero, null);

		this.metaType = MetaTalentType.valueOf(json.getString(FIELD_META_TYPE));
		this.favorite = json.getBoolean(FIELD_FAVORITE);
		this.unused = json.getBoolean(FIELD_UNUSED);

		switch (metaType) {
		case PirschAnsitzJagd:
			probeInfo = ProbeInfo.parse("(MU/IN/GE)");
			break;
		case Kräutersuchen:
		case NahrungSammeln:
			probeInfo = ProbeInfo.parse("(MU/IN/FF)");
			break;
		case Wache:
			probeInfo = ProbeInfo.parse("(MU/IN/KO)");
			break;
		default:
			probeInfo = new ProbeInfo();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.Talent#getName()
	 */
	@Override
	public String getName() {
		return metaType.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.Talent#getValue()
	 */
	@Override
	public Integer getValue() {
		return getProbeBonus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.Talent#getProbeBonus()
	 */
	@Override
	public Integer getProbeBonus() {

		switch (metaType) {
		case PirschAnsitzJagd: {
			Integer wildnis = getTalentValue(Talent.WILDNISLEBEN);
			Integer fährtensuche = getTalentValue(Talent.FÄHRTENSUCHEN);
			Integer schleichen = getTalentValue(Talent.SCHLEICHEN);
			Integer tierkunde = getTalentValue(Talent.TIERKUNDE);
			Integer distance = 0;

			EquippedItem huntingWeapon = hero.getHuntingWeapon();
			if (huntingWeapon != null) {
				if (huntingWeapon.getTalent() instanceof CombatDistanceTalent) {
					CombatDistanceTalent distanceTalent = (CombatDistanceTalent) huntingWeapon.getTalent();
					// we want only the acutal talent value without the FK base
					distance = distanceTalent.getValue() - distanceTalent.getBaseValue();
				}
			}

			Debug.verbose("Wild " + wildnis + " färten " + fährtensuche + " schleich " + schleichen + " tierk "
					+ tierkunde + " fernk " + distance);

			Integer minValue = DsaMath.min(wildnis, fährtensuche, schleichen, tierkunde, distance);

			Debug.verbose("Minium value is " + minValue);
			int value = DsaMath.sum(wildnis, fährtensuche, schleichen, tierkunde, distance) / 5;

			Debug.verbose("Sum value/5 is " + value);
			if (minValue == null)
				return null;
			else {
				value = Math.min(minValue * 2, value);
			}
			return value;
		}
		case Kräutersuchen: {
			Integer wildnis = getTalentValue(Talent.WILDNISLEBEN);
			Integer sinnen = getTalentValue(Talent.SINNENSCHÄRFE);
			Integer planzen = getTalentValue(Talent.PFLANZENKUNDE);

			Integer minValue = DsaMath.min(wildnis, sinnen, planzen);
			int value = DsaMath.sum(wildnis, sinnen, planzen) / 3;

			if (minValue == null)
				return null;
			else {
				value = Math.min(minValue * 2, value);
			}
			return value;
		}
		case NahrungSammeln: {
			Integer wildnis = getTalentValue(Talent.WILDNISLEBEN);
			Integer sinnen = getTalentValue(Talent.SINNENSCHÄRFE);
			Integer planzen = getTalentValue(Talent.PFLANZENKUNDE);

			Integer minValue = DsaMath.min(wildnis, sinnen, planzen);
			int value = DsaMath.sum(wildnis, sinnen, planzen) / 3;

			if (minValue == null)
				return null;
			else {
				value = Math.min(minValue * 2, value);
			}
			return value;
		}
		case Wache:
			Integer selbst = getTalentValue(Talent.SELBSTBEHERRSCHUNG);
			Integer sinnen = getTalentValue(Talent.SINNENSCHÄRFE);
			Integer schleichen = getTalentValue(Talent.SCHLEICHEN);
			Integer verstecken = getTalentValue(Talent.SICH_VERSTECKEN);
			Integer wildnis = getTalentValue(Talent.WILDNISLEBEN);

			Debug.verbose("selbst " + selbst + " sinnen " + sinnen + " schleich " + schleichen + " versteck "
					+ verstecken + " wildn " + wildnis);

			Integer minValue = DsaMath.min(selbst, sinnen, schleichen, verstecken, wildnis);
			int value = DsaMath.sum(selbst, selbst, selbst, sinnen, sinnen, sinnen, sinnen, schleichen, verstecken,
					wildnis) / 10;

			if (minValue == null)
				return null;
			else {
				value = Math.min(minValue * 2, value);
			}
			return value;

		default:
			return null;
		}
	}

	private int getTalentValue(String talentName) {
		Talent talent = hero.getTalent(talentName);

		if (talent == null)
			return 0;
		else
			return talent.getValue();
	}

	public MetaTalentType getMetaType() {
		return metaType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.Talent#getProbeType()
	 */
	@Override
	public ProbeType getProbeType() {
		return ProbeType.ThreeOfThree;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.MarkableElement#setFavorite(boolean)
	 */
	@Override
	public void setFavorite(boolean value) {
		this.favorite = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.MarkableElement#setUnused(boolean)
	 */
	@Override
	public void setUnused(boolean value) {
		this.unused = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.MarkableElement#isFavorite()
	 */
	@Override
	public boolean isFavorite() {
		return favorite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.MarkableElement#isUnused()
	 */
	@Override
	public boolean isUnused() {
		return unused;
	}

	/**
	 * Constructs a json object with the current data
	 * 
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJSONObject() throws JSONException {
		JSONObject out = new JSONObject();

		out.put(FIELD_META_TYPE, metaType.name());
		out.put(FIELD_UNUSED, unused);
		out.put(FIELD_FAVORITE, favorite);

		return out;
	}

}
