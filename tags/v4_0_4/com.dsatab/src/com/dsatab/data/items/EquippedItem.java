package com.dsatab.data.items;

import java.io.File;
import java.util.UUID;

import android.content.Context;
import android.widget.Toast;

import com.dsatab.data.CombatDistanceTalent;
import com.dsatab.data.CombatMeleeTalent;
import com.dsatab.data.CombatProbe;
import com.dsatab.data.CombatShieldTalent;
import com.dsatab.data.CombatTalent;
import com.dsatab.data.Hero;
import com.dsatab.data.ItemLocationInfo;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.util.Util;
import com.gandulf.guilib.util.Debug;

public class EquippedItem implements ItemCard {

	public static final String NAME_PREFIX_RUESTUNG = "ruestung";

	public static final String NAME_PREFIX_SCHILD = "schild";

	public static final String NAME_PREFIX_NK = "nkwaffe";

	public static final String NAME_PREFIX_FK = "fkwaffe";

	private Item item = null;

	private UUID id;

	private int nameId;

	private ItemSpecification itemSpecification = null;

	/**
	 * Weapon-Shield combies
	 */
	private EquippedItem secondaryEquippedItem = null;

	private CombatTalent talent = null;

	private UsageType usageType = null;

	private Hero hero;

	private ItemLocationInfo itemInfo;

	private CombatProbe at, pa;

	private Hand hand;

	private int set;

	private String slot, name;

	private Integer schildIndex;

	private boolean beidhändigerKampf;

	public EquippedItem(Hero hero, CombatTalent talent, Item item, ItemSpecification itemSpecification) {
		this.id = UUID.randomUUID();
		this.hero = hero;
		this.itemInfo = new ItemLocationInfo();
		this.item = item;
		setTalent(talent);
		setItemSpecification(itemSpecification);
	}

	public Integer getSchildIndex() {
		return schildIndex;
	}

	public CombatProbe getCombatProbeAttacke() {
		if (at == null) {
			at = new CombatProbe(this, true);
		}
		return at;
	}

	public CombatProbe getCombatProbeDefense() {
		if (pa == null) {
			pa = new CombatProbe(this, false);
		}
		return pa;
	}

	public ItemSpecification getItemSpecification() {
		return itemSpecification;
	}

	protected void refreshTalent(Context context) {
		if (itemSpecification instanceof Weapon) {
			Weapon weapon = (Weapon) itemSpecification;
			// if the current talent does not fit search for a new one
			if (!(talent instanceof CombatMeleeTalent)
					|| !weapon.getCombatTalentTypes().contains(getTalent().getCombatTalentType())) {
				CombatTalent combatTalent = Util.getBest(hero.getAvailableCombatTalents(weapon));
				if (combatTalent == null) {
					if (context != null) {
						Toast.makeText(context, "Es wurde kein verwendbares Talent gefunden.", Toast.LENGTH_LONG)
								.show();
					} else {
						Debug.warning("Es wurde kein verwendbares Talent gefunden: " + toString());
					}
					return;
				} else {
					setTalent(combatTalent);
				}
			}
		} else if (itemSpecification instanceof Shield) {
			Shield shield = (Shield) itemSpecification;
			if (talent instanceof CombatShieldTalent) {
				CombatShieldTalent combatShieldTalent = (CombatShieldTalent) talent;
				if (getUsageType() != combatShieldTalent.getUsageType()) {
					setTalent(hero.getCombatShieldTalent(getUsageType(), getSet(), getName()));
				}
			} else {
				setTalent(hero.getCombatShieldTalent(getUsageType(), getSet(), getName()));
			}
		} else if (itemSpecification instanceof DistanceWeapon) {
			DistanceWeapon distanceweapon = (DistanceWeapon) itemSpecification;
			if (!(talent instanceof CombatDistanceTalent)
					|| distanceweapon.getCombatTalentType() != getTalent().getCombatTalentType()) {
				CombatTalent talent = hero.getCombatTalent(distanceweapon.getCombatTalentType().name());
				setTalent(talent);
			}
		}
	}

