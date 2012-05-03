package com.dsatab.data.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.Hero;
import com.dsatab.data.filter.ItemListFilter;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemType;
import com.dsatab.view.EquippedItemListItem;

public class EquippedItemListAdapter extends OpenArrayAdapter<Item> implements OnClickListener {

	private Hero hero;

	private ItemListFilter filter;

	private LayoutInflater inflater;

	public EquippedItemListAdapter(Context context, Hero hero, Item[] objects) {
		super(context, 0, objects);
		this.hero = hero;
		inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public EquippedItemListAdapter(Context context, Hero hero, List<Item> objects) {
		super(context, 0, objects);
		this.hero = hero;
		inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void filter(List<ItemType> type, String category, String constraint) {
		getFilter().setTypes(type);
		filter.setCategory(category);
		filter.filter(constraint);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getFilter()
	 */
	@Override
	public ItemListFilter getFilter() {
		if (filter == null)
			filter = new ItemListFilter(this);

		return filter;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// View view = super.getView(position, convertView, parent);

		EquippedItemListItem view;
		if (!(convertView instanceof EquippedItemListItem)) {
			view = (EquippedItemListItem) inflater.inflate(R.layout.equippeditem_listitem, parent, false);
		} else {
			view = (EquippedItemListItem) convertView;
		}

		Item item = getItem(position);
		view.setItem(item);

		if (item.isEquipable()) {

			view.getSet1().setSelected(false);
			view.getSet1().setOnClickListener(this);
			view.getSet1().setTag(item);
			view.getSet2().setSelected(false);
			view.getSet2().setOnClickListener(this);
			view.getSet2().setTag(item);
			view.getSet3().setSelected(false);
			view.getSet3().setOnClickListener(this);
			view.getSet3().setTag(item);

			for (EquippedItem equippedItem : item.getEquippedItems()) {
				switch (equippedItem.getSet()) {
				case 0:
					view.getSet1().setSelected(true);
					view.getSet1().setTag(equippedItem);
					break;
				case 1:
					view.getSet2().setSelected(true);
					view.getSet2().setTag(equippedItem);
					break;
				case 2:
					view.getSet3().setSelected(true);
					view.getSet3().setTag(equippedItem);
					break;
				}
			}
		} else {
			view.getSet1().setTag(null);
			view.getSet2().setTag(null);
			view.getSet3().setTag(null);
		}

		Util.applyRowStyle(view, position);

		return view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {

		if (v.getTag() instanceof Item) {
			Item item = (Item) v.getTag();
			switch (v.getId()) {
			case R.id.set1:
				hero.addEquippedItem(getContext(), item, null, null, 0, null);
				break;
			case R.id.set2:
				hero.addEquippedItem(getContext(), item, null, null, 1, null);
				break;
			case R.id.set3:
				hero.addEquippedItem(getContext(), item, null, null, 2, null);
				break;
			}
		} else if (v.getTag() instanceof EquippedItem) {
			EquippedItem item = (EquippedItem) v.getTag();
			hero.removeEquippedItem(item);
		}

	}

}
