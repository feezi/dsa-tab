package com.dsatab.data.items;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jdom.Element;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.dsatab.common.DsaTabRuntimeException;
import com.dsatab.common.Util;
import com.dsatab.data.CombatProbe;
import com.dsatab.data.CombatTalent;
import com.dsatab.data.Hero;
import com.dsatab.data.ItemLocationInfo;
import com.dsatab.data.enums.CombatTalentType;
import com.dsatab.exception.InconsistentDataException;
import com.dsatab.util.Debug;
import com.dsatab.xml.DataManager;
import com.dsatab.xml.Xml;

public class EquippedItem implements ItemCard {

	private static final String RUESTUNGSNAME = "ruestungsname";

	private static final String SCHILDNAME = "schildname";

	private static final String WAFFENNAME = "waffenname";

	private static final String NAME_PREFIX_RUESTUNG = "ruestung";

	public static final String NAME_PREFIX_SCHILD = "schild";

	private static final String NAME_PREFIX_NK = "nkwaffe";

	private static final String NAME_PREFIX_FK = "fkwaffe";

	private Element element;

	private Item item = null;

	private UUID id;

	private int nameId;

	private ItemSpecification itemSpecification = null;

	/**
	 * Weapon-Shield combies
	 */
	private EquippedItem secondaryEquippedItem = null;

	private String itemNameField;

	private CombatTalent talent = null;

	private UsageType usageType = null;

	private Hero hero;

	private ItemLocationInfo itemInfo;

	private CombatProbe at, pa;

	public EquippedItem(Hero hero, Element element) {
		this(hero, element, null);
	}