	public static CombatTalent getCombatTalent(Hero hero, UsageType usageType, int set, String name,
			ItemSpecification itemSpecification) {
		CombatTalent talent = null;

		// search for the default talents of the items
		if (itemSpecification instanceof Weapon) {
			Weapon weapon = (Weapon) itemSpecification;
			talent = Util.getBest(hero.getAvailableCombatTalents(weapon));
		} else if (itemSpecification instanceof DistanceWeapon) {
			DistanceWeapon weapon = (DistanceWeapon) itemSpecification;
			talent = hero.getCombatTalent(weapon.getCombatTalentType().getName());
		} else if (itemSpecification instanceof Shield) {
			Shield shield = (Shield) itemSpecification;
			talent = hero.getCombatShieldTalent(usageType, set, name);
		}
		return talent;
	}

	public static ItemSpecification getItemSpecification(Hero hero, String equippedName, Item item,
			UsageType usageType, String bezeichner) {
		ItemSpecification itemSpecification = null;

		if (item.getSpecifications().size() == 1) {
			itemSpecification = item.getSpecifications().get(0);
		} else {
			// search for a spec with this name
			if (bezeichner != null) {
				for (ItemSpecification itemSpec : item.getSpecifications()) {
					if (bezeichner.equals(itemSpec.getSpecificationLabel())) {
						itemSpecification = itemSpec;
						break;
					}
				}
			}
		}

		// find a version that fits the talent or at least the type of
		// equipped item matches the type of weapon (nk = weapon, fk =
		// Distance, shield = Shield, ...)
		if (itemSpecification == null) {
			outer: {
				for (ItemSpecification specification : item.getSpecifications()) {
					if (equippedName.startsWith(NAME_PREFIX_NK) && specification instanceof Weapon) {
						Weapon weapon = (Weapon) specification;
						for (CombatTalentType type : weapon.getCombatTalentTypes()) {
							if (hero.getCombatTalent(type.name()) != null) {
								itemSpecification = specification;
								break outer;
							}
						}
					} else if (equippedName.startsWith(NAME_PREFIX_SCHILD) && specification instanceof Shield) {
						Shield shield = (Shield) specification;
						if (shield.isParadeWeapon() && usageType == UsageType.Paradewaffe) {
							itemSpecification = specification;
							break;
						} else if (shield.isShield() && usageType == UsageType.Schild) {
							itemSpecification = specification;
							break;
						}
					} else if (equippedName.startsWith(NAME_PREFIX_FK) && specification instanceof DistanceWeapon) {
						itemSpecification = specification;
						break;
					}
				}
			}
		}

		// still nothing found, just take the first one without a specLabel
		if (itemSpecification == null && !item.getSpecifications().isEmpty()) {
			for (ItemSpecification itemSpec : item.getSpecifications()) {
				if (itemSpec.getSpecificationLabel() == null) {
					Debug.verbose("Could not find a specific itemspecificaton for " + item.toString() + " using "
							+ itemSpec.toString());
					itemSpecification = itemSpec;
					break;
				}
			}
			// if there is not itemspec without a specLabel take the first
			// one atall
			if (itemSpecification == null) {
				itemSpecification = item.getSpecifications().get(0);
			}
		}

		return itemSpecification;
	}

	public void setItemSpecification(ItemSpecification itemSpecification) {
		setItemSpecification(null, itemSpecification);
	}

	public void setItemSpecification(Context context, ItemSpecification itemSpecification) {
		this.itemSpecification = itemSpecification;
		refreshTalent(context);
		hero.fireItemChangedEvent(this);
	}

	public ItemLocationInfo getItemInfo() {
		return itemInfo;
	}

