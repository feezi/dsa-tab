package com.dsatab.data;

import com.dsatab.DSATabApplication;
import com.dsatab.activity.BasePreferenceActivity;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.enums.Position;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.UsageType;

public class CombatShieldTalent extends BaseCombatTalent {

	protected CombatTalentType type;

	protected UsageType usageType;

	private int set;

	private String equippedName;

	public CombatShieldTalent(Hero hero, UsageType usageType, int set, String equippedName) {
		super(hero);
		this.usageType = usageType;
		this.set = set;
		this.equippedName = equippedName;

		if (UsageType.Paradewaffe == usageType)
			this.type = CombatTalentType.Dolche;
		else
			this.type = CombatTalentType.Raufen;

		this.probeInfo.applyBePattern(type.getBe());
		this.value = 0;
	}

	public String getName() {
		switch (usageType) {
		case Schild:
			return "Schildparade";
		case Paradewaffe:
			return "Parierwaffenparade";
		default:
			return "CombatShieldTalent with no UsageType";
		}

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
		int baseValue = 0;

		if (UsageType.Paradewaffe == usageType) {
			if (hero != null) {
				// der basiswert eine paradewaffe ist der paradewert der
				// geführten
				// hauptwaffe -/+ evtl. parierwaffen WdS 75
				EquippedItem paradeItem = hero.getEquippedItem(set, equippedName);
				if (paradeItem != null
						&& paradeItem.getSecondaryItem() != null
						&& (hero.hasFeature(SpecialFeature.PARIERWAFFEN_1) || hero
								.hasFeature(SpecialFeature.PARIERWAFFEN_2))) {
					EquippedItem equippedWeapon = paradeItem.getSecondaryItem();
					// check wether mainweapon has a defense value
					// TODO modifiers on main weapon should be considered here
					// too!!!
					if (equippedWeapon.getTalent() instanceof CombatMeleeTalent
							&& equippedWeapon.getTalent().getDefense() != null) {
						baseValue = equippedWeapon.getTalent().getDefense().getValue();

						int weaponPaMod = hero.getModifier(new CombatProbe(equippedWeapon, false));
						baseValue += weaponPaMod;

					} else {
						baseValue = hero.getAttributeValue(AttributeType.pa);
					}
				} else {
					baseValue = hero.getAttributeValue(AttributeType.pa);
				}
			}
		} else {
			baseValue = hero.getAttributeValue(AttributeType.pa);
		}

		return baseValue;
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

	public UsageType getUsageType() {
		return usageType;
	}

}
