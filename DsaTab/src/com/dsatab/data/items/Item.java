﻿package com.dsatab.data.items;

import java.io.File;
import java.io.Serializable;
import java.util.UUID;

import org.w3c.dom.Element;

import android.text.TextUtils;

import com.dsatab.R;
import com.dsatab.activity.DSATabApplication;
import com.dsatab.view.drag.ItemInfo;
import com.dsatab.xml.DomUtil;
import com.dsatab.xml.Xml;
import com.gandulf.guilib.util.Debug;

public class Item implements Serializable, Comparable<Item>, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7011220901677479470L;

	static final String POSTFIX_LQ = "_LQ.gif";
	static final String POSTFIX_HQ = "_HQ.jpg";

	static final String BLANK_PATH = "blank_LQ.gif";

	private ItemType type;

	private transient Element element;

	private UUID id;

	private String name;

	private String title;

	private String category;

	public String path = BLANK_PATH;

	private ItemInfo itemInfo;

	public Item() {
		id = UUID.randomUUID();
		itemInfo = new ItemInfo();
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

		if (element.hasAttribute(Xml.KEY_PATH)) {
			path = element.getAttribute(Xml.KEY_PATH);
		} else if (path != null) {
			element.setAttribute(Xml.KEY_PATH, path);
		}

		Element domallgemein = DomUtil.getChildByTagName(element, Xml.KEY_MOD_ALLGEMEIN);
		if (domallgemein != null) {
			Element name = DomUtil.getChildByTagName(domallgemein, Xml.KEY_NAME);
			if (name != null) {
				title = name.getAttribute(Xml.KEY_VALUE);
			}
		}
	}

	public ItemType getType() {
		return type;
	}

	public String getCategory() {
		return category;
	}

	public String getPath() {
		if (type != null && path != null)
			return type.getPath() + "/" + path;
		else
			return null;
	}

	public void setType(ItemType type) {
		this.type = type;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setPath(String path) {
		if (TextUtils.isEmpty(path))
			path = BLANK_PATH;

		this.path = path;

		if (element != null) {
			element.setAttribute(Xml.KEY_PATH, path);
		}
	}

	public File getFile() {
		if (getPath() != null)
			return new File(DSATabApplication.getDsaTabPath() + "cards/" + getPath());
		else
			return null;
	}

	public File getHQFile() {
		if (getHQPath() != null)
			return new File(DSATabApplication.getDsaTabPath() + "cards/" + getHQPath());
		else
			return null;
	}

	public String getHQPath() {
		if (path != null && type != null)
			return type.getPath() + "/" + path.replace(POSTFIX_LQ, POSTFIX_HQ);
		else
			return null;
	}

	public String getInfo() {
		return "";
	}

	@Override
	public int compareTo(Item another) {

		int comp0 = 0;
		if (getType() != null && another.getType() != null)
			comp0 = getType().compareTo(another.getType());
		else if (getType() == null)
			comp0 = -1;
		else if (another.getType() == null)
			comp0 = 1;
		else
			comp0 = 0;

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

		return comp0 * 1000000 + comp1 * 10000 + comp2 * 100 + comp3;
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
		if (!otherItem.getName().equals(getName()))
			return false;

		if (!otherItem.getId().equals(getId()))
			return false;

		if (getItemInfo() != null) {
			if (!getItemInfo().equals(otherItem.getItemInfo()))
				return false;
		} else if (otherItem.getItemInfo() != null) {
			return false;
		}

		return true;
	}

	public int getResourceId() {
		switch (type) {
		case Behälter:
			return R.drawable.icon_bags;
		case Special:
			return R.drawable.icon_special;
		case Kleidung:
			return R.drawable.icon_armor_cloth;
		case Sonstiges:
			return R.drawable.icon_misc;
		default:
			return R.drawable.icon_items;
		}

	}

	/**
	 * @return the itemInfo
	 */
	public ItemInfo getItemInfo() {
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
		item.itemInfo = (ItemInfo) itemInfo.clone();
		return item;
	}

}
