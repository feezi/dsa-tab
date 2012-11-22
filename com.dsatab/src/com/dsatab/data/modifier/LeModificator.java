package com.dsatab.data.modifier;

import com.dsatab.data.Attribute;
import com.dsatab.data.CombatDistanceTalent;
import com.dsatab.data.CombatMeleeAttribute;
import com.dsatab.data.CombatProbe;
import com.dsatab.data.CombatShieldTalent;
import com.dsatab.data.Hero;
import com.dsatab.data.Modifier;
import com.dsatab.data.Probe;
import com.dsatab.data.Spell;
import com.dsatab.data.Talent;
import com.dsatab.data.enums.AttributeType;

public class LeModificator extends AbstractModificator {

	public static final float LEVEL_1 = 0.5f;
	public static final float LEVEL_2 = 0.33f;
	public static final float LEVEL_3 = 0.25f;

	public LeModificator(Hero hero) {
		super(hero);
	}

	@Override
	public String getModificatorName() {

		float ratio = hero.getRatio(AttributeType.Lebensenergie);
		if (ratio < LEVEL_3) {
			return "Lebensenergie < 1/4";
		} else if (ratio < LEVEL_2) {
			return "Lebensenergie < 1/3";
		} else if (ratio < LEVEL_1) {
			return "Lebensenergie < 1/2";
		} else {
			return null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.modifier.AbstractModificator#isActive()
	 */
	@Override
	public boolean isActive() {
		return hero.getHeroConfiguration().isLeModifierActive();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.modifier.AbstractModificator#setActive(boolean)
	 */
	@Override
	public void setActive(boolean active) {
		hero.getHeroConfiguration().setLeModifierActive(active);
		super.setActive(active);
	}

	@Override
	public String getModificatorInfo() {
		String info = null;
		float ratio = hero.getRatio(AttributeType.Lebensenergie);
		if (ratio < LEVEL_3) {
			info = "Eigenschaften, Kampf -3; Talente,Zauber -9; GS -3";
		} else if (ratio < LEVEL_2) {
			info = "Eigenschaften, Kampf -2; Talente,Zauber -6; GS -2";
		} else if (ratio < LEVEL_1) {
			info = "Eigenschaften, Kampf -1; Talente,Zauber -3; GS -1";
		} else
			info = "";
		return info;
	}

	@Override
	public Modifier getModifier(Probe probe) {
		if (isActive()) {
			int modifier = 0;
			float ratio = hero.getRatio(AttributeType.Lebensenergie);

			if (probe instanceof Attribute) {
				Attribute attribute = (Attribute) probe;
				return getModifier(attribute.getType());
			} else if (probe instanceof Talent || probe instanceof Spell) {

				if (ratio < LEVEL_3) {
					modifier = -9;
				} else if (ratio < LEVEL_2) {
					modifier = -6;
				} else if (ratio < LEVEL_1) {
					modifier = -3;
				}
			} else if (probe instanceof CombatProbe || probe instanceof CombatShieldTalent
					|| probe instanceof CombatDistanceTalent || probe instanceof CombatMeleeAttribute) {
				if (ratio < LEVEL_3) {
					modifier = -3;
				} else if (ratio < LEVEL_2) {
					modifier = -2;
				} else if (ratio < LEVEL_1) {
					modifier = -1;
				}
			}

			this.modifier.setModifier(modifier);
			this.modifier.setTitle(getModificatorName());
			this.modifier.setDescription(getModificatorInfo());
			return this.modifier;
		} else {
			return null;
		}
	}

	@Override
	public Modifier getModifier(AttributeType type) {
		if (isActive()) {
			int modifier = 0;
			float ratio = hero.getRatio(AttributeType.Lebensenergie);

			if (ratio < LEVEL_3) {
				if (AttributeType.isEigenschaft(type) || AttributeType.isFight(type)) {
					modifier = -3;
				}
			} else if (ratio < LEVEL_2) {
				if (AttributeType.isEigenschaft(type) || AttributeType.isFight(type)) {
					modifier = -2;
				}
			} else if (ratio < LEVEL_1) {
				if (AttributeType.isEigenschaft(type) || AttributeType.isFight(type)) {
					modifier = -1;
				}
			}

			this.modifier.setModifier(modifier);
			this.modifier.setTitle(getModificatorName());
			this.modifier.setDescription(getModificatorInfo());
			return this.modifier;
		} else {
			return null;
		}
	}

}
