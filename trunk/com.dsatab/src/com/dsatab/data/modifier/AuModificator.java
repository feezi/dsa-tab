package com.dsatab.data.modifier;

import com.dsatab.data.Attribute;
import com.dsatab.data.CombatDistanceTalent;
import com.dsatab.data.CombatMeleeAttribute;
import com.dsatab.data.CombatProbe;
import com.dsatab.data.CombatShieldTalent;
import com.dsatab.data.Hero;
import com.dsatab.data.Modifier;
import com.dsatab.data.Probe;
import com.dsatab.data.enums.AttributeType;

public class AuModificator extends AbstractModificator {

	public static final float LEVEL_1 = 0.33f;
	public static final float LEVEL_2 = 0.25f;

	public AuModificator(Hero hero) {
		super(hero);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.modifier.AbstractModificator#isActive()
	 */
	@Override
	public boolean isActive() {
		return hero.getHeroConfiguration().isAuModifierActive();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.modifier.AbstractModificator#setActive(boolean)
	 */
	@Override
	public void setActive(boolean active) {
		hero.getHeroConfiguration().setAuModifierActive(active);
		super.setActive(active);
	}

	@Override
	public String getModificatorName() {

		float ratio = hero.getRatio(AttributeType.Ausdauer);
		if (ratio < LEVEL_2) {
			return "Ausdauer < 1/4";
		} else if (ratio < LEVEL_1) {
			return "Ausdauer < 1/3";
		} else {
			return null;
		}

	}

	@Override
	public String getModificatorInfo() {
		String info;
		float ratio = hero.getRatio(AttributeType.Ausdauer);
		if (ratio < LEVEL_2) {
			info = "AT,PA,INI -2";
		} else if (ratio < LEVEL_1) {
			info = "AT,PA,INI -1";
		} else {
			info = "";
		}
		return info;
	}

	@Override
	public Modifier getModifier(Probe probe) {
		if (isActive()) {
			int modifier = 0;

			if (probe instanceof CombatProbe || probe instanceof CombatShieldTalent
					|| probe instanceof CombatDistanceTalent || probe instanceof CombatMeleeAttribute) {
				float ratio = hero.getRatio(AttributeType.Ausdauer);
				if (ratio < LEVEL_2) {
					modifier = -2;
				} else if (ratio < LEVEL_1) {
					modifier = -1;
				}
			} else if (probe instanceof Attribute) {
				Attribute attr = (Attribute) probe;
				return getModifier(attr.getType());
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
			float ratio = hero.getRatio(AttributeType.Ausdauer);

			if (ratio < LEVEL_2) {
				if (type == AttributeType.ini || type == AttributeType.Initiative_Aktuell
						|| AttributeType.isFight(type)) {
					modifier = -2;
				}
			} else if (ratio < LEVEL_1) {
				if (type == AttributeType.ini || type == AttributeType.Initiative_Aktuell
						|| AttributeType.isFight(type)) {
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
