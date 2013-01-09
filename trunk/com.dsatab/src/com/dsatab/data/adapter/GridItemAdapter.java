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

import java.util.ArrayList;
import java.util.Comparator;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.dsatab.R;
import com.dsatab.data.filter.ItemCardListFilter;
import com.dsatab.data.items.ItemCard;
import com.dsatab.data.items.ItemType;
import com.dsatab.view.CardView;

public class GridItemAdapter extends OpenArrayAdapter<ItemCard> {

	private ItemCardListFilter filter;

	int width;
	int height;

	public GridItemAdapter(Context context) {
		super(context, 0, new ArrayList<ItemCard>());

		width = getContext().getResources().getDimensionPixelSize(R.dimen.workspace_cell_width);
		height = getContext().getResources().getDimensionPixelSize(R.dimen.workspace_cell_height);
	}

	public void filter(ItemType type, String category, String constraint) {
		getFilter().setType(type);
		filter.setCategory(category);
		filter.filter(constraint);
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see com.dsatab.data.adapter.OpenArrayAdapter#getCount()
	// */
	// @Override
	// public int getCount() {
	// int count = super.getCount();
	// return Math.max(16, count);
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.adapter.OpenArrayAdapter#getItem(int)
	 */
	@Override
	public ItemCard getItem(int position) {
		if (position < mObjects.size())
			return mObjects.get(position);
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.adapter.OpenArrayAdapter#sort(java.util.Comparator)
	 */
	@Override
	public void sort(Comparator<? super ItemCard> comparator) {
		super.sort(comparator);
		propagatePosition();
	}

	public void prepare() {
		sort(ItemCard.CELL_NUMBER_COMPARATOR);
	}

	private void propagatePosition() {
		final int count = mObjects.size();
		ItemCard card;
		for (int i = 0; i < count; i++) {
			card = mObjects.get(i);
			card.getItemInfo().setCellNumber(i);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getFilter()
	 */
	@Override
	public ItemCardListFilter getFilter() {
		if (filter == null)
			filter = new ItemCardListFilter(this);

		return filter;
	}

	public int getPositionByName(ItemCard item) {
		if (item != null) {
			for (int i = 0; i < mObjects.size(); i++) {
				if (item.getTitle().equals(mObjects.get(i).getTitle()))
					return i;
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.adapter.OpenArrayAdapter#notifyDataSetChanged()
	 */
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		CardView cardView;
		ItemCard item = getItem(position);

		if (convertView instanceof CardView) {
			cardView = (CardView) convertView;
		} else {
			cardView = new CardView(getContext());
			if (cardView.getBackground() != null)
				cardView.getBackground().mutate();
			// cardView.setLayoutParams(new GridView.LayoutParams(width,
			// height));
			cardView.setLayoutParams(new AbsListView.LayoutParams(width, height));
			cardView.setMinimumWidth(width);
			cardView.setMinimumHeight(height);
		}
		cardView.setItem(item);
		return cardView;
	}

}
