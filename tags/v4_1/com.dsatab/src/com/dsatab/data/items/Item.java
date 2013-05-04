package com.dsatab.data.items;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import android.net.Uri;
import android.text.TextUtils;

import com.dsatab.data.ItemLocationInfo;
import com.dsatab.util.Util;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "item")
public class Item implements Serializable, Comparable<Item>, Cloneable, ItemCard {

	/**
	 * 
	 */
	public static final String ITEM_TYPES_SEP = ";";

	private static final long serialVersionUID = 7011220901677479470L;

	public static Comparator<Item> NAME_COMPARATOR = new Comparator<Item>() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Item object1, Item object2) {
			return object1.getName().compareToIgnoreCase(object2.getName());
		}
	};

	@DatabaseField(id = true, columnName = "_id")
	private UUID id;

	@DatabaseField
	private String name;
	@DatabaseField
	private String title;
	@DatabaseField
	private String category;
	@DatabaseField
	private String itemTypes;
	@DatabaseField
	public String imageUriHelper = null;
	@DatabaseField
	public String iconUriHelper = null;
	@DatabaseField
	public boolean imageTextOverlay = true;
	/**
	 * in kreuzer
	 */
	@DatabaseField
	private int price;
	/**
	 * in unzen
	 */
	@DatabaseField
	private float weight;

	// we need these wrapper since ormlite does not inheritance for
	// ItemSpecification yet. All these collections will be merged into
	// itemSpecs which is used by the app
	@ForeignCollectionField(eager = true)
	private ForeignCollection<Weapon> weaponSpecsHelper;
	@ForeignCollectionField(eager = true)
	private ForeignCollection<Shield> shieldSpecsHelper;
	@ForeignCollectionField(eager = true)
	private ForeignCollection<DistanceWeapon> distanceWeaponSpecsHelper;
	@ForeignCollectionField(eager = true)
	private ForeignCollection<Armor> armorSpecsHelper;
	@ForeignCollectionField(eager = true)
	private ForeignCollection<MiscSpecification> miscSpecsHelper;

	// transient fields

	private List<ItemSpecification> itemSpecs;

	private ItemLocationInfo itemInfo;

	private Boolean hasCardImage;
	private Uri iconUri = null, imageUri = null;
	private int count;
	private String slot;

	public Item() {
		id = UUID.randomUUID();
		itemInfo = new ItemLocationInfo();

		slot = "0";
		count = 1;
	}

	@SuppressWarnings("unchecked")
	public <T extends ItemSpecification> T getSpecification(Class<T> type) {
		for (ItemSpecification itemSpecification : getSpecifications()) {
			if (itemSpecification.getClass() == type)
				return (T) itemSpecification;
		}
		return null;
	}

	public void addSpecification(ItemSpecification itemSpecification) {
		int version = 0;
		for (ItemSpecification specification : getSpecifications()) {
			if (specification.getClass().equals(itemSpecification.getClass())) {
				version++;
			}
		}
		itemSpecification.setVersion(version);
		itemSpecification.setItem(this);

		itemSpecs.add(itemSpecification);

		if (itemSpecification.getType() != null) {
			if (TextUtils.isEmpty(itemTypes))
				itemTypes = ITEM_TYPES_SEP;
			itemTypes = itemTypes.concat(itemSpecification.getType().name() + ITEM_TYPES_SEP);
		}
	}

	public List<ItemSpecification> getSpecifications() {
		if (itemSpecs == null) {
			itemSpecs = new ArrayList<ItemSpecification>();
			if (weaponSpecsHelper != null)
				itemSpecs.addAll(weaponSpecsHelper);
			if (shieldSpecsHelper != null)
				itemSpecs.addAll(shieldSpecsHelper);
			if (distanceWeaponSpecsHelper != null)
				itemSpecs.addAll(distanceWeaponSpecsHelper);
			if (armorSpecsHelper != null)
				itemSpecs.addAll(armorSpecsHelper);
			if (miscSpecsHelper != null)
				itemSpecs.addAll(miscSpecsHelper);

			if (itemSpecs.isEmpty()) {
				addSpecification(new MiscSpecification(this, ItemType.Sonstiges));
			}
		}
		return Collections.unmodifiableList(itemSpecs);
	}

	public List<String> getSpecificationNames() {
		List<String> specInfo = new ArrayList<String>(getSpecifications().size());

		for (ItemSpecification itemSpec : getSpecifications()) {
			specInfo.add(itemSpec.getName()
					+ (itemSpec.getSpecificationLabel() != null ? "(" + itemSpec.getSpecificationLabel() + ")" : "")
					+ ": " + itemSpec.getInfo());
		}

		return specInfo;
	}

	public boolean hasSpecification(Class<? extends ItemSpecification> type) {
		for (ItemSpecification itemSpecification : getSpecifications()) {
			if (itemSpecification.getClass() == type)
				return true;
		}
		return false;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSlot() {
		return slot;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public boolean isImageTextOverlay() {
		return imageTextOverlay;
	}

	public void setImageTextOverlay(boolean imageTextOverlay) {
		this.imageTextOverlay = imageTextOverlay;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.items.ItemCard#getItem()
	 */
	@Override
	public Item getItem() {
		return this;
	}

	public boolean hasTitle() {
		return !TextUtils.isEmpty(title);
	}

	public String getTitle() {
		if (!TextUtils.isEmpty(title))
			return title;
		else
			return getName();
	}

	public String getCategory() {
		return category;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setSlot(String slot) {
		this.slot = slot;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setTitle(String title) {
		if (TextUtils.isEmpty(title))
			this.title = null;
		else
			this.title = title;
	}

	public Uri getImageUri() {
		if (imageUri == null && imageUriHelper != null) {
			imageUri = Uri.parse(imageUriHelper);
			hasCardImage = true;
		}
		return imageUri;
	}

	public boolean isEquipable() {
		for (ItemSpecification specification : getSpecifications()) {
			if (specification.type.isEquipable())
				return true;
		}
		return false;
	}

	public boolean hasImage() {
		if (hasCardImage == null) {
			hasCardImage = getImageUri() != null;
		}
		return hasCardImage;
	}

	public String getInfo() {
		if (getSpecifications().isEmpty()) {
			if (getCount() > 1)
				return getCount() + " Stück";
			else
				return "";
		} else
			return getSpecifications().get(0).getInfo();
	}

	@Override
	public int compareTo(Item another) {

		int comp1 = 0;
		if (getCategory() != null && another.getCategory() != null)
			comp1 = getCategory().compareToIgnoreCase(another.getCategory());
		else if (getCategory() == null)
			comp1 = -1;
		else if (another.getCategory() == null)
			comp1 = 1;
		else
			comp1 = 0;

		int comp2 = getName().compareToIgnoreCase(another.getName());
		int comp3 = getId().compareTo(another.getId());

		return comp1 * 10000 + comp2 * 100 + comp3;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {

		if (o == null) {
			return false;
		}

		if (!o.getClass().equals(this.getClass()))
			return false;

		Item otherItem = (Item) o;

		if (!otherItem.getId().equals(getId()))
			return false;

		return true;
	}

	public Uri getIconUri() {
		if (iconUri == null && iconUriHelper != null) {
			iconUri = Uri.parse(iconUriHelper);
		}

		if (iconUri != null) {
			return iconUri;
		} else {
			if (getSpecifications().isEmpty())
				return null;
			else
				return Util.getUriForResourceId(getSpecifications().get(0).getResourceId());
		}
	}

	public void setIconUri(Uri iconUri) {
		this.iconUri = iconUri;
		if (iconUri != null)
			this.iconUriHelper = iconUri.toString();
		else
			this.iconUriHelper = null;
	}

	public void setImageUri(Uri imageUri) {
		this.imageUri = imageUri;
		if (imageUri != null) {
			this.imageUriHelper = imageUri.toString();
			this.hasCardImage = true;
		} else {
			this.hasCardImage = false;
			this.imageUriHelper = null;
		}
	}

	/**
	 * @return the itemInfo
	 */
	public ItemLocationInfo getItemInfo() {
		return itemInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}

	public Item duplicate() {
		Item item = this.clone();
		item.id = UUID.randomUUID();
		return item;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Item clone() {
		Item item;
		try {
			item = (Item) super.clone();
			item.itemInfo = (ItemLocationInfo) itemInfo.clone();

			item.itemSpecs = new ArrayList<ItemSpecification>(getSpecifications().size());

			for (ItemSpecification specification : getSpecifications()) {
				item.itemSpecs.add(specification.clone());
			}

			item.weaponSpecsHelper = null;
			item.shieldSpecsHelper = null;
			item.distanceWeaponSpecsHelper = null;
			item.armorSpecsHelper = null;
			item.miscSpecsHelper = null;
		} catch (CloneNotSupportedException e) {
			return null;
		}

		return item;
	}

}
