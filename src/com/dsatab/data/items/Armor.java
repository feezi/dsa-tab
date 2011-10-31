package com.dsatab.data.items;

import java.util.Arrays;
import java.util.HashMap;

import org.jdom.Element;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.enums.Position;
import com.dsatab.xml.DomUtil;
import com.dsatab.xml.Xml;

public class Armor extends ItemSpecification {

	private static final long serialVersionUID = 5895989410415630188L;

	public static final String CATEGORY_ARME = "Arme";
	public static final String CATEGORY_BEINE = "Beine";
	public static final String CATEGORY_HELM = "Helm";
	public static final String CATEGORY_FULL = "Komplettrüstung";
	public static final String CATEGORY_TORSO = "Torso";

	private float be;

	private int stars;

	private boolean zonenHalfBe;

	private HashMap<Position, Integer> rs;

	private int zonenRs = 0;
	private int totalRs = 0;

	private int maxRs = 0;

	private String info = null;

	public Armor(Item item) {
		super(item, ItemType.Rüstung, 0);
		rs = new HashMap<Position, Integer>(Position.values().length);
	}

	public float getBe() {
		return be;
	}

	public void setBe(float be) {
		this.be = be;
	}

	public boolean isZonenHalfBe() {
		return zonenHalfBe;
	}

	public void setZonenHalfBe(boolean zonenHalfBe) {
		this.zonenHalfBe = zonenHalfBe;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.items.ItemSpecification#getName()
	 */
	@Override
	public String getName() {
		return "Rüstung";
	}

	public String getInfo() {

		if (info == null) {
			StringBuilder sb = new StringBuilder();

			sb.append("Be ");
			sb.append(Util.toString(getBe()));

			int[] kopf = new int[] { getRs(Position.Head_Face), getRs(Position.Head_Side), getRs(Position.Head_Up),
					getRs(Position.Kopf), getRs(Position.Neck) };
			int[] rumpf = new int[] { getRs(Position.Bauch), getRs(Position.Brust), getRs(Position.Pelvis),
					getRs(Position.Ruecken), getRs(Position.LeftShoulder), getRs(Position.RightShoulder) };
			int[] arms = new int[] { getRs(Position.LeftLowerArm), getRs(Position.LeftUpperArm),
					getRs(Position.RightLowerArm), getRs(Position.RightUpperArm), getRs(Position.LinkerArm),
					getRs(Position.RechterArm) };
			int[] legs = new int[] { getRs(Position.UpperLeg), getRs(Position.LowerLeg), getRs(Position.LinkesBein),
					getRs(Position.RechtesBein) };

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

	public int getStars() {
		return stars;
	}

	public void setStars(int stars) {
		this.stars = stars;
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

	public int getZonenRs() {
		return zonenRs;
	}

	public void setZonenRs(int zonenRs) {
		this.zonenRs = zonenRs;
	}

	public int getTotalRs() {
		return totalRs;
	}

	public void setTotalRs(int totalRs) {
		this.totalRs = totalRs;
	}

	public void setElement(Element element) {

		Element ruestung = element.getChild(Xml.KEY_RUESTUNG);

		// revert changes made due to harmony parsing bug of umlauts
		if (ruestung == null) {
			ruestung = element.getChild(Xml.KEY_RUESTUNG_UE);
			if (ruestung != null) {
				ruestung.setName(Xml.KEY_RUESTUNG);
			}
		}

		if (ruestung != null) {

			String be = DomUtil.getChildValue(ruestung, Xml.KEY_GESAMT_BE, Xml.KEY_VALUE);
			if (be != null) {
				setBe(Util.parseFloat(be));
			}
			for (Position pos : Position.values()) {
				String rs = DomUtil.getChildValue(ruestung, pos.name().toLowerCase(), Xml.KEY_VALUE);
				if (rs != null) {
					setRs(pos, Util.parseInt(rs));
				}
			}

		}

	}

	public int getResourceId() {
		if (CATEGORY_HELM.equalsIgnoreCase(item.getCategory())) {
			if (getRs(Position.Head_Face) > 5)
				return R.drawable.icon_helm_full;
			else if (getRs(Position.Head_Face) > 0)
				return R.drawable.icon_helm_half;
			else
				return R.drawable.icon_helm;
		} else if (CATEGORY_TORSO.equalsIgnoreCase(item.getCategory())
				|| CATEGORY_FULL.equalsIgnoreCase(item.getCategory())) {
			if (maxRs > 6)
				return R.drawable.icon_armor_metal;
			else if (maxRs > 2)
				return R.drawable.icon_armor_chain;
			else
				return R.drawable.icon_armor_cloth;
		} else if (CATEGORY_ARME.equalsIgnoreCase(item.getCategory())
				|| CATEGORY_BEINE.equalsIgnoreCase(item.getCategory())) {
			return R.drawable.icon_greaves;
		} else {
			return R.drawable.icon_armor;
		}
	}
}
