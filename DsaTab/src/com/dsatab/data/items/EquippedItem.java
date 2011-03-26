package com.dsatab.data.items;

import org.w3c.dom.Element;

import com.dsatab.common.Util;
import com.dsatab.data.CombatTalent;
import com.dsatab.data.Hero;
import com.dsatab.xml.Xml;

public class EquippedItem {

	private static final String RUESTUNGSNAME = "ruestungsname";

	private static final String SCHILDNAME = "schildname";

	private static final String WAFFENNAME = "waffenname";

	private static final String NAME_PREFIX_RUESTUNG = "ruestung";

	private static final String NAME_PREFIX_SCHILD = "schild";

	private static final String NAME_PREFIX_NK = "nkwaffe";

	private static final String NAME_PREFIX_FK = "fkwaffe";

	private Element element;

	private Item item = null;

	private int nameId;

	/**
	 * Weapon-Shield combies
	 */
	private EquippedItem secondaryEquippedItem = null;

	private String itemNameField;

	private CombatTalent talent = null;

	private UsageType usageType = null;

	private Hero hero;

	public EquippedItem(Hero hero) {
		this.hero = hero;
	}

	public EquippedItem(Hero hero, Element element) {
		this.hero = hero;
		setElement(element);
	}

	public EquippedItem(Hero hero, Element element, Item item) {
		this.hero = hero;
		this.element = element;
		this.item = item;
		setItem(item);
		setElement(element);
	}

	public Element getElement() {
		return element;
	}

	public UsageType getUsageType() {
		if (usageType == null && element != null && element.hasAttribute(Xml.KEY_VERWENDUNGSART))
			usageType = UsageType.valueOf(element.getAttribute(Xml.KEY_VERWENDUNGSART));
		return usageType;
	}

	public void setUsageType(UsageType usageType) {
		if (usageType != null)
			element.setAttribute(Xml.KEY_VERWENDUNGSART, usageType.name());
		else
			element.removeAttribute(Xml.KEY_VERWENDUNGSART);

		this.usageType = usageType;
	}

	public void setHand(Hand hand) {
		element.setAttribute(Xml.KEY_HAND, hand.name());
	}

	public Hand getHand() {
		if (element.hasAttribute(Xml.KEY_HAND))
			return Hand.valueOf(element.getAttribute(Xml.KEY_HAND));
		else
			return null;
	}

	public int getSet() {
		if (element.hasAttribute(Xml.KEY_SET))
			return Util.parseInt(element.getAttribute(Xml.KEY_SET));
		else
			return 0;
	}

	public void setSet(int set) {
		element.setAttribute(Xml.KEY_SET, Util.toString(set));
	}

	public void setElement(Element element) {
		this.element = element;

		if (!element.hasAttribute(Xml.KEY_SLOT))
			element.setAttribute(Xml.KEY_SLOT, "0");
		if (getName().startsWith(NAME_PREFIX_NK)) {
			itemNameField = WAFFENNAME;
			nameId = Util.parseInt(getName().substring(NAME_PREFIX_NK.length()));
		} else if (getName().startsWith(NAME_PREFIX_FK)) {
			itemNameField = WAFFENNAME;
			nameId = Util.parseInt(getName().substring(NAME_PREFIX_FK.length()));
			if (getItem() instanceof DistanceWeapon) {
				DistanceWeapon weapon = (DistanceWeapon) getItem();
				talent = hero.getCombatTalent(weapon.getCombatTalentType().getName());
			}
		} else if (getName().startsWith(NAME_PREFIX_SCHILD)) {
			itemNameField = SCHILDNAME;
			nameId = Util.parseInt(getName().substring(NAME_PREFIX_SCHILD.length()));
			if (getItem() instanceof Shield) {
				Shield shield = (Shield) getItem();

				if (getUsageType() == null) {
					if (shield.isShield())
						setUsageType(UsageType.Schild);
					else if (shield.isParadeWeapon())
						setUsageType(UsageType.Paradewaffe);
				}

				if (shield.isShield() && getUsageType() == UsageType.Schild)
					talent = hero.getCombatShieldTalent();
				else if (shield.isParadeWeapon() && getUsageType() == UsageType.Paradewaffe)
					talent = hero.getCombatParadeWeaponTalent(this);
			} else {
				talent = hero.getCombatShieldTalent();
			}

		} else if (getName().startsWith(NAME_PREFIX_RUESTUNG)) {
			itemNameField = RUESTUNGSNAME;
			nameId = Util.parseInt(getName().substring(NAME_PREFIX_RUESTUNG.length()));
		}
	}

