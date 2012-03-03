package com.dsatab.data.items;

import java.util.LinkedList;
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

public class Weapon extends ItemSpecification {

	private String tp;

	private Integer tpKKMin;
	private Integer tpKKStep;

	private Integer bf;

	private Integer ini;

	private Integer wmAt;
	private Integer wmPa;

	private boolean twoHanded;
	private String distance;

	private List<CombatTalentType> combatTalentType = new LinkedList<CombatTalentType>();

	private String info;

	public Weapon(Item item, int version) {
		super(item, ItemType.Waffen, version);
	}

	/**
	 * 
	 */
	public Weapon(Item item) {
		super(item, ItemType.Waffen, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.items.Item#setElement(org.jdom.Element)
	 */
	@Override
	public void setElement(Element element) {

		@SuppressWarnings("unchecked")
		List<Element> waffen = element.getChildren(Xml.KEY_NAHKAMPWAFFE);

		for (Element waffe : waffen) {

			if (waffe.getAttribute(Xml.KEY_VARIANTE) != null) {

				int variante = Util.parseInt(waffe.getAttributeValue(Xml.KEY_VARIANTE));
				if (variante == version) {
					Element trefferpunkte = waffe.getChild(Xml.KEY_TREFFERPUNKTE);
					if (trefferpunkte != null) {
						String tp = trefferpunkte.getAttributeValue(Xml.KEY_TREFFERPUNKTE_MUL) + "W"
								+ trefferpunkte.getAttributeValue(Xml.KEY_TREFFERPUNKTE_DICE) + "+"
								+ trefferpunkte.getAttributeValue(Xml.KEY_TREFFERPUNKTE_SUM);
						setTp(tp);
					}

					Element tpKK = waffe.getChild(Xml.KEY_TREFFERPUNKTE_KK);
					if (tpKK != null) {
						setTpKKMin(Util.parseInt(tpKK.getAttributeValue(Xml.KEY_TREFFERPUNKTE_KK_MIN)));
						setTpKKStep(Util.parseInt(tpKK.getAttributeValue(Xml.KEY_TREFFERPUNKTE_KK_STEP)));
					}
					Element wm = waffe.getChild(Xml.KEY_WAFFENMODIF);
					if (wm != null) {
						setWmAt(Util.parseInt(wm.getAttributeValue(Xml.KEY_WAFFENMODIF_AT)));
						setWmPa(Util.parseInt(wm.getAttributeValue(Xml.KEY_WAFFENMODIF_PA)));
					}
					Element bf = waffe.getChild(Xml.KEY_BRUCHFAKTOR);
					if (bf != null) {
						setBf(Util.parseInt(bf.getAttributeValue(Xml.KEY_BRUCHFAKTOR_AKT)));
					}
					Element ini = waffe.getChild(Xml.KEY_INI_MOD);
					if (ini != null) {
						setIni(Util.parseInt(ini.getAttributeValue(Xml.KEY_INI_MOD_INI)));
					}
				}
			}

		}
	}

	public String getTp() {
		return tp;
	}

	public void setTp(String tp) {
		this.tp = tp;
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

	public Integer getTpKKMin() {
		return tpKKMin;
	}

	public void setTpKKMin(Integer tpKKMin) {
		this.tpKKMin = tpKKMin;
	}

	public Integer getTpKKStep() {
		return tpKKStep;
	}

	public void setTpKKStep(Integer tpKKStep) {
		this.tpKKStep = tpKKStep;
	}

	public boolean isTwoHanded() {
		return twoHanded;
	}

	public void setTwoHanded(boolean twoHanded) {
		this.twoHanded = twoHanded;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
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
		switch (getCombatTalentType()) {
		case Anderthalbhänder:
		case Zweihandschwerter:
			return R.drawable.icon_2schwert;
		case Hiebwaffen:
			return R.drawable.icon_hieb;
		case Stäbe:
			return R.drawable.icon_stab;
		case Fechtwaffen:
			return R.drawable.icon_fecht;
		case Dolche:
			return R.drawable.icon_messer;
		case Speere:
			return R.drawable.icon_speer;
		case Infanteriewaffen:
			return R.drawable.icon_halberd;
		case Zweihandhiebwaffen:
			if (item.getName().contains("hammer")) {
				return R.drawable.icon_hammer;
			} else {
				return R.drawable.icon_2hieb;
			}
		case Zweihandflegel:
			return R.drawable.icon_2hieb;
		case Kettenstäbe:
		case Kettenwaffen:
			return R.drawable.icon_kettenwaffen;
		case Raufen:
		case Ringen:
			return R.drawable.icon_fist;
		case Peitsche:
			return R.drawable.icon_whip;
		default:
			return R.drawable.icon_sword;
		}
	}

	public int getKKModifier(int kk) {
		kk = kk - getTpKKMin();
		int kkModifier = 0;
		if (kk > 0)
			kkModifier = (int) Math.floor((float) kk / getTpKKStep());
		else
			kkModifier = (int) Math.ceil((float) kk / getTpKKStep());

		return kkModifier;
	}

	public CharSequence getInfo(int kk, int modifier) {

		StyleableSpannableStringBuilder info = new StyleableSpannableStringBuilder();

		String tp = getTp();

		int tpModifier = getKKModifier(kk);

		if (tpModifier != 0 || modifier != 0) {
			Dice dice = Dice.parseDice(tp);
			if (dice != null) {
				dice.constant += tpModifier;
				dice.constant += modifier;
				if (tpModifier + modifier > 0) {
					info.appendColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueGreen),
							dice.toString());
				} else {
					info.appendColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueRed),
							dice.toString());
				}
			} else {
				info.append(tp);
				if (tpModifier + modifier > 0) {
					info.appendColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueGreen),
							Util.toProbe(tpModifier + modifier));
				} else {
					info.appendColor(DSATabApplication.getInstance().getResources().getColor(R.color.ValueRed),
							Util.toProbe(tpModifier + modifier));
				}
			}
		} else {
			info.append(tp);
		}

		info.append(" ");
		info.append(Util.toString(getTpKKMin()) + "/" + Util.toString(getTpKKStep()));
		info.append(" ");
		info.append(Util.toString(getWmAt()) + "/" + Util.toString(getWmPa()));
		info.append(" ");
		info.append("Ini " + Util.toString(getIni()));
		info.append(" ");
		info.append(getDistance());
		if (isTwoHanded()) {
			info.append(" 2H");
		}

		return info;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.items.ItemSpecification#getName()
	 */
	@Override
	public String getName() {
		return "Nahkampf";
	}

	public String getInfo() {
		if (info == null) {
			info = TextUtils.expandTemplate("^1 ^2/^3 ^4/^5 Ini ^6 ^7 ^8", getTp(), Util.toString(getTpKKMin()),
					Util.toString(getTpKKStep()), Util.toString(getWmAt()), Util.toString(getWmPa()),
					Util.toString(getIni()), getDistance(), isTwoHanded() ? "2H" : "").toString();
		}
		return info;

	}

}
