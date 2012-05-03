package com.dsatab.data;

import com.dsatab.DSATabApplication;
import com.dsatab.activity.BasePreferenceActivity;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.enums.Position;
import com.dsatab.data.items.EquippedItem;

public class CombatParadeWeaponTalent extends CombatShieldTalent {

	private EquippedItem paradeItem;

	public CombatParadeWeaponTalent(Hero hero, EquippedItem paradeItem) {
		super(hero);

		this.type = CombatTalentType.Dolche;

		int value = -6;
		if (hero.hasFeature(SpecialFeature.LINKHAND))
			value += 1;
		if (hero.hasFeature(SpecialFeature.PARIERWAFFEN_1))
			value += 2;
		if (hero.hasFeature(SpecialFeature.PARIERWAFFEN_2))
			value += 2;

		this.value = value;
		this.paradeItem = paradeItem;

		probeInfo.applyBePattern(type.getBe());
	}

	public String getName() {
		return "Parierwaffenparade";
	}

	protected int getBaseValue() {
		int baseValue = 0;

		if (hero != null) {
			// der basiswert eine paradewaffe ist der paradewert der geführten
			// hauptwaffe (-6) + evtl. linkhand parierwaffen
			if (paradeItem != null && paradeItem.getSecondaryItem() != null) {
				EquippedItem equippedWeapon = paradeItem.getSecondaryItem();
				// check wether mainweapon has a defense value
				if (equippedWeapon.getTalent().getDefense() != null)
					baseValue = equippedWeapon.getTalent().getDefense().getValue();
				else
					baseValue = 0;
			} else {
				baseValue = hero.getAttributeValue(AttributeType.pa);
			}
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
}
