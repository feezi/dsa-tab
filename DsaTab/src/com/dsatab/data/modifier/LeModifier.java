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

public class LeModifier extends AbstractModifier {

	public LeModifier(Hero hero) {
		super(hero);
	}

	@Override
	public String getModifierName() {

		double ratio = hero.getLeRatio();
		if (ratio < 0.25) {
			return "Lebensenergie < 1/4";
		} else if (ratio < 0.33) {
			return "Lebensenergie < 1/3";
		} else if (ratio < 0.5) {
			return "Lebensenergie < 1/2";
		} else {
			return null;
		}

	}

	@Override
	public String getModifierInfo() {
		String info = "";
		double ratio = hero.getLeRatio();
		if (ratio < 0.25) {
			info = "Eigenschaft, Kampf +3; Talent-/Zauberproben +9; GS-3";
		} else if (ratio < 0.33) {
			info = "Eigenschaft, Kampf +2; Talent-/Zauberproben +6; GS-2";
		} else if (ratio < 0.5) {
			info = "Eigenschaft, Kampf +1; Talent-/Zauberproben +3; GS-1";
		}
		return info;
	}

	@Override
	public Modifier getModifier(Probe probe) {

		int modifier = 0;
		double ratio = hero.getLeRatio();

		if (probe instanceof Talent || probe instanceof Spell) {

			if (ratio < 0.25) {
				modifier = -9;
			} else if (ratio < 0.33) {
				modifier = -6;
			} else if (ratio < 0.5) {
				modifier = -3;
			}
		} else if (probe instanceof CombatProbe || probe instanceof CombatShieldTalent || probe instanceof CombatDistanceTalent
				|| probe instanceof CombatMeleeAttribute || probe instanceof Attribute) {
			if (ratio < 0.25) {
				modifier = -3;
			} else if (ratio < 0.33) {
				modifier = -2;
			} else if (ratio < 0.5) {
				modifier = -1;
			}
		}

		return new Modifier(modifier, getModifierName(), getModifierInfo());
	}

	@Override
	public Modifier getModifier(AttributeType type) {
		int modifier = 0;
		double ratio = hero.getLeRatio();

		if (ratio < 0.25) {
			if (AttributeType.isEigenschaft(type)) {
				modifier = -3;
			}
		} else if (ratio < 0.33) {
			if (AttributeType.isEigenschaft(type)) {
				modifier = -2;
			}
		} else if (ratio < 0.5) {
			if (AttributeType.isEigenschaft(type)) {
				modifier = -1;
			}
		}
		return new Modifier(modifier, getModifierName(), getModifierInfo());
	}

}
