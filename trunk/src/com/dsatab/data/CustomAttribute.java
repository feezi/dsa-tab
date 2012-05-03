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

import com.dsatab.common.Util;
import com.dsatab.data.enums.AttributeType;

/**
 * @author Ganymede
 * 
 */
public class CustomAttribute extends Attribute implements JSONable {

	private static final String FIELD_NAME = "name";

	private static final String FIELD_VALUE = "value";

	private static final String FIELD_REFERENCE_VALUE = "refValue";

	private static final String FIELD_ERSCHWERNIS = "erschwernis";

	private static final String FIELD_TYPE = "type";

	private String name;

	private Integer value;

	/**
	 * @param element
	 * @param hero
	 */
	public CustomAttribute(Hero hero) {
		super(null, hero);
	}

	/**
	 * @param element
	 * @param hero
	 */
	public CustomAttribute(Hero hero, JSONObject json) throws JSONException {
		super(null, AttributeType.valueOf(json.getString(FIELD_TYPE)), hero);

		this.name = json.getString(FIELD_NAME);
		if (json.has(FIELD_VALUE))
			this.value = json.getInt(FIELD_VALUE);
		if (json.has(FIELD_ERSCHWERNIS))
			this.probeInfo.setErschwernis(json.getInt(FIELD_ERSCHWERNIS));
		if (json.has(FIELD_REFERENCE_VALUE))
			this.referenceValue = json.getInt(FIELD_REFERENCE_VALUE);
	}

	/**
	 * @param element
	 * @param type
	 * @param hero
	 */
	public CustomAttribute(Hero hero, AttributeType type) {
		super(null, type, hero);

		this.name = type.name();

		if (type == AttributeType.Ausweichen)
			probeInfo.setErschwernis(0);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getValue() {
		if (value != null)
			return value;
		else
			return getCoreValue();
	}

	public void setValue(Integer value) {
		Integer oldValue = getValue();
		this.value = value;

		if (!Util.equalsOrNull(oldValue, value))
			hero.fireValueChangedEvent(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.Attribute#getReferenceValue()
	 */
	@Override
	public Integer getReferenceValue() {
		if (referenceValue == null)
			referenceValue = getCoreValue();

		return referenceValue;
	}

	private Integer getCoreValue() {
		Integer value = null;

		switch (type) {
		case Ausweichen:

			value = 0;
			if (hero.hasFeature(SpecialFeature.AUSWEICHEN_1))
				value += 3;
			if (hero.hasFeature(SpecialFeature.AUSWEICHEN_2))
				value += 3;
			if (hero.hasFeature(SpecialFeature.AUSWEICHEN_3))
				value += 3;

			Talent athletik = hero.getTalent(Talent.ATHLETIK);
			if (athletik != null && athletik.getValue() >= 9) {
				value += (athletik.getValue() - 9) / 3;
			}

			return value + getBaseValue();

		case Geschwindigkeit:
			return getBaseValue();
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.Attribute#getBaseValue()
	 */
	@Override
	public int getBaseValue() {
		if (type == AttributeType.Geschwindigkeit) {
			int value = 7;

			int ge = hero.getAttributeValue(AttributeType.Gewandtheit);
			if (ge >= 16)
				value += 2;
			else if (ge >= 11)
				value += 1;

			if (hero.hasFeature(SpecialFeature.FLINK))
				value += 1;
			if (hero.hasFeature(SpecialFeature.BEHAEBIG))
				value -= 1;
			if (hero.hasFeature(SpecialFeature.EINBEINIG))
				value -= 3;
			if (hero.hasFeature(SpecialFeature.KLEINWUECHSIG))
				value -= 1;
			if (hero.hasFeature(SpecialFeature.LAHM))
				value -= 1;
			if (hero.hasFeature(SpecialFeature.ZWERGENWUCHS))
				value -= 2;

			if (this.originalBaseValue == null)
				this.originalBaseValue = value;

			return value;
		} else
			return super.getBaseValue();
	}

	/**
	 * Constructs a json object with the current data
	 * 
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJSONObject() throws JSONException {
		JSONObject out = new JSONObject();

		out.put(FIELD_NAME, name);
		out.put(FIELD_TYPE, type.name());
		out.put(FIELD_VALUE, value);
		out.put(FIELD_REFERENCE_VALUE, referenceValue);
		out.put(FIELD_ERSCHWERNIS, probeInfo.getErschwernis());

		return out;
	}

}
