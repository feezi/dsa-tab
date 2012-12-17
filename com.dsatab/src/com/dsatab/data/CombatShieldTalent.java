package com.dsatab.data;

import com.dsatab.DSATabApplication;
import com.dsatab.activity.BasePreferenceActivity;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.enums.Position;

public class CombatShieldTalent extends BaseCombatTalent {

	protected CombatTalentType type;

	public CombatShieldTalent(Hero hero) {
		super(hero);
		this.type = CombatTalentType.Raufen;

		int value = 0;

		if (hero.hasFeature(SpecialFeature.LINKHAND))
			value += 1;
		if (hero.hasFeature(SpecialFeature.SCHILDKAMPF_1))
			value += 2;
		if (hero.hasFeature(SpecialFeature.SCHILDKAMPF_2))
			value += 2;
		if (hero.hasFeature(SpecialFeature.SCHILDKAMPF_3))
			value += 2;

		this.probeInfo.applyBePattern(type.getBe());

		this.value = value;
	}

	public String getName() {
		return "Schildparade";
	}

	public Probe getAttack() {
		return null;
	}

	public Probe getDefense() {
		return this;
	}

	public CombatTalentType getCombatTalentType() {
		return type;
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

	public Integer getValue() {

		if (this.value != null) {
			return this.value + getBaseValue();
		} else
			return null;
	}

	protected int getBaseValue() {
		return hero.getAttributeValue(AttributeType.pa);
	}

	public Position getPosition(int w20) {
		if (DSATabApplication.getPreferences().getBoolean(BasePreferenceActivity.KEY_HOUSE_RULES_MORE_TARGET_ZONES,
				false)) {
			return Position.box_rauf_hruru[w20];
		} else {
			return Position.official[w20];
		}
	}

	@Override
	public String toString() {
		return getName();
	}
}
