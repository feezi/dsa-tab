package com.dsatab.data;

import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.items.EquippedItem;

public class CombatProbe extends BaseProbe {

	private EquippedItem equippedItem;

	private CombatTalent combatTalent;

	private Probe probe;

	private boolean attack;

	protected CombatTalentType type;

	public CombatProbe(EquippedItem item, boolean attack) {
		this.equippedItem = item;
		this.attack = attack;

		this.combatTalent = item.getTalent();

		if (combatTalent != null) {
			if (this.attack)
				probe = combatTalent.getAttack();
			else
				probe = combatTalent.getDefense();

			if (probe != null)
				this.probeInfo = probe.getProbeInfo().clone();
		}

		// distance talents actually have probe values (MU/FF/KK) but they are
		// not used in case of a attack
		if (probe instanceof CombatDistanceTalent) {
			this.probeInfo.setAttributeTypes(null);
		}

		if (combatTalent instanceof CombatShieldTalent && equippedItem.getSecondaryItem() != null) {
			// shields and paradeweapons use the BE from their main weapon
			type = equippedItem.getSecondaryItem().getTalent().getCombatTalentType();

			if (type != null) {
				probeInfo.applyBePattern(type.getBe());
			}
		}
	}

	@Override
	public String getName() {
		if (probe != null)
			return probe.getName();
		else
			return null;
	}

	@Override
	public Integer getProbeBonus() {
		if (probe != null)
			return probe.getProbeBonus();
		else
			return null;
	}

	@Override
	public ProbeType getProbeType() {
		if (probe != null)
			return probe.getProbeType();
		else
			return ProbeType.TwoOfThree;
	}

	@Override
	public Integer getProbeValue(int i) {
		return getValue();
	}

	@Override
	public Integer getValue() {
		if (probe != null)
			return probe.getValue();
		else
			return null;
	}

	public EquippedItem getEquippedItem() {
		return equippedItem;
	}

	public CombatTalent getCombatTalent() {
		return combatTalent;
	}

	public boolean isAttack() {
		return attack;
	}

	@Override
	public String toString() {

		return (isAttack() ? "Angriff mit " : "Parade mit ")
				+ (getEquippedItem() != null ? (getEquippedItem().getItem().getTitle() + "(" + getName() + ")")
						: getName());
	}

}
