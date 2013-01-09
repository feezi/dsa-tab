package com.dsatab.data;

import com.dsatab.DSATabApplication;
import com.dsatab.activity.BasePreferenceActivity;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.enums.Position;

public class CombatDistanceTalent extends BaseCombatTalent implements Value {

	private CombatTalentType type;

	private Integer referenceValue;

	public CombatDistanceTalent(Hero hero) {
		super(hero);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.Talent#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		super.setName(name);
		setCombatTalentType(CombatTalentType.byName(name));
	}

	/**
	 * @param type
	 */
	private void setCombatTalentType(CombatTalentType type) {
		this.type = type;
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
		if (referenceValue == null)
			this.referenceValue = getValue();
		return referenceValue;
	}

	public Integer getValue() {
		if (value != null) {
			return value + getBaseValue();
		} else
			return null;
	}

	public int getBaseValue() {
		int baseValue = 0;

		if (type == CombatTalentType.Lanzenreiten)
			baseValue = hero.getAttributeValue(AttributeType.at);
		else if (type.isFk())
			baseValue = hero.getAttributeValue(AttributeType.fk);

		return baseValue;
	}

	public void setValue(Integer value) {
		if (this.value != value) {
			this.value = value;

			hero.fireValueChangedEvent(this);
		}
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
