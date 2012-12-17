package com.dsatab.data.items;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.jdom2.Element;

import android.text.TextUtils;

import com.dsatab.DSATabApplication;
import com.dsatab.data.ItemLocationInfo;
import com.dsatab.data.XmlWriteable;
import com.dsatab.util.Debug;
import com.dsatab.xml.Xml;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "item")
public class Item implements Serializable, Comparable<Item>, Cloneable, ItemCard, XmlWriteable {

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

	public static final String POSTFIX = ".jpg";

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
	public String path = null;

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

	private List<ItemSpecification> itemSpecs;

	private ItemLocationInfo itemInfo;

	private Boolean hasCardImage;
	private File imageFile;
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

		itemSpecs.add(itemSpecification);

		if (itemSpecification.getType() != null) {
			if (TextUtils.isEmpty(itemTypes))
				itemTypes = ";";
			itemTypes = itemTypes.concat(itemSpecification.getType().name() + ";");
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.items.ItemCard#getItem()
	 */
	@Override
	public Item getItem() {
		return this;
	}

	public String getTitle() {
		if (title != null)
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

	public String getPath() {
		return path;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setPath(String path) {
		if (TextUtils.isEmpty(path))
			path = null;

		this.imageFile = null;
		this.hasCardImage = null;
		this.path = path;
	}

	public File getFile() {
		if (imageFile == null) {
			if (!TextUtils.isEmpty(path)) {
				imageFile = new File(DSATabApplication.getDirectory(DSATabApplication.DIR_CARDS), path);
				if (!imageFile.exists())
					imageFile = null;
			}

			// try to find a image with title of item in cards directory
			if (imageFile == null && !TextUtils.isEmpty(title)) {
				imageFile = new File(DSATabApplication.getDirectory(DSATabApplication.DIR_CARDS), title + POSTFIX);
				if (!imageFile.exists())
					imageFile = null;
			}

			// try to find a image with name of item in cards directory
			if (imageFile == null && !TextUtils.isEmpty(name)) {
				imageFile = new File(DSATabApplication.getDirectory(DSATabApplication.DIR_CARDS), name + POSTFIX);
				if (!imageFile.exists())
					imageFile = null;
			}

		}
		return imageFile;
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
			File file = getFile();
			hasCardImage = file != null && file.isFile();
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

	public int getResourceId() {
		if (getSpecifications().isEmpty())
			return 0;
		else
			return getSpecifications().get(0).getResourceId();

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
		Item item = null;
		try {
			item = (Item) clone();

			item.id = UUID.randomUUID();
		} catch (CloneNotSupportedException e) {
			Debug.error(e);
		}
		return item;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		Item item = (Item) super.clone();
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

		return item;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.XmlWriteable#populateXml(org.jdom2.Element)
	 */
	@Override
	public void populateXml(Element element) {

		element.setAttribute(Xml.KEY_NAME, getName());
		element.setAttribute(Xml.KEY_ANZAHL, Integer.toString(count));
		element.setAttribute(Xml.KEY_SLOT, slot);

		if (itemInfo != null)
			itemInfo.populateXml(element);
	}

}
