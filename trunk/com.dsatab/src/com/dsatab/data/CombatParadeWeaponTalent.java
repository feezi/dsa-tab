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
		this.paradeItem = paradeItem;
		this.value = 0;
		probeInfo.applyBePattern(type.getBe());
	}

	public String getName() {
		return "Parierwaffenparade";
	}

	@Override
	protected int getBaseValue() {
		int baseValue = 0;

		if (hero != null) {
			// der basiswert eine paradewaffe ist der paradewert der geführten
			// hauptwaffe -/+ evtl. parierwaffen WdS 75
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