	public UsageType getUsageType() {
		if (usageType == null && itemSpecification instanceof Shield) {
			Shield shield = (Shield) itemSpecification;
			if (shield.isShield()) {
				usageType = UsageType.Schild;
			} else if (shield.isParadeWeapon()) {
				usageType = UsageType.Paradewaffe;
			}
		}
		return usageType;
	}

	public void setUsageType(UsageType usageType) {
		this.usageType = usageType;
	}

	public void setHand(Hand hand) {
		this.hand = hand;
	}

	public Hand getHand() {
		return hand;
	}

	public UUID getId() {
		return id;
	}

	public int getSet() {
		return set;
	}

	public void setSet(int set) {
		this.set = set;
	}

	public void setName(String name) {
		this.name = name;

		String number = null;
		if (name.startsWith(NAME_PREFIX_NK)) {
			number = name.substring(NAME_PREFIX_NK.length());
		} else if (name.startsWith(NAME_PREFIX_FK)) {
			number = name.substring(NAME_PREFIX_FK.length());
		} else if (name.startsWith(NAME_PREFIX_RUESTUNG)) {
			number = name.substring(NAME_PREFIX_RUESTUNG.length());
		} else if (name.startsWith(NAME_PREFIX_SCHILD)) {
			number = name.substring(NAME_PREFIX_SCHILD.length());
		}
		if (number != null) {
			nameId = Util.parseInteger(number);
		}
	}

	public void setSchildIndex(Integer schildIndex) {
		this.schildIndex = schildIndex;
	}

	public boolean isShieldWeapon() {
		return getItemSpecification() instanceof Shield;
	}

	public boolean isCloseCombatWeapon() {
		return getItemSpecification() instanceof Weapon;
	}

	public boolean isDistanceWeapon() {
		return getItemSpecification() instanceof DistanceWeapon;
	}

	public boolean isArmor() {
		return getItemSpecification() instanceof Armor;
	}

	public Hero getHero() {
		return hero;
	}

	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.items.ItemCard#getTitle()
	 */
	@Override
	public String getTitle() {
		return getItem().getTitle();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.items.ItemCard#getFile()
	 */
	@Override
	public File getFile() {
		return getItem().getFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.items.ItemCard#hasImage()
	 */
	@Override
	public boolean hasImage() {
		return getItem().hasImage();
	}

	public int getNameId() {
		return nameId;
	}

	public Item getItem() {
		return item;
	}

	public EquippedItem getSecondaryItem() {
		return secondaryEquippedItem;
	}

	public void setSecondaryItem(EquippedItem secondaryEquippedItem) {
		if (secondaryEquippedItem == null) {
			this.setBeidhändigerKampf(false);
		} else if (this.secondaryEquippedItem != null) {
			this.secondaryEquippedItem.setBeidhändigerKampf(false);
		}

		this.secondaryEquippedItem = secondaryEquippedItem;

		if (getItemSpecification() instanceof Weapon) {
			if (secondaryEquippedItem == null) {
				schildIndex = 0;
			} else if (secondaryEquippedItem.getItemSpecification() instanceof Shield) {
				String name = secondaryEquippedItem.getName();
				int mySchildIndex = Util.parseInteger(name.substring(NAME_PREFIX_SCHILD.length()));
				schildIndex = mySchildIndex;
			}
		}
	}

	public CombatTalent getTalent() {
		return talent;
	}

	public void setTalent(CombatTalent talent) {
		this.talent = talent;

		// since the at and pa is based on the talent, we have to reload them if
		// the talent changes
		at = null;
		pa = null;
	}

	public String getSlot() {
		return slot;
	}

	public void setSlot(String slot) {
		this.slot = slot;
	}

	public boolean isBeidhändigerKampf() {
		return beidhändigerKampf;
	}

	public void setBeidhändigerKampf(boolean beidhändigerKampf) {
		this.beidhändigerKampf = beidhändigerKampf;

		hero.clearModifiersCache(getCombatProbeAttacke());
		hero.clearModifiersCache(getCombatProbeDefense());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name + " " + talent + " = " + item != null ? item.getName() : "";
	}

}
