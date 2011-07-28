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
import com.dsatab.data.Hero;
import com.dsatab.data.items.Item;
import com.dsatab.xml.DataManager;

public class InventoryCardAdapter extends BaseAdapter {

	private WeakReference<Bitmap>[] bmps;

	private Context context;

	private int mGalleryItemBackground;

	private int width, height;

	private Hero hero;

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public InventoryCardAdapter(Context context, Hero hero) {

		this.hero = hero;
		this.context = context;

		bmps = (WeakReference<Bitmap>[]) new WeakReference[hero.getItems().size()];

		TypedArray a = context.obtainStyledAttributes(R.styleable.Gallery);
		mGalleryItemBackground = a.getResourceId(R.styleable.Gallery_android_galleryItemBackground, 0);

		width = context.getResources().getDimensionPixelSize(R.dimen.gallery_thumb_width);
		height = context.getResources().getDimensionPixelSize(R.dimen.gallery_thumb_height);
		a.recycle();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return hero.getItems().size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Item getItem(int position) {
		return hero.getItems().get(position);
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

		ImageView i;
		if (convertView instanceof ImageView) {
			i = (ImageView) convertView;
		} else {
			i = new ImageView(context);
		}

		Bitmap bitmap = null;
		WeakReference<Bitmap> ref = bmps[position];
		if (ref != null && ref.get() != null) {
			bitmap = ref.get();
		} else {

			File lqFile = getItem(position).getFile();
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