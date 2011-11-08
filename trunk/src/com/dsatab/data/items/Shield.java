package com.dsatab.data.items;

import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;

import android.text.TextUtils;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.xml.Xml;

public class Shield extends ItemSpecification {

	private static final long serialVersionUID = -1317930157801685718L;

	private Integer bf;

	private Integer ini;

	private Integer wmAt;
	private Integer wmPa;

	private boolean shield;
	private boolean paradeWeapon;

	private List<CombatTalentType> combatTalentType = new LinkedList<CombatTalentType>();

	private String info;

	public Shield(Item item) {
		super(item, ItemType.Schilde, 0);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.items.ItemSpecification#setElement(org.jdom.Element)
	 */
	@Override
	public void setElement(Element element) {

		@SuppressWarnings("unchecked")
		List<Element> waffen = element.getChildren(Xml.KEY_SCHILDWAFFE);

		for (Element waffe : waffen) {
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

	@Override
	public int getResourceId() {
		if (isParadeWeapon() && !isShield())
			return R.drawable.icon_messer;
		else
			return R.drawable.icon_shield;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.items.ItemSpecification#getName()
	 */
	@Override
	public String getName() {
		return "Paradewaffe";
	}

	public String getInfo() {
		if (info == null) {
			info = TextUtils.expandTemplate("^1/^2 Bf ^3 Ini ^4", Util.toString(getWmAt()), Util.toString(getWmPa()),
					Util.toString(getBf()), Util.toString(getIni())).toString();
		}
		return info;
	}
}
