package com.dsatab.data;

import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.data.items.EquippedItem;

public class CombatProbe implements Probe {

	private EquippedItem equippedItem = null;

	private CombatTalent combatTalent;

	private Probe probe = null;

	private boolean attack;

	private Integer erschwernis;

	private Hero hero = null;

	protected CombatTalentType type = null;

	public CombatProbe(Hero hero, EquippedItem item, boolean attack) {
		this(hero, item.getTalent(), attack);
		this.equippedItem = item;

		if (combatTalent instanceof CombatShieldTalent && equippedItem.getSecondaryItem() != null) {

			// shields and paradeweapons use the BE from their main weapon
			type = equippedItem.getSecondaryItem().getTalent().getCombatTalentType();
		}
	}

	public CombatProbe(Hero hero, CombatTalent talent, boolean attack) {
		this.attack = attack;
		this.hero = hero;
		this.combatTalent = talent;

		if (combatTalent != null) {
			if (this.attack)
				probe = combatTalent.getAttack();
			else
				probe = combatTalent.getDefense();
		}
	}

	public Integer getErschwernis() {
		return erschwernis;
	}

	public void setErschwernis(Integer erschwernis) {
		this.erschwernis = erschwernis;
	}

	@Override
	public String getBe() {
		if (type != null)
			return type.getBe();
		else
			return probe.getBe();
	}

	@Override
	public String getName() {
		if (probe != null)
			return probe.getName();
		else
			return null;
	}

	@Override
	public String getProbe() {
		// distance talents actually have probe values (MU/FF/KK) but they are
		// not used in case of a attack
		if (probe instanceof CombatDistanceTalent)
			return null;
		else
			return probe.getProbe();
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
				+ (getEquippedItem() != null ? (getEquippedItem().getItem().getTitle() + "(" + getName() + ")") : probe
						.getName());
	}

}
