package com.dsatab.data;

import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.enums.Position;

public class CombatShieldTalent extends BaseCombatTalent implements Probe {

	protected Hero hero;

	protected CombatTalentType type;

	protected Integer value;

	public CombatShieldTalent(Hero hero) {
		super(hero, null, null);
		this.hero = hero;
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

		this.value = value;
	}

	public String getName() {
		return "Schildparade";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.BaseCombatTalent#isFavorite()
	 */
	@Override
	public boolean isFavorite() {
		// TODO Auto-generated method stub
		return super.isFavorite();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.BaseCombatTalent#setFavorite(boolean)
	 */
	@Override
	public void setFavorite(boolean value) {
		// TODO Auto-generated method stub
		super.setFavorite(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.BaseCombatTalent#isUnused()
	 */
	@Override
	public boolean isUnused() {
		// TODO Auto-generated method stub
		return super.isUnused();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.BaseCombatTalent#setUnused(boolean)
	 */
	@Override
	public void setUnused(boolean value) {
		// TODO Auto-generated method stub
		super.setUnused(value);
	}

	@Override
	public Integer getErschwernis() {
		return null;
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

	public void setType(CombatTalentType type) {
		this.type = type;
	}

	public String getBe() {
		return type.getBe();
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

	public Integer getValue() {

		if (this.value != null) {
			return this.value + getBaseValue();
		} else
			return null;
	}

	protected int getBaseValue() {
		int baseValue = 0;

		if (hero != null) {
			baseValue = hero.getAttributeValue(AttributeType.pa);
		}

		return baseValue;
	}

	public Position getPosition(int w20) {
		return Position.box_rauf_hruru[w20];
	}

	@Override
	public String toString() {
		return getName();
	}
}
