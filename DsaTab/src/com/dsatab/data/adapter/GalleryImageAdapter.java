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
import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.dsatab.R;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemType;
import com.dsatab.view.CardView;
import com.dsatab.xml.DataManager;

public class GalleryImageAdapter extends BaseAdapter {

	private Item[] images;

	private WeakReference<Bitmap>[] bmps;

	private Context context;

	private int mGalleryItemBackground;

	private int width, height;

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public GalleryImageAdapter(Context context, ItemType cardType, String category) {

		this.context = context;

		if (category != null) {
			images = DataManager.getItemsByCategory(category).toArray(new Item[0]);
		} else if (cardType != null) {
			images = DataManager.getItemsByType(cardType).toArray(new Item[0]);
		} else {
			images = DataManager.getItemsMap().values().toArray(new Item[0]);
		}

		bmps = (WeakReference<Bitmap>[]) new WeakReference[images.length];

		TypedArray a = context.obtainStyledAttributes(R.styleable.Gallery);
		mGalleryItemBackground = a.getResourceId(R.styleable.Gallery_android_galleryItemBackground, 0);

		width = context.getResources().getDimensionPixelSize(R.dimen.gallery_thumb_width);
		height = context.getResources().getDimensionPixelSize(R.dimen.gallery_thumb_height);
		a.recycle();
	}

	public int getPosition(Item item) {
		if (item != null) {
			for (int i = 0; i < images.length; i++) {
				if (item.equals(images[i]))
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
		return images.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Item getItem(int position) {
		return images[position];
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
		}

		Bitmap bitmap = null;
		WeakReference<Bitmap> ref = bmps[position];
		if (ref != null && ref.get() != null) {
			bitmap = ref.get();
		} else {
			File lqFile = item.getFile();
			if (lqFile != null && lqFile.isFile()) {
				bitmap = DataManager.getBitmap(lqFile.getAbsolutePath());
				bmps[position] = new WeakReference<Bitmap>(bitmap);
			}
		}

		i.setImageBitmap(bitmap);

		/* Image should be scaled as width/height are set. */
		i.setScaleType(ImageView.ScaleType.FIT_XY);

		/* Set the Width/Height of the ImageView. */
		i.setLayoutParams(new Gallery.LayoutParams(width, height));
		i.setBackgroundResource(mGalleryItemBackground);

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
