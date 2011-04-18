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

public class AuModifier extends AbstractModifier {

	public AuModifier(Hero hero) {
		super(hero);
	}

	@Override
	public String getModifierName() {

		double ratio = hero.getAuRatio();
		if (ratio < 0.25) {
			return "Ausdauer < 1/4";
		} else if (ratio < 0.33) {
			return "Ausdauer < 1/3";
		} else {
			return null;
		}

	}

	@Override
	public String getModifierInfo() {
		String info = "";
		double ratio = hero.getAuRatio();
		if (ratio < 0.25) {
			info = "AT,PA,INI +2";
		} else if (ratio < 0.33) {
			info = "AT,PA,INI +1";
		}
		return info;
	}

	@Override
	public Modifier getModifier(Probe probe) {

		int modifier = 0;
		double ratio = hero.getAuRatio();

		if (probe instanceof CombatProbe || probe instanceof CombatShieldTalent
				|| probe instanceof CombatDistanceTalent || probe instanceof CombatMeleeAttribute) {

			if (ratio < 0.25) {
				modifier = -2;
			} else if (ratio < 0.33) {
				modifier = -1;
			}
		} else if (probe instanceof Attribute) {
			Attribute attr = (Attribute) probe;
			if (attr.getType() == AttributeType.ini) {
				if (ratio < 0.25) {
					modifier = -2;
				} else if (ratio < 0.33) {
					modifier = -1;
				}
			}
		}

		return new Modifier(modifier, getModifierName(), getModifierInfo());
	}

	@Override
	public Modifier getModifier(AttributeType type) {
		int modifier = 0;
		double ratio = hero.getAuRatio();

		if (ratio < 0.25) {
			if (type == AttributeType.ini || type == AttributeType.Initiative_Aktuell) {
				modifier = -2;
			}
		} else if (ratio < 0.33) {
			if (type == AttributeType.ini || type == AttributeType.Initiative_Aktuell) {
				modifier = -1;
			}
		}
		return new Modifier(modifier, getModifierName(), getModifierInfo());
	}

}