	public Hero getHero() {
		return hero;
	}

	public String getName() {
		return element.getAttribute(Xml.KEY_NAME);
	}

	public int getNameId() {
		return nameId;
	}

	public void setName(String name) {
		element.setAttribute(Xml.KEY_NAME, name);
	}

	public String getItemName() {
		return element.getAttribute(itemNameField);
	}

	public void setItem(Item item) {

		this.item = item;

		if (item instanceof Weapon || item instanceof DistanceWeapon) {
			element.setAttribute(WAFFENNAME, item.getName());
		} else if (item instanceof Shield) {
			element.setAttribute(SCHILDNAME, item.getName());
		} else if (item instanceof Armor) {
			element.setAttribute(RUESTUNGSNAME, item.getName());
		}

		if (!element.hasAttribute(Xml.KEY_NAME)) {
			String namePrefix = null;

			if (item instanceof Weapon) {
				namePrefix = NAME_PREFIX_NK;
			}
			if (item instanceof DistanceWeapon) {
				namePrefix = NAME_PREFIX_FK;
			}
			if (item instanceof Shield) {
				namePrefix = NAME_PREFIX_SCHILD;
			}
			if (item instanceof Armor) {
				namePrefix = NAME_PREFIX_RUESTUNG;
			}

			// find first free slot
			int i = 1;
			while (hero.getEquippedItem(namePrefix + i) != null) {
				i++;
			}
			element.setAttribute(Xml.KEY_NAME, namePrefix + i);
		}
	}

	public Item getItem() {
		if (item == null && getItemName() != null) {
			item = hero.getItem(getItemName());
		}

		return item;
	}

	public EquippedItem getSecondaryItem() {
		if (secondaryEquippedItem == null) {
			if (getItem() instanceof Weapon && element.hasAttribute(Xml.KEY_SCHILD)) {
				int schild = Util.parseInt(element.getAttribute(Xml.KEY_SCHILD));
				if (schild > 0) {
					secondaryEquippedItem = hero.getEquippedItem(NAME_PREFIX_SCHILD + schild);
				}
			} else if (getItem() instanceof Shield) {

				int mySchildIndex = getNameId();
				for (EquippedItem equippedItem : hero.getEquippedItems()) {
					if (equippedItem.getElement().hasAttribute(Xml.KEY_SCHILD)) {
						int schild = Util.parseInt(equippedItem.getElement().getAttribute(Xml.KEY_SCHILD));
						if (mySchildIndex == schild) {
							secondaryEquippedItem = equippedItem;
						}
					}
				}
			}

		}
		return secondaryEquippedItem;
	}

	public void setSecondaryItem(EquippedItem secondaryEquippedItem) {

		this.secondaryEquippedItem = secondaryEquippedItem;
		Item secondaryItem = null;
		if (secondaryEquippedItem != null) {
			secondaryItem = secondaryEquippedItem.getItem();
		}

		if (getItem() instanceof Weapon) {
			if (secondaryEquippedItem == null) {
				element.setAttribute(Xml.KEY_SCHILD, "0");
			} else if (secondaryItem instanceof Shield) {
				String name = secondaryEquippedItem.getName();
				int mySchildIndex = Util.parseInt(name.substring(NAME_PREFIX_SCHILD.length()));
				element.setAttribute(Xml.KEY_SCHILD, Util.toString(mySchildIndex));
			} else if (secondaryItem instanceof Weapon) {
				getHero().addBeidhaendigerKampf(this, secondaryEquippedItem);
			}
		}

	}

	private String getTalentName() {
		return element.getAttribute(Xml.KEY_TALENT);
	}

	public CombatTalent getTalent() {
		if (talent == null) {
			talent = hero.getCombatTalent(getTalentName());
		}
		return talent;
	}

	public void setTalent(CombatTalent talent) {
		if (talent != null)
			element.setAttribute(Xml.KEY_TALENT, talent.getName());
		else
			element.removeAttribute(Xml.KEY_TALENT);

		this.talent = talent;
	}

	public String getSlot() {
		return element.getAttribute(Xml.KEY_SLOT);
	}

	public void setSlot(String slot) {
		element.setAttribute(Xml.KEY_SLOT, slot);
	}

}
