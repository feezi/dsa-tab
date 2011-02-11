package com.dsatab.data.items;

import java.util.Arrays;
import java.util.HashMap;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.enums.Position;

public class Armor extends Item {

	private static final long serialVersionUID = 5895989410415630188L;

	public enum ArmorType {
		Helm, Beine, Arme, Torso, Komplettrüstung
	}

	private double be;

	private HashMap<Position, Integer> rs;

	private int maxRs = 0;

	private ArmorType armorType;

	private String info = null;

	public Armor() {
		rs = new HashMap<Position, Integer>(Position.values().length);
	}

	public double getBe() {
		return be;
	}

	public void setBe(double be) {
		this.be = be;
	}

	public String getInfo() {

		if (info == null) {
			StringBuilder sb = new StringBuilder();

			sb.append("Be ");
			sb.append(Util.toString(getBe()));

			int[] kopf = new int[] { getRs(Position.Head_Face), getRs(Position.Head_Side), getRs(Position.Head_Up),
					getRs(Position.Head), getRs(Position.Neck) };
			int[] rumpf = new int[] { getRs(Position.Stomach), getRs(Position.Chest), getRs(Position.Pelvis),
					getRs(Position.LeftShoulder), getRs(Position.RightShoulder) };
			int[] arms = new int[] { getRs(Position.LeftArm), getRs(Position.LeftUpperArm), getRs(Position.RightArm),
					getRs(Position.RightUpperArm) };
			int[] legs = new int[] { getRs(Position.UpperLeg), getRs(Position.LowerLeg) };

			Arrays.sort(kopf);
			Arrays.sort(rumpf);
			Arrays.sort(arms);
			Arrays.sort(legs);

			sb.append(";Ko " + kopf[kopf.length - 1]);
			sb.append(" Ru " + rumpf[kopf.length - 1]);
			sb.append(" Arm " + arms[arms.length - 1]);
			sb.append(" Bein " + legs[legs.length - 1]);

			info = sb.toString();
		}
		return info;
	}

	public int getRs(Position pos) {
		Integer i = rs.get(pos);

		if (i == null)
			return 0;
		else
			return i;
	}

	public void setRs(Position pos, int rs) {
		this.rs.put(pos, rs);

		maxRs = Math.max(maxRs, rs);
	}

	public ArmorType getArmorType() {
		return armorType;
	}

	public void setArmorType(ArmorType armorType) {
		this.armorType = armorType;
	}

	@Override
	public int getResourceId() {
		switch (getArmorType()) {
		case Helm:
			if (getRs(Position.Head_Face) > 5)
				return R.drawable.icon_helm_full;
			else if (getRs(Position.Head_Face) > 0)
				return R.drawable.icon_helm_half;
			else
				return R.drawable.icon_helm;
		case Torso:
			if (maxRs > 6)
				return R.drawable.icon_armor_metal;
			else if (maxRs > 2)
				return R.drawable.icon_armor_chain;
			else
				return R.drawable.icon_armor_cloth;
		case Arme:
		case Beine:
			return R.drawable.icon_greaves;
		default:
			return R.drawable.icon_armor;

		}
	}

}
