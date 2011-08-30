package com.dsatab.data.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.dsatab.R;
import com.dsatab.data.Hero;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemType;
import com.dsatab.view.EquippedItemListItem;

public class EquippedItemListAdapter extends OpenArrayAdapter<Item> implements OnClickListener {

	private Hero hero;

	private ItemListFilter filter;

	public EquippedItemListAdapter(Context context, Hero hero, Item[] objects) {
		super(context, 0, objects);
		this.hero = hero;
	}

	public EquippedItemListAdapter(Context context, Hero hero, List<Item> objects) {
		super(context, 0, objects);
		this.hero = hero;
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
			// We need the layoutinflater to pick up the view from xml
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// Pick up the TwoLineListItem defined in the xml file
			view = (EquippedItemListItem) inflater.inflate(R.layout.equippeditem_listitem, parent, false);
		} else {
			view = (EquippedItemListItem) convertView;
		}

		Item item = getItem(position);
		view.setItem(item);

		if (item.isEquipable()) {

			view.getSet1().setBackgroundResource(R.drawable.button_disabled_patch);
			view.getSet1().setOnClickListener(this);
			view.getSet1().setTag(item);
			view.getSet2().setBackgroundResource(R.drawable.button_disabled_patch);
			view.getSet2().setOnClickListener(this);
			view.getSet2().setTag(item);
			view.getSet3().setBackgroundResource(R.drawable.button_disabled_patch);
			view.getSet3().setOnClickListener(this);
			view.getSet3().setTag(item);

			for (EquippedItem equippedItem : item.getEquippedItems()) {
				switch (equippedItem.getSet()) {
				case 0:
					view.getSet1().setBackgroundResource(R.drawable.icon_btn);
					view.getSet1().setTag(equippedItem);
					break;
				case 1:
					view.getSet2().setBackgroundResource(R.drawable.icon_btn);
					view.getSet2().setTag(equippedItem);
					break;
				case 2:
					view.getSet3().setBackgroundResource(R.drawable.icon_btn);
					view.getSet3().setTag(equippedItem);
					break;
				}
			}
		} else {
			view.getSet1().setTag(null);
			view.getSet2().setTag(null);
			view.getSet3().setTag(null);
		}

		if (position % 2 == 1) {
			view.setBackgroundResource(R.drawable.list_row_odd);
		} else {
			view.setBackgroundResource(R.drawable.list_row_even);
		}

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
				hero.addEquippedItem(getContext(), item, null, 0);
				break;
			case R.id.set2:
				hero.addEquippedItem(getContext(), item, null, 1);
				break;
			case R.id.set3:
				hero.addEquippedItem(getContext(), item, null, 2);
				break;
			}
		} else if (v.getTag() instanceof EquippedItem) {
			EquippedItem item = (EquippedItem) v.getTag();
			hero.removeEquippedItem(item);
		}

	}

}
