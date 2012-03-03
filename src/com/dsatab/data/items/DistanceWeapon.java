package com.dsatab.data.items;

import java.util.List;

import org.jdom.Element;

import android.text.TextUtils;

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.common.StyleableSpannableStringBuilder;
import com.dsatab.common.Util;
import com.dsatab.data.Dice;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.xml.Xml;

public class DistanceWeapon extends ItemSpecification {

	private static final long serialVersionUID = 8538598636617857056L;

	private static final int DISTANCE_COUNT = 5;

	private String tp;

	private String distance[];
	private String distances;

	private String tpDistance[];
	private String tpDistances;

	private CombatTalentType combatTalentType;

	public DistanceWeapon(Item item) {
		super(item, ItemType.Fernwaffen, 0);
	}

	public String getTp() {

		return tp;
	}

	public void setTp(String tp) {
		this.tp = tp;
	}

	public String getDistances() {
		if (distances == null && distance != null) {
			distances = "(" + TextUtils.join("/", distance) + ")";
		}
		return distances;
	}

	public int getMaxDistance() {
		int count = getDistanceCount();

		if (count >= 0)
			return Util.parseInt(getDistance(count - 1));
		else
			return 0;

	}

	public int getDistanceCount() {
		if (distance != null)
			return distance.length;
		else
			return 0;
	}

	public String getDistance(int index) {
		if (distance != null)
			return distance[index];
		else
			return null;
	}

	public void setDistances(int index, String value) {
		if (distance == null) {
			distance = new String[DISTANCE_COUNT];
		}
		this.distance[index] = value;
		this.distances = null;
	}

	public void setDistances(String[] distances) {
		this.distance = distances;
		this.distances = null;
	}

	public void setDistances(String distances) {
		this.distances = distances;
		this.distance = Util.splitDistanceString(distances);
	}

	public String getTpDistances() {
		if (tpDistances == null && tpDistance != null) {
			tpDistances = "(" + TextUtils.join("/", tpDistance) + ")";
		}
		return tpDistances;
	}

	public void setTpDistances(String tpDistances) {
		this.tpDistances = tpDistances;
		this.tpDistance = Util.splitDistanceString(tpDistances);
	}

	public void setTpDistances(int index, String value) {
		if (tpDistance == null) {
			tpDistance = new String[DISTANCE_COUNT];
		}
		this.tpDistance[index] = value;
		this.tpDistances = null;
	}

	public CombatTalentType getCombatTalentType() {
		return combatTalentType;
	}

	public void setCombatTalentType(CombatTalentType combatTalentType) {
		this.combatTalentType = combatTalentType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.items.ItemSpecification#getName()
	 */
	@Override
	public String getName() {
		return "Fernkampf";
	}

	public String getInfo() {
		return getTp() + " " + getDistances() + " " + getTpDistances();
	}

	public CharSequence getInfo(int modifier) {

		StyleableSpannableStringBuilder info = new StyleableSpannableStringBuilder();

		if (modifier != 0) {
			Dice dice = Dice.parseDice(tp);
			if (dice != null) {

				dice.constant += modifier;
				if (modifier > 0) {
					info.appendColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueGreen),
							dice.toString());
				} else {
					info.appendColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueRed),
							dice.toString());
				}
			} else {
				info.append(tp);
				if (modifier > 0) {
					info.appendColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueGreen),
							Util.toProbe(modifier));
				} else {
					info.appendColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueRed),
							Util.toProbe(modifier));
				}
			}
		} else {
			info.append(tp);
		}

		info.append(" ");
		info.append(getDistances());
		info.append(" ");
		info.append(getTpDistances());

		return info;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.items.ItemSpecification#setElement(org.jdom.Element)
	 */
	@Override
	public void setElement(Element element) {
		@SuppressWarnings("unchecked")
		List<Element> waffen = element.getChildren(Xml.KEY_FERNKAMPWAFFE);

		Element child;
		for (Element waffe : waffen) {

			child = waffe.getChild(Xml.KEY_ENTFERNUNG);
			if (child != null) {
				for (int i = 0; i < DISTANCE_COUNT; i++) {
					String value = child.getAttributeValue("E" + i);
					if (!TextUtils.isEmpty(value)) {
						setDistances(i, value);
					}
				}
			}

			child = waffe.getChild(Xml.KEY_TPMOD);
			if (child != null) {
				for (int i = 0; i < DISTANCE_COUNT; i++) {
					String value = child.getAttributeValue("M" + i);
					if (!TextUtils.isEmpty(value)) {
						setTpDistances(i, value);
					}
				}
			}

			child = waffe.getChild(Xml.KEY_TREFFERPUNKTE);
			if (child != null) {
				String tp = child.getAttributeValue(Xml.KEY_TREFFERPUNKTE_MUL) + "W"
						+ child.getAttributeValue(Xml.KEY_TREFFERPUNKTE_DICE) + "+"
						+ child.getAttributeValue(Xml.KEY_TREFFERPUNKTE_SUM);
				setTp(tp);
			}
		}

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
