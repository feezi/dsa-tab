package com.dsatab.data;

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
	}

	public String getName() {
		return "Parierwaffenparade";
	}

	public String getBe() {
		return type.getBe();
	}

	protected int getBaseValue() {
		int baseValue = 0;

		if (hero != null) {
			// der basiswert eine paradewaffe ist der paradewert der geführten
			// hauptwaffe (-6) + evtl. linkhand parierwaffen
			if (paradeItem != null && paradeItem.getSecondaryItem() != null) {
				EquippedItem equippedWeapon = paradeItem.getSecondaryItem();
				baseValue = equippedWeapon.getTalent().getDefense().getValue();
			} else {
				baseValue = hero.getAttributeValue(AttributeType.pa);
			}
		}

		return baseValue;
	}

	public Position getPosition(int w20) {
		return Position.box_rauf_hruru[w20];
	}
}
