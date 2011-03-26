package com.dsatab.data.items;

import java.util.LinkedList;
import java.util.List;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.enums.CombatTalentType;

public class Shield extends Item {

	private static final long serialVersionUID = -1317930157801685718L;

	private Integer bf;

	private Integer ini;

	private Integer wmAt;
	private Integer wmPa;

	private boolean shield;
	private boolean paradeWeapon;

	private List<CombatTalentType> combatTalentType = new LinkedList<CombatTalentType>();

	public Shield() {

	}

	public Integer getBf() {
		return bf;
	}

	public void setBf(Integer bf) {
		this.bf = bf;
	}

	public Integer getIni() {
		return ini;
	}

	public void setIni(Integer ini) {
		this.ini = ini;
	}

	public Integer getWmAt() {
		return wmAt;
	}

	public void setWmAt(Integer wmAt) {
		this.wmAt = wmAt;
	}

	public Integer getWmPa() {
		return wmPa;
	}

	public void setWmPa(Integer wmPa) {
		this.wmPa = wmPa;
	}

	public boolean isShield() {
		return shield;
	}

	public void setShield(boolean shield) {
		this.shield = shield;
	}

	public boolean isParadeWeapon() {
		return paradeWeapon;
	}

	public void setParadeWeapon(boolean paradeWeapon) {
		this.paradeWeapon = paradeWeapon;
	}

	public CombatTalentType getCombatTalentType() {
		if (combatTalentType.isEmpty())
			return null;
		else
			return combatTalentType.get(0);
	}

	public List<CombatTalentType> getCombatTalentTypes() {
		return combatTalentType;
	}

	public void setCombatTalentType(List<CombatTalentType> type) {
		this.combatTalentType = type;
	}

	@Override
	public int getResourceId() {
		if (isParadeWeapon() && !isShield())
			return R.drawable.icon_messer;
		else
			return R.drawable.icon_shield;
	}

	public String getInfo() {
		return Util.toString(getWmAt()) + "/" + Util.toString(getWmPa()) + " Bf " + Util.toString(getBf()) + " Ini "
				+ Util.toString(getIni());
	}

}
