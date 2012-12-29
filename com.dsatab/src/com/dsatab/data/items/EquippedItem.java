package com.dsatab.data.items;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.dsatab.data.CombatProbe;
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

	private String slot, name, talentName, itemSpecificationLabel;

	private Integer schildIndex;

	public EquippedItem(Hero hero) {
		this.id = UUID.randomUUID();
		this.hero = hero;
		this.itemInfo = new ItemLocationInfo();
	}

	public EquippedItem(Hero hero, CombatTalent talent, Item item) {
		this(hero);
		setTalent(talent);
		setItem(item);
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

	public String getItemSpecificationLabel() {
		return itemSpecificationLabel;
	}

	public void setItemSpecificationLabel(String specLabel) {
		this.itemSpecificationLabel = specLabel;
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
						Debug.verbose("Could not find a specific itemspecificaton for " + toString() + " using "
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
		} else if (itemSpecification instanceof Shield) {
			Shield shield = (Shield) itemSpecification;

			if (getUsageType() == null) {
				if (shield.isShield()) {
					setUsageType(UsageType.Schild);
				} else if (shield.isParadeWeapon()) {
					setUsageType(UsageType.Paradewaffe);
				}
			}

			if (shield.isShield() && getUsageType() == UsageType.Schild)
				setTalent(hero.getCombatShieldTalent());
			else if (shield.isParadeWeapon() && getUsageType() == UsageType.Paradewaffe)
				setTalent(hero.getCombatParadeWeaponTalent(this));

		}

		hero.fireItemChangedEvent(EquippedItem.this);
	}

	public ItemLocationInfo getItemInfo() {
		return itemInfo;
	}

	public UsageType getUsageType() {
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

	public void setTalentName(String talentName) {
		this.talentName = talentName;
	}

	public void setSchildIndex(Integer schildIndex) {
		this.schildIndex = schildIndex;
	}

	public boolean isShieldWeapon() {
		return name.startsWith(NAME_PREFIX_SCHILD);
	}

	public boolean isCloseCombatWeapon() {
		return name.startsWith(NAME_PREFIX_NK);
	}

	public boolean isDistanceWeapon() {
		return name.startsWith(NAME_PREFIX_FK);
	}

	public boolean isArmor() {
		return name.startsWith(NAME_PREFIX_RUESTUNG);
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

	public void setItem(Item item) {
		this.item = item;

		if (talent == null && talentName == null && name != null) {
			if (isDistanceWeapon()) {
				DistanceWeapon distanceWeapon = item.getSpecification(DistanceWeapon.class);
				if (distanceWeapon != null) {
					setTalent(hero.getCombatTalent(distanceWeapon.getCombatTalentType().getName()));
				}
			}

			if (isShieldWeapon()) {
				Shield shield = item.getSpecification(Shield.class);
				if (shield != null) {

					if (getUsageType() == null) {
						if (shield.isShield()) {
							setUsageType(UsageType.Schild);
						} else if (shield.isParadeWeapon()) {
							setUsageType(UsageType.Paradewaffe);
						}
					}

					if (shield.isShield() && getUsageType() == UsageType.Schild)
						setTalent(hero.getCombatShieldTalent());
					else if (shield.isParadeWeapon() && getUsageType() == UsageType.Paradewaffe)
						setTalent(hero.getCombatParadeWeaponTalent(this));
				}
			}
		}
	}

	public EquippedItem getSecondaryItem() {
		return secondaryEquippedItem;
	}

	public void setSecondaryItem(EquippedItem secondaryEquippedItem) {

		this.secondaryEquippedItem = secondaryEquippedItem;

		if (getItemSpecification() instanceof Weapon) {
			if (secondaryEquippedItem == null) {
				schildIndex = 0;
			} else if (secondaryEquippedItem.getItemSpecification() instanceof Shield) {
				String name = secondaryEquippedItem.getName();
				int mySchildIndex = Util.parseInteger(name.substring(NAME_PREFIX_SCHILD.length()));
				schildIndex = mySchildIndex;
			} else if (secondaryEquippedItem.getItemSpecification() instanceof Weapon) {
				getHero().addBeidhaendigerKampf(this, secondaryEquippedItem);
			}
		}

	}

	private String getTalentName() {
		return talentName;
	}

	public CombatTalent getTalent() {
		return getTalent(null);
	}

	protected CombatTalent getTalent(ItemSpecification itemSpecification) {
		if (talent == null) {
			if (getTalentName() != null) {
				setTalent(hero.getCombatTalent(getTalentName()));
			} else {
				if (itemSpecification == null) {
					itemSpecification = getItemSpecification();
				}
				// search for the default talents of the items
				if (itemSpecification instanceof Weapon) {
					Weapon weapon = (Weapon) itemSpecification;
					for (CombatTalentType type : weapon.getCombatTalentTypes()) {
						setTalent(hero.getCombatTalent(type.getName()));
						if (talent != null) {
							break;
						}
					}
				} else if (itemSpecification instanceof DistanceWeapon) {
					DistanceWeapon weapon = (DistanceWeapon) itemSpecification;
					setTalent(hero.getCombatTalent(weapon.getCombatTalentType().getName()));
				} else if (itemSpecification instanceof Shield) {
					Shield shield = (Shield) itemSpecification;
					if (shield.isShield()) {
						setTalent(hero.getCombatShieldTalent());
					} else {
						// paradeweapon
						for (CombatTalentType type : shield.getCombatTalentTypes()) {
							setTalent(hero.getCombatTalent(type.getName()));
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
