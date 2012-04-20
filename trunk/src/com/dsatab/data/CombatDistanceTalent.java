package com.dsatab.data;

import org.jdom.Element;

import com.dsatab.DSATabApplication;
import com.dsatab.activity.BasePreferenceActivity;
import com.dsatab.common.Util;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.enums.Position;
import com.dsatab.xml.Xml;

public class CombatDistanceTalent extends BaseCombatTalent implements Value {

	private Element element;

	private Hero hero;

	private CombatTalentType type;

	private Integer referenceValue;

	public CombatDistanceTalent(Hero hero, Element element) {
		super(hero, element, null);
		this.hero = hero;
		this.element = element;
		this.type = CombatTalentType.byName(getName());
		this.referenceValue = getValue();

		probeInfo.applyBePattern(type.getBe());
	}

	public Probe getAttack() {
		return this;
	}

	public Probe getDefense() {
		return null;
	}

	public CombatTalentType getCombatTalentType() {
		return type;
	}

	public int getMinimum() {
		return 0;
	}

	public int getMaximum() {
		return 32;
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
		if (element.getAttribute(Xml.KEY_VALUE) != null) {
			return Util.parseInt(element.getAttributeValue(Xml.KEY_VALUE)) + getBaseValue();
		} else
			return null;
	}

	public int getBaseValue() {
		int baseValue = 0;

		if (hero != null) {
			if (type == CombatTalentType.Lanzenreiten)
				baseValue = hero.getAttributeValue(AttributeType.at);
			else if (type.isFk())
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
		if (DSATabApplication.getPreferences().getBoolean(BasePreferenceActivity.KEY_HOUSE_RULES_MORE_TARGET_ZONES,
				false)) {
			return Position.fern_wurf[w20];
		} else {
			return Position.official[w20];
		}
	}

	@Override
	public String toString() {
		return getName();
	}
}