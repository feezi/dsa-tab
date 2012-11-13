package com.dsatab.data.items;

import java.util.List;

import org.jdom.Element;

import android.text.TextUtils;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.xml.Xml;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "item_shield")
public class Shield extends CloseCombatItem {

	@DatabaseField(generatedId = true)
	protected int id;

	@DatabaseField
	private boolean shield;
	@DatabaseField
	private boolean paradeWeapon;

	// transient cache for the info string
	private String info;

	/**
	 * no arg constructor for ormlite
	 */
	public Shield() {

	}

	public Shield(Item item) {
		super(item, ItemType.Schilde, 0);
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
