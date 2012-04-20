/*
 * Copyright (C) 2010 Gandulf Kohlweiss
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.dsatab.xml;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;

import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemSpecification;
import com.dsatab.data.items.ItemType;

/**
 * @author Seraphim
 * 
 */
public class DataManager {

	private static WeakReference<Map<String, Item>> itemsMap;

	private static WeakReference<List<Item>> items;

	private static List<String> cardCategories;

	private static LruCache<String, Bitmap> mMemoryCache;

	private static Map<ItemType, List<String>> cardTypeCategories;

	public static void init(Context context) {

		// Get memory class of this device, exceeding this amount will throw an
		// OutOfMemory exception.
		final int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();

		// Use 1/8th of the available memory for this memory cache.
		final int cacheSize = 1024 * 1024 * memClass / 8;

		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// The cache size will be measured in bytes rather than number
				// of items.
				return bitmap.getRowBytes() * bitmap.getHeight();
			}
		};

	}

	private static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	private static Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}

	public static Map<String, Item> getItemsMap() {
		if (itemsMap == null || itemsMap.get() == null) {
			itemsMap = new WeakReference<Map<String, Item>>(XmlParser.readItems());
		}

		return itemsMap.get();
	}

	public static List<Item> getItems() {

		if (items == null || items.get() == null) {
			Collection<Item> itemsList = getItemsMap().values();

			if (!(itemsList instanceof List))
				itemsList = new ArrayList<Item>(itemsList);

			Collections.sort((List<Item>) itemsList, Item.NAME_COMPARATOR);

			items = new WeakReference<List<Item>>((List<Item>) itemsList);

		}

		return items.get();

	}

	public static Bitmap getBitmap(String path) {
		Bitmap bitmap = getBitmapFromMemCache(path);
		if (bitmap == null) {
			bitmap = BitmapFactory.decodeFile(path);
			addBitmapToMemoryCache(path, bitmap);
		}
		return bitmap;
	}

	public static void reloadItemsMap() {
		itemsMap = null;
	}

	public static Item getItemByName(String name) {
		return getItemsMap().get(name);
	}

	public static List<String> getCardCategories() {

		if (cardCategories == null) {

			Set<String> categoryMap = new HashSet<String>();

			for (Item card : getItemsMap().values()) {
				categoryMap.add(card.getCategory());
			}

			cardCategories = new ArrayList<String>(categoryMap);

		}

		return cardCategories;
	}

	public static List<String> getCardCategories(ItemType cardType) {

		if (cardTypeCategories == null) {

			cardTypeCategories = new HashMap<ItemType, List<String>>();

			for (Item item : getItemsMap().values()) {

				for (ItemSpecification spec : item.getSpecifications()) {
					List<String> categoryList = cardTypeCategories.get(spec.getType());
					if (categoryList == null) {
						categoryList = new LinkedList<String>();
						cardTypeCategories.put(spec.getType(), categoryList);
					}

					if (!categoryList.contains(item.getCategory()))
						categoryList.add(item.getCategory());

				}
			}
		}

		List<String> result = cardTypeCategories.get(cardType);
		if (result == null)
			result = Collections.emptyList();
		return result;
	}

}