	public EquippedItem(Hero hero, Element element, Item item) {

		assert element != null : "Element has to ne set in equipped item";

		this.id = UUID.randomUUID();
		this.hero = hero;
		this.element = element;
		this.item = item;
		this.itemInfo = new ItemLocationInfo();
		this.itemInfo.setElement(element);

		applyElement();

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

	public String getItemSpecificationLabel() {
		if (element != null) {
			String value = element.getAttributeValue(Xml.KEY_BEZEICHNER);
			if (TextUtils.isEmpty(value))
				return null;
			else
				return value;
		} else {
			return null;
		}
	}

	public void setItemSpecificationLabel(String specLabel) {
		if (element != null) {
			if (specLabel != null)
				element.setAttribute(Xml.KEY_BEZEICHNER, specLabel);
			else
				element.setAttribute(Xml.KEY_BEZEICHNER, "");
		} else
			throw new DsaTabRuntimeException("Setting SpecificationLabel without a DOM element");

	}

	public ItemSpecification getItemSpecification() {
		if (itemSpecification == null) {

			// no choice take to only one present
			if (item.getSpecifications().size() == 1) {
				itemSpecification = item.getSpecifications().get(0);
			} else {
				String specLabel = getItemSpecificationLabel();
				// search for a spec with this name
				if (specLabel != null) {
					for (ItemSpecification itemSpec : item.getSpecifications()) {
						if (specLabel.equals(itemSpec.getSpecificationLabel())) {
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
				for (ItemSpecification specification : item.getSpecifications()) {
					if (isCloseCombatWeapon() && specification instanceof Weapon) {
						if (getTalent(specification) != null) {
							Weapon weapon = (Weapon) specification;
							if (weapon.getCombatTalentTypes().contains(getTalent().getCombatTalentType())) {
								itemSpecification = specification;
								break;
							}
						}
					} else if (isShieldWeapon() && specification instanceof Shield) {
						Shield shield = (Shield) specification;
						if (shield.isParadeWeapon() && getUsageType() == UsageType.Paradewaffe) {
							itemSpecification = specification;
							break;
						} else if (shield.isShield() && getUsageType() == UsageType.Schild) {
							itemSpecification = specification;
							break;
						}
					} else if (isDistanceWeapon() && specification instanceof DistanceWeapon) {
						itemSpecification = specification;
						break;
					}
				}
			}

			// still nothing found, just take the first one without a specLabel
			if (itemSpecification == null && !item.getSpecifications().isEmpty()) {
				for (ItemSpecification itemSpec : item.getSpecifications()) {
					if (itemSpec.getSpecificationLabel() == null) {
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

		}
		return itemSpecification;

	}

	public void setItemSpecification(Context context, ItemSpecification itemSpecification) {
		this.itemSpecification = itemSpecification;
		if (itemSpecification != null)
			setItemSpecificationLabel(itemSpecification.getSpecificationLabel());
		else {
			setItemSpecificationLabel(null);
		}

		if (itemSpecification instanceof Weapon) {
			Weapon weapon = (Weapon) itemSpecification;
			if (getTalent() != null && weapon.getCombatTalentTypes().contains(getTalent().getCombatTalentType())) {
				// talentOk
			} else {

				final List<CombatTalent> combatTalents = hero.getAvailableCombatTalents(weapon);
				if (combatTalents.size() == 1) {
					setTalent(combatTalents.get(0));
				} else if (combatTalents.isEmpty()) {
					Toast.makeText(context, "Es wurde kein verwendbares Talent gefunden.", Toast.LENGTH_LONG).show();
					return;
				} else {
					List<String> talentNames = new ArrayList<String>(combatTalents.size());
					for (CombatTalent combatTalent : combatTalents) {
						talentNames.add(combatTalent.getName());
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle("Wähle ein Talent...");
					builder.setItems(talentNames.toArray(new String[0]), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							setTalent(combatTalents.get(which));
							hero.fireItemChangedEvent(EquippedItem.this);
						}
					});
					builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

						@Override
						public void onCancel(DialogInterface dialog) {
							hero.fireItemChangedEvent(EquippedItem.this);
						}
					});
					builder.show().setCanceledOnTouchOutside(true);
					return;
				}
			}
		} else if (itemSpecification instanceof DistanceWeapon) {
			DistanceWeapon distanceweapon = (DistanceWeapon) itemSpecification;
			if (getTalent() != null && distanceweapon.getCombatTalentType() == getTalent().getCombatTalentType()) {
				// talentOk
			} else {
				CombatTalent talent = hero.getCombatTalent(distanceweapon.getCombatTalentType().name());
				setTalent(talent);
			}
		}

		hero.fireItemChangedEvent(EquippedItem.this);
	}

	public Element getElement() {
		return element;
	}

	public ItemLocationInfo getItemInfo() {
		return itemInfo;
	}

	public UsageType getUsageType() {
		if (usageType == null && element != null && element.getAttribute(Xml.KEY_VERWENDUNGSART) != null)
			usageType = UsageType.valueOf(element.getAttributeValue(Xml.KEY_VERWENDUNGSART));
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
		if (element.getAttribute(Xml.KEY_HAND) != null)
			return Hand.valueOf(element.getAttributeValue(Xml.KEY_HAND));
		else
			return null;
	}

	public UUID getId() {
		return id;
	}

	public int getSet() {
		if (element.getAttribute(Xml.KEY_SET) != null)
			return Util.parseInt(element.getAttributeValue(Xml.KEY_SET));
		else
			return 0;
	}

	public void setSet(int set) {
		element.setAttribute(Xml.KEY_SET, Util.toString(set));
	}

	private void applyElement() {

		if (item != null) {
			if (item.hasSpecification(Weapon.class) || item.hasSpecification(DistanceWeapon.class)) {
				element.setAttribute(WAFFENNAME, item.getName());
			} else if (item.hasSpecification(Shield.class)) {
				element.setAttribute(SCHILDNAME, item.getName());
			} else if (item.hasSpecification(Armor.class)) {
				element.setAttribute(RUESTUNGSNAME, item.getName());
			}

			if (element != null && element.getAttribute(Xml.KEY_NAME) == null) {
				String namePrefix = null;

				if (item.hasSpecification(Weapon.class)) {
					namePrefix = NAME_PREFIX_NK;
				}
				if (item.hasSpecification(DistanceWeapon.class)) {
					namePrefix = NAME_PREFIX_FK;
				}
				if (item.hasSpecification(Shield.class)) {
					namePrefix = NAME_PREFIX_SCHILD;
				}
				if (item.hasSpecification(Armor.class)) {
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

		if (element.getAttribute(Xml.KEY_SLOT) == null)
			element.setAttribute(Xml.KEY_SLOT, "0");
		if (getName().startsWith(NAME_PREFIX_NK)) {
			itemNameField = WAFFENNAME;
			nameId = Util.parseInt(getName().substring(NAME_PREFIX_NK.length()));
		} else if (getName().startsWith(NAME_PREFIX_FK)) {
			itemNameField = WAFFENNAME;
			nameId = Util.parseInt(getName().substring(NAME_PREFIX_FK.length()));
			if (getItem().hasSpecification(DistanceWeapon.class)) {
				DistanceWeapon weapon = getItem().getSpecification(DistanceWeapon.class);
				talent = hero.getCombatTalent(weapon.getCombatTalentType().getName());
			}
		} else if (getName().startsWith(NAME_PREFIX_SCHILD)) {
			itemNameField = SCHILDNAME;
			nameId = Util.parseInt(getName().substring(NAME_PREFIX_SCHILD.length()));

			Item item = DataManager.getItemByName(getItemName());

			if (item != null && item.hasSpecification(Shield.class)) {
				Shield shield = item.getSpecification(Shield.class);

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

	public boolean isShieldWeapon() {
		return getName().startsWith(NAME_PREFIX_SCHILD);
	}

	public boolean isCloseCombatWeapon() {
		return getName().startsWith(NAME_PREFIX_NK);
	}

	public boolean isDistanceWeapon() {
		return getName().startsWith(NAME_PREFIX_FK);
	}

	public boolean isArmor() {
		return getName().startsWith(NAME_PREFIX_RUESTUNG);
	}

	public Hero getHero() {
		return hero;
	}

	public String getName() {
		return element.getAttributeValue(Xml.KEY_NAME);
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

	public void setName(String name) {
		element.setAttribute(Xml.KEY_NAME, name);
	}

	public String getItemName() {
		return element.getAttributeValue(itemNameField);
	}

	public String getItemSlot() {
		return element.getAttributeValue(Xml.KEY_SLOT);
	}

	public Item getItem() {
		if (item == null && getItemName() != null) {
			item = hero.getItem(getItemName(), getItemSlot());

			if (item == null) {
				BugSenseHandler.log(Debug.CATEGORY_DATA, new InconsistentDataException(
						"Unable to find an item with the name '" + getItemName() + "' in slot '" + getItemSlot()
								+ "'. xml=" + element.toString()));
			}
		}

		return item;
	}

	public EquippedItem getSecondaryItem() {
		return secondaryEquippedItem;
	}

	public void setSecondaryItem(EquippedItem secondaryEquippedItem) {

		this.secondaryEquippedItem = secondaryEquippedItem;

		if (getItemSpecification() instanceof Weapon) {
			if (secondaryEquippedItem == null) {
				element.setAttribute(Xml.KEY_SCHILD, "0");
			} else if (secondaryEquippedItem.getItemSpecification() instanceof Shield) {
				String name = secondaryEquippedItem.getName();
				int mySchildIndex = Util.parseInt(name.substring(NAME_PREFIX_SCHILD.length()));
				element.setAttribute(Xml.KEY_SCHILD, Util.toString(mySchildIndex));
			} else if (secondaryEquippedItem.getItemSpecification() instanceof Weapon) {
				getHero().addBeidhaendigerKampf(this, secondaryEquippedItem);
			}
		}

	}

	private String getTalentName() {
		if (element.getAttribute(Xml.KEY_TALENT) != null)
			return element.getAttributeValue(Xml.KEY_TALENT);
		else
			return null;
	}

	public CombatTalent getTalent() {
		return getTalent(null);
	}

	protected CombatTalent getTalent(ItemSpecification itemSpecification) {
		if (talent == null) {
			if (getTalentName() != null) {
				talent = hero.getCombatTalent(getTalentName());
			} else {
				if (itemSpecification == null) {
					itemSpecification = getItemSpecification();
				}
				// search for the default talents of the items
				if (itemSpecification instanceof Weapon) {
					Weapon weapon = (Weapon) itemSpecification;
					for (CombatTalentType type : weapon.getCombatTalentTypes()) {
						talent = hero.getCombatTalent(type.getName());
						if (talent != null) {
							break;
						}
					}
				} else if (itemSpecification instanceof DistanceWeapon) {
					DistanceWeapon weapon = (DistanceWeapon) itemSpecification;
					talent = hero.getCombatTalent(weapon.getCombatTalentType().getName());
				} else if (itemSpecification instanceof Shield) {
					Shield shield = (Shield) itemSpecification;
					if (shield.isShield()) {
						talent = hero.getCombatShieldTalent();
					} else {
						// paradeweapon
						for (CombatTalentType type : shield.getCombatTalentTypes()) {
							talent = hero.getCombatTalent(type.getName());
							if (talent != null) {
								break;
							}
						}
					}
				}
			}
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
		return element.getAttributeValue(Xml.KEY_SLOT);
	}

	public void setSlot(String slot) {
		element.setAttribute(Xml.KEY_SLOT, slot);
	}

}
