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
package com.dsatab.data.adapter;

import java.io.File;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;

import com.dsatab.R;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemType;
import com.dsatab.view.CardView;
import com.dsatab.xml.DataManager;

public class GalleryImageAdapter extends BaseAdapter {

	private Item[] items;

	private Context context;

	private int mGalleryItemBackground;

	private int width, height;

	public GalleryImageAdapter(Context context, ItemType cardType, String category, Item[] items) {

		this.context = context;

		if (items == null) {
			if (category != null) {
				this.items = DataManager.getItemsByCategory(category).toArray(new Item[0]);
			} else if (cardType != null) {
				this.items = DataManager.getItemsByType(cardType).toArray(new Item[0]);
			} else {
				this.items = DataManager.getItemsMap().values().toArray(new Item[0]);
			}
		} else {
			this.items = items;
		}

		TypedArray a = context.obtainStyledAttributes(R.styleable.Gallery);
		mGalleryItemBackground = a.getResourceId(R.styleable.Gallery_android_galleryItemBackground, 0);

		width = context.getResources().getDimensionPixelSize(R.dimen.gallery_thumb_width);
		height = context.getResources().getDimensionPixelSize(R.dimen.gallery_thumb_height);
		a.recycle();
	}

	public int getPosition(Item item) {
		if (item != null) {
			for (int i = 0; i < items.length; i++) {
				if (item.equals(items[i]))
					return i;
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return items.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Item getItem(int position) {
		return items[position];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		CardView i;
		Item item = getItem(position);

		if (convertView instanceof CardView) {
			i = (CardView) convertView;
			i.setItem(item);
		} else {
			i = new CardView(context, item);
			/* Set the Width/Height of the ImageView. */
			i.setMaxWidth(width);
			i.setMaxHeight(height);
			i.setLayoutParams(new Gallery.LayoutParams(width, height));
			i.setBackgroundResource(mGalleryItemBackground);
		}

		Bitmap bitmap = null;

		File lqFile = item.getFile();
		if (lqFile != null && lqFile.isFile()) {
			bitmap = DataManager.getBitmap(lqFile.getAbsolutePath());
		}
		i.setImageBitmap(bitmap);

		return i;
	}

	/**
	 * Returns the size (0.0f to 1.0f) of the views
	 * 
	 * depending on the 'offset' to the center.
	 */
	public float getScale(boolean focused, int offset) {
		/* Formula: 1 / (2 ^ offset) */
		return Math.max(0, 1.0f / (float) Math.pow(2, Math.abs(offset)));
	}
}
