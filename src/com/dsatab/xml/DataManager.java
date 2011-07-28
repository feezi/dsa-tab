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

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemSpecification;
import com.dsatab.data.items.ItemType;

/**
 * @author Seraphim
 * 
 */
public class DataManager {

	private static WeakReference<Map<String, Item>> itemsMap;

	private static SoftReference<Map<String, List<Item>>> cardsCategoryMap;

	private static SoftReference<Map<ItemType, List<Item>>> cardsTypeMap;

	private static SoftReference<List<String>> cardCategories;

	private static SoftReference<Map<ItemType, List<String>>> cardTypeCategories;

	private static SoftReference<Map<String, SoftReference<Bitmap>>> bitmapsMap;

	public static Map<String, Item> getItemsMap() {
		if (itemsMap == null || itemsMap.get() == null) {
			itemsMap = new WeakReference<Map<String, Item>>(XmlParser.readItems());
		}

		return itemsMap.get();
	}

	public static Bitmap getBitmap(String path) {

		Map<String, SoftReference<Bitmap>> b = null;
		if (bitmapsMap == null || bitmapsMap.get() == null) {
			b = new HashMap<String, SoftReference<Bitmap>>();
			bitmapsMap = new SoftReference<Map<String, SoftReference<Bitmap>>>(b);
		} else {
			b = bitmapsMap.get();
		}

		SoftReference<Bitmap> bitmapRef = b.get(path);

		Bitmap bitmap = null;
		if (bitmapRef == null || bitmapRef.get() == null) {
			bitmap = BitmapFactory.decodeFile(path);

			bitmapRef = new SoftReference<Bitmap>(bitmap);
			b.put(path, bitmapRef);
		} else {
			bitmap = bitmapRef.get();
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

		if (cardCategories == null || cardCategories.get() == null) {

			Set<String> categoryMap = new HashSet<String>();

			for (Item card : getItemsMap().values()) {
				categoryMap.add(card.getCategory());
			}

			List<String> categoryList = new ArrayList<String>(categoryMap);
			cardCategories = new SoftReference<List<String>>(categoryList);
		}

		List<String> list = cardCategories.get();
		return list;
	}

	public static List<String> getCardCategories(ItemType cardType) {

		if (cardTypeCategories == null || cardTypeCategories.get() == null) {

			Map<ItemType, List<String>> categoriesMap = new HashMap<ItemType, List<String>>();

			for (Item item : getItemsMap().values()) {

				for (ItemSpecification spec : item.getSpecifications()) {
					List<String> categoryList = categoriesMap.get(spec.getType());
					if (categoryList == null) {
						categoryList = new LinkedList<String>();
						categoriesMap.put(spec.getType(), categoryList);
					}

					if (!categoryList.contains(item.getCategory()))
						categoryList.add(item.getCategory());

				}
			}

			cardTypeCategories = new SoftReference<Map<ItemType, List<String>>>(categoriesMap);
		}

		List<String> result = cardTypeCategories.get().get(cardType);
		if (result == null)
			result = Collections.emptyList();
		return result;
	}

	public static List<Item> getItemsByCategory(String itemCategory) {

		if (itemCategory == null)
			return Collections.emptyList();

		if (cardsCategoryMap == null || cardsCategoryMap.get() == null) {

			Map<String, List<Item>> categoryMap = new HashMap<String, List<Item>>();
			cardsCategoryMap = new SoftReference<Map<String, List<Item>>>(categoryMap);

			for (Item card : getItemsMap().values()) {
				List<Item> nameCards = categoryMap.get(card.getCategory());
				if (nameCards == null) {
					nameCards = new LinkedList<Item>();
					categoryMap.put(card.getCategory(), nameCards);
				}
				nameCards.add(card);
			}

		}

		Map<String, List<Item>> nameMap = cardsCategoryMap.get();

		List<Item> result = nameMap.get(itemCategory);

		if (result == null)
			result = Collections.emptyList();
		return result;
	}

	public static List<Item> getItemsByType(ItemType itemCategory) {

		if (itemCategory == null)
			return Collections.emptyList();

		if (cardsTypeMap == null || cardsTypeMap.get() == null) {

			Map<ItemType, List<Item>> categoryMap = new HashMap<ItemType, List<Item>>();
			cardsTypeMap = new SoftReference<Map<ItemType, List<Item>>>(categoryMap);

			for (Item item : getItemsMap().values()) {

				for (ItemSpecification spec : item.getSpecifications()) {
					List<Item> nameCards = categoryMap.get(spec.getType());
					if (nameCards == null) {
						nameCards = new LinkedList<Item>();
						categoryMap.put(spec.getType(), nameCards);
					}
					nameCards.add(item);
				}
			}

			for (List<Item> items : categoryMap.values()) {
				Collections.sort(items);
			}

		}

		Map<ItemType, List<Item>> nameMap = cardsTypeMap.get();

		List<Item> result = nameMap.get(itemCategory);

		if (result == null)
			result = Collections.emptyList();
		return result;
	}

}