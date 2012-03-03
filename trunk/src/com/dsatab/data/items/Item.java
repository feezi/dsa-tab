package com.dsatab.data.items;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.jdom.Element;

import android.text.TextUtils;

import com.dsatab.DSATabApplication;
import com.dsatab.common.Util;
import com.dsatab.data.ItemLocationInfo;
import com.dsatab.xml.Xml;
import com.gandulf.guilib.util.Debug;

public class Item implements Serializable, Comparable<Item>, Cloneable, ItemCard {

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

	static final String POSTFIX_LQ = "_LQ.gif";
	static final String POSTFIX_HQ = "_HQ.jpg";

	public static final String BLANK_PATH = "blank_w_LQ.gif";
	public static final String BLANK_PATH_HQ = "blank_w_HQ.jpg";

	private transient Element element;

	private UUID id;

	private String name;

	private String title;

	private String category;

	public String path = BLANK_PATH;

	private ItemLocationInfo itemInfo;

	private List<ItemSpecification> itemSpecs;

	private List<EquippedItem> equippedItems;

	private Boolean hasCardImage;

	public Item() {
		id = UUID.randomUUID();
		itemInfo = new ItemLocationInfo();
		itemSpecs = new LinkedList<ItemSpecification>();
	}

	@SuppressWarnings("unchecked")
	public <T extends ItemSpecification> T getSpecification(Class<T> type) {
		for (ItemSpecification itemSpecification : itemSpecs) {
			if (itemSpecification.getClass() == type)
				return (T) itemSpecification;
		}
		return null;
	}

	public List<EquippedItem> getEquippedItems() {
		if (equippedItems == null)
			equippedItems = new LinkedList<EquippedItem>();
		return equippedItems;
	}

	public void addSpecification(ItemSpecification itemSpecification) {
		int version = 0;
		for (ItemSpecification specification : itemSpecs) {
			if (specification.getClass().equals(itemSpecification.getClass())) {
				version++;
			}
		}
		itemSpecification.setVersion(version);

		if (element != null) {
			itemSpecification.setElement(element);
		}
		itemSpecs.add(itemSpecification);
	}

	public List<ItemSpecification> getSpecifications() {
		return itemSpecs;
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
		for (ItemSpecification itemSpecification : itemSpecs) {
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

	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		this.element = element;
		this.itemInfo.setElement(element);

		Element domallgemein = element.getChild(Xml.KEY_MOD_ALLGEMEIN);
		if (domallgemein != null) {
			Element name = domallgemein.getChild(Xml.KEY_NAME);
			if (name != null) {
				title = name.getAttributeValue(Xml.KEY_VALUE);
			}
		}

		for (ItemSpecification specification : itemSpecs) {
			specification.setElement(element);
		}
	}

	public String getCategory() {
		return category;
	}

	public int getCount() {
		Integer count = 1;

		if (element != null) {
			count = Util.parseInt(element.getAttributeValue(Xml.KEY_ANZAHL));
			if (count == null)
				count = 1;
		}

		return count;
	}

	public String getPath() {
		if (path != null)
			return path;
		else
			return null;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setPath(String path) {
		if (TextUtils.isEmpty(path))
			path = BLANK_PATH;

		this.path = path;
	}

	public File getFile() {
		if (getPath() != null)
			return new File(DSATabApplication.getDirectory(DSATabApplication.DIR_CARDS), getPath());
		else
			return null;
	}

	public File getHQFile() {
		if (getHQPath() != null)
			return new File(DSATabApplication.getDirectory(DSATabApplication.DIR_CARDS), getHQPath());
		else
			return null;
	}

	public String getHQPath() {
		if (path != null)
			return path.replace(POSTFIX_LQ, POSTFIX_HQ);
		else
			return null;
	}

	public boolean isEquipable() {
		for (ItemSpecification specification : itemSpecs) {
			if (specification.type.isEquipable())
				return true;
		}
		return false;
	}

	public boolean hasImage() {

		if (hasCardImage == null) {

			File lqFile = getFile();
			if (lqFile == null || !lqFile.isFile())
				hasCardImage = false;
			else
				hasCardImage = (!lqFile.getName().equals(Item.BLANK_PATH));

		}
		return hasCardImage;

	}

	public String getInfo() {
		if (itemSpecs.isEmpty()) {
			if (getCount() > 1)
				return getCount() + " Stück";
			else
				return "";
		} else
			return itemSpecs.get(0).getInfo();
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
		if (itemSpecs.isEmpty())
			return 0;
		else
			return itemSpecs.get(0).getResourceId();

	}

	/**
	 * @return the itemInfo
	 */
	public ItemLocationInfo getItemInfo() {
		return itemInfo;
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
		return item;
	}

}
