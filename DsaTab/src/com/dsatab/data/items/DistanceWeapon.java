package com.dsatab.data.items;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.enums.CombatTalentType;

public class DistanceWeapon extends Item {

	private static final long serialVersionUID = 8538598636617857056L;

	private String tp;

	private String distances;

	private String distance[];

	private String tpDistances;

	private CombatTalentType combatTalentType;

	public DistanceWeapon() {

	}

	public String getTp() {
		return tp;
	}

	public void setTp(String tp) {
		this.tp = tp;
	}

	public String getDistances() {
		return distances;
	}

	public String getDistance(int index) {

		if (distance == null) {
			distance = Util.splitDistanceString(distances);
		}
		return distance[index];
	}

	public void setDistances(String distances) {
		this.distances = distances;
	}

	public String getTpDistances() {
		return tpDistances;
	}

	public void setTpDistances(String tpDistances) {
		this.tpDistances = tpDistances;
	}

	public CombatTalentType getCombatTalentType() {
		return combatTalentType;
	}

	public void setCombatTalentType(CombatTalentType combatTalentType) {
		this.combatTalentType = combatTalentType;
	}

	public String getInfo() {
		return getTp() + " " + getDistances() + " " + getTpDistances();
	}

	public int getResourceId() {
		switch (getCombatTalentType()) {
		case Wurfmesser:
			return R.drawable.icon_wurfdolch;
		case Armbrust:
			return R.drawable.icon_crossbow;
		case Wurfbeile:
			return R.drawable.icon_wurfbeil;
		case Wurfspeere:
			return R.drawable.icon_speer;
		case Diskus:
			return R.drawable.icon_diskus;
		case Schleuder:
			return R.drawable.icon_sling;
		default:
			return R.drawable.icon_bow;

		}
	}

}
