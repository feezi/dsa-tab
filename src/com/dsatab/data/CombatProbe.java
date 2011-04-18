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
			type = equippedItem.getSecondaryItem().getTalent().getType();
		}
	}

	public CombatProbe(Hero hero, CombatTalent talent, boolean attack) {
		this.attack = attack;
		this.hero = hero;
		this.combatTalent = talent;

		if (this.attack)
			probe = talent.getAttack();
		else
			probe = talent.getDefense();
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
		return probe.getName();
	}

	@Override
	public String getProbe() {
		return probe.getProbe();
	}

	@Override
	public Integer getProbeBonus() {
		return probe.getProbeBonus();
	}

	@Override
	public ProbeType getProbeType() {
		return probe.getProbeType();
	}

	@Override
	public Integer getProbeValue(int i) {
		return getValue();
	}

	@Override
	public Integer getValue() {
		return probe.getValue();
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
				+ (getEquippedItem() != null ? (getEquippedItem().getItem().getTitle() + "(" + probe.getName() + ")")
						: probe.getName());
	}

}
