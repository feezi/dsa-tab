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
package com.dsatab.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemSpecification;
import com.dsatab.util.Debug;

/**
 * @author Seraphim
 * 
 */
public class ItemListItem extends CheckableTwoLineListItem {

	private int textColor = 0;

	public ItemListItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	public ItemListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public ItemListItem(Context context) {
		super(context);
		init(null);
	}

	/**
	 * @param textColor
	 *            the textColor to set
	 */
	public void setTextColor(int textColor) {
		this.textColor = textColor;

		if (getText1() != null && (textColor != Color.TRANSPARENT)) {
			getText1().setTextColor(textColor);
		}
		if (getText2() != null && (textColor != Color.TRANSPARENT)) {
			getText2().setTextColor(textColor);
		}
	}

	/**
	 * @param attrs
	 */
	private void init(AttributeSet attrs) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.TwoLineListItem#onFinishInflate()
	 */
	@Override
	protected void onFinishInflate() {
		if (getText1() != null && (textColor != Color.TRANSPARENT)) {
			getText1().setTextColor(textColor);
		}
		if (getText2() != null && (textColor != Color.TRANSPARENT)) {
			getText2().setTextColor(textColor);
		}

		if (getIcon1() != null) {
			getIcon1().setFocusable(false);
			getIcon1().setClickable(false);
		}
		if (getIcon2() != null) {
			getIcon2().setVisibility(View.GONE);
			getIcon2().setFocusable(false);
			getIcon2().setClickable(false);
		}

		super.onFinishInflate();
	}

	public ImageView getIcon1() {
		return (ImageView) findViewById(android.R.id.icon1);
	}

	public ImageView getIcon2() {
		return (ImageView) findViewById(android.R.id.icon2);
	}

	public void setItem(Item e) {
		if (e.getSpecifications().isEmpty())
			Debug.error("Item without spec found " + e.getName());
		setItem(e, e.getSpecifications().get(0));
	}

	public void setItem(Item e, ItemSpecification spec) {

		ImageView icon1 = getIcon1();

		if (icon1 != null) {
			icon1.setVisibility(View.VISIBLE);
			if (spec != null)
				icon1.setImageResource(spec.getResourceId());
			else
				icon1.setImageResource(e.getResourceId());
		}
		// Set value for the first text field
		if (getText1() != null) {
			getText1().setText(e.getTitle());
			if (textColor != Color.TRANSPARENT)
				getText1().setTextColor(textColor);
		}

		// set value for the second text field
		if (getText2() != null) {
			getText2().setText(spec.getInfo());
			if (textColor != Color.TRANSPARENT)
				getText2().setTextColor(textColor);
		}
	}

}
