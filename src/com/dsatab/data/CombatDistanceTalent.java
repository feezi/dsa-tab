package com.dsatab.data;

import org.w3c.dom.Element;

import com.dsatab.common.Util;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.enums.Position;
import com.dsatab.xml.Xml;

public class CombatDistanceTalent implements Probe, Value, CombatTalent, Markable {

	private Element element;

	private Hero hero;

	private CombatTalentType type;

	private Integer referenceValue;

	public CombatDistanceTalent(Hero hero, Element element) {
		this.hero = hero;
		this.element = element;
		this.type = CombatTalentType.byName(getName());
		this.referenceValue = getValue();
	}

	public String getName() {
		return element.getAttribute(Xml.KEY_NAME);
	}

	@Override
	public Integer getErschwernis() {
		return null;
	}

	public Probe getAttack() {
		return this;
	}

	public Probe getDefense() {
		return null;
	}

	public CombatTalentType getType() {
		return type;
	}

	public boolean isFavorite() {
		if (element.hasAttribute(Xml.KEY_FAVORITE)) {
			return Boolean.valueOf(element.getAttribute(Xml.KEY_FAVORITE));
		} else {
			return false;
		}
	}

	public boolean isUnused() {
		if (element.hasAttribute(Xml.KEY_UNUSED)) {
			return Boolean.valueOf(element.getAttribute(Xml.KEY_UNUSED));
		} else {
			return false;
		}
	}

	public void setFavorite(boolean value) {
		if (value)
			element.setAttribute(Xml.KEY_FAVORITE, Boolean.TRUE.toString());
		else
			element.removeAttribute(Xml.KEY_FAVORITE);
	}

	public void setUnused(boolean value) {
		if (value)
			element.setAttribute(Xml.KEY_UNUSED, Boolean.TRUE.toString());
		else
			element.removeAttribute(Xml.KEY_UNUSED);
	}

	public String getBe() {
		return type.getBe();
	}

	public int getMinimum() {
		return 0;
	}

	public int getMaximum() {
		return 32;
	}

	public String getProbe() {
		return null;
	}

	@Override
	public ProbeType getProbeType() {
		return ProbeType.TwoOfThree;
	}

	public Integer getProbeValue(int i) {
		return getValue();
	}

	@Override
	public Integer getProbeBonus() {
		return null;
	}

	public Integer getReferenceValue() {
		return referenceValue;
	}

	public Integer getValue() {

		if (element.hasAttribute(Xml.KEY_VALUE)) {
			return Util.parseInt(element.getAttribute(Xml.KEY_VALUE)) + getBaseValue();
		} else
			return null;
	}

	public int getBaseValue() {
		int baseValue = 0;

		if (hero != null) {
			if (type.isFk())
				baseValue = hero.getAttributeValue(AttributeType.fk);
		}

		return baseValue;
	}

	public void setValue(Integer value) {
		if (value != null)
			element.setAttribute(Xml.KEY_VALUE, Util.toString(value - getBaseValue()));
		else
			element.removeAttribute(Xml.KEY_VALUE);

		hero.fireValueChangedEvent(this);
	}

	public Position getPosition(int w20) {
		return Position.fern_wurf[w20];
	}

	@Override
	public String toString() {
		return getName();
	}
}
